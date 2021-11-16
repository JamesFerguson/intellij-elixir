package org.elixir_lang.psi.impl

import com.ericsson.otp.erlang.*
import com.intellij.lang.ASTNode
import org.elixir_lang.psi.*
import org.elixir_lang.psi.call.name.Module
import org.elixir_lang.psi.impl.QuotableImpl.metadata
import org.elixir_lang.psi.impl.QuotableImpl.quotedFunctionCall
import org.jetbrains.annotations.Contract
import java.nio.charset.Charset

object ParentImpl {
    @JvmStatic
    fun addChildTextCodePoints(codePointList: MutableList<Int>?, child: ASTNode): MutableList<Int> =
            addStringCodePoints(codePointList, child.text)

    fun elixirCharList(codePointList: List<Int>): OtpErlangObject =
        elixirCodePointList(codePointList).let { elixirCharList(it) }

    /**
     * Erlang will automatically stringify a list that is just a list of LATIN-1 printable code
     * points.
     * OtpErlangString and OtpErlangList are not equal when they have the same content, so to check against
     * Elixir.Code.string_to_quoted, this code must determine if Erlang would return an OtpErlangString instead
     * of OtpErlangList and do the same.
     */
    fun elixirCharList(erlangList: OtpErlangList): OtpErlangObject =
        /* JInterface will return an OtpErlangString in some case and an OtpErlangList in other.  Right now, I'm
           assuming it works similar to the printing in `iex` and is based on whether the codePoint is printable, but
           ASCII printable instead of Unicode printable since Erlang is ASCII/LATIN-1 based */
        if (isErlangPrintable(erlangList)) {
            try {
                OtpErlangString(erlangList)
            } catch (e: OtpErlangException) {
                TODO()
            }
        } else {
            erlangList
        }

    fun elixirString(codePointList: List<Int>): OtpErlangBinary {
        val stringAccumulator = StringBuilder()

        for (codePoint in codePointList) {
            stringAccumulator.appendCodePoint(codePoint)
        }

        return elixirString(stringAccumulator.toString())
    }

    @JvmStatic
    fun elixirString(javaString: String): OtpErlangBinary =
        javaString.toByteArray(Charset.forName("UTF-8")).let(::OtpErlangBinary)

    // Parent methods

    @JvmStatic
    fun addEscapedCharacterCodePoints(parent: Quote, maybeCodePointList: MutableList<Int>?, child: ASTNode): List<Int> {
        val escapedCharacterCodePoint = child.psi.let { it as ElixirEscapedCharacter }.codePoint()

        val codePointList = ensureCodePointList(maybeCodePointList)
        codePointList.add(escapedCharacterCodePoint)

        return codePointList
    }

    @JvmStatic
    fun addEscapedCharacterCodePoints(parent: Sigil, codePointList: MutableList<Int>?, child: ASTNode): List<Int> {
        val childText = child.text

        // Not sure, why, but \ gets stripped in front of # when quoting using Quoter prior to 1.6
        val string = if (parent is SigilLine) {
            val terminator = parent.terminator()

            if (childText == "\\" + terminator) {
                String(
                        charArrayOf(terminator)
                )
            } else {
                childText
            }
        } else {
            childText
        }

        return addStringCodePoints(codePointList, string)
    }

    @JvmStatic
    fun addEscapedEOL(parent: Parent,
                      maybeCodePointList: MutableList<Int>?): List<Int> {
        val codePointList: MutableList<Int> = ensureCodePointList(maybeCodePointList)

        if (parent is Literal) {
            for (codePoint in codePoints("\\\n")) {
                codePointList.add(codePoint)
            }
        }

        return codePointList
    }

    @JvmStatic
    fun addFragmentCodePoints(parent: Parent, codePointList: MutableList<Int>?, child: ASTNode): List<Int> =
            addChildTextCodePoints(codePointList, child)

    @JvmStatic
    fun addHexadecimalEscapeSequenceCodePoints(parent: Quote, maybeCodePointList: MutableList<Int>?, child: ASTNode): List<Int> {
        val hexadecimalEscapeSequenceCodePoint = child
                .psi.let { it as ElixirQuoteHexadecimalEscapeSequence }
                .codePoint()

        val codePointList = ensureCodePointList(maybeCodePointList)
        codePointList.add(hexadecimalEscapeSequenceCodePoint)

        return codePointList
    }

    @JvmStatic
    fun addHexadecimalEscapeSequenceCodePoints(parent: Sigil, codePointList: MutableList<Int>?, child: ASTNode): List<Int> =
            addChildTextCodePoints(codePointList, child)

    // See https://github.com/elixir-lang/elixir/commit/e89e9d874bf803379d729a3bae185052a5323a85
    @Contract(pure = true)
    @JvmStatic
    fun quoteBinary(interpolatedCharList: InterpolatedCharList, metadata: OtpErlangList, argumentList: List<OtpErlangObject>): OtpErlangObject =
            quotedFunctionCall(
                    "Elixir.List",
                    "to_charlist",
                    metadata(interpolatedCharList),
                    OtpErlangList(argumentList.toTypedArray())
            )

    @Contract(pure = true)
    @JvmStatic
    fun quoteBinary(interpolatedString: InterpolatedString, metadata: OtpErlangList, argumentList: List<OtpErlangObject>): OtpErlangObject =
            quotedFunctionCall("<<>>", metadata, *argumentList.toTypedArray())

    @Contract(pure = true)
    @JvmStatic
    fun quoteBinary(sigil: Sigil, metadata: OtpErlangList, argumentList: List<OtpErlangObject>): OtpErlangObject =
            quotedFunctionCall("<<>>", metadata, *argumentList.toTypedArray())

    @Contract(pure = true)
    @JvmStatic
    fun quoteEmpty(interpolatedCharList: InterpolatedCharList): OtpErlangObject = OtpErlangList()

    @Contract(pure = true)
    @JvmStatic
    fun quoteEmpty(interpolatedString: InterpolatedString): OtpErlangObject = elixirString("")

    @Contract(pure = true)
    @JvmStatic
    fun quoteEmpty(sigil: Sigil): OtpErlangObject = elixirString("")

    // See https://github.com/elixir-lang/elixir/commit/e89e9d874bf803379d729a3bae185052a5323a85
    @JvmStatic
    fun quoteInterpolation(interpolatedCharList: InterpolatedCharList, interpolation: ElixirInterpolation): OtpErlangObject {
        val quotedChildren = QuotableImpl.quote(interpolation.children)
        val interpolationMetadata = metadata(interpolation)

        return quotedFunctionCall(
                Module.prependElixirPrefix(Module.KERNEL),
                "to_string",
                interpolationMetadata,
                quotedChildren
        )
    }

    /* "#{a}" is transformed to "<<Kernel.to_string(a) :: binary>>" in
     * `"\"\#{a}\"" |> Code.string_to_quoted |> Macro.to_string`, so interpolation has to be represented as a type call
     * (`:::`) to binary of a call of `Kernel.to_string`
     */
    @JvmStatic
    fun quoteInterpolation(interpolatedString: InterpolatedString, interpolation: ElixirInterpolation): OtpErlangObject {
        val quotedChildren = QuotableImpl.quote(interpolation.children)
        val interpolationMetadata = metadata(interpolation)

        val quotedKernelToStringCall = quotedFunctionCall(
                Module.prependElixirPrefix(Module.KERNEL),
                "to_string",
                interpolationMetadata,
                quotedChildren
        )
        val quotedBinaryCall = QuotableImpl.quotedVariable(
                "binary",
                interpolationMetadata
        )

        return quotedFunctionCall(
                "::",
                interpolationMetadata,
                quotedKernelToStringCall,
                quotedBinaryCall
        )
    }

    /* "#{a}" is transformed to "<<Kernel.to_string(a) :: binary>>" in
     * `"\"\#{a}\"" |> Code.string_to_quoted |> Macro.to_string`, so interpolation has to be represented as a type call
     * (`:::`) to binary of a call of `Kernel.to_string`
     */
    @JvmStatic
    fun quoteInterpolation(sigil: Sigil, interpolation: ElixirInterpolation): OtpErlangObject {
        val quotedChildren = QuotableImpl.quote(interpolation.children)
        val interpolationMetadata = metadata(interpolation)

        val quotedKernelToStringCall = quotedFunctionCall(
                Module.prependElixirPrefix(Module.KERNEL),
                "to_string",
                interpolationMetadata,
                quotedChildren
        )
        val quotedBinaryCall = QuotableImpl.quotedVariable(
                "binary",
                interpolationMetadata
        )

        return quotedFunctionCall(
                "::",
                interpolationMetadata,
                quotedKernelToStringCall,
                quotedBinaryCall
        )
    }

    @Contract(pure = true)
    @JvmStatic
    fun quoteLiteral(interpolatedCharList: InterpolatedCharList, codePointList: List<Int>): OtpErlangObject =
            elixirCharList(codePointList)

    @Contract(pure = true)
    @JvmStatic
    fun quoteLiteral(interpolatedString: InterpolatedString, codePointList: List<Int>): OtpErlangObject =
            elixirString(codePointList)

    @Contract(pure = true)
    @JvmStatic
    fun quoteLiteral(sigil: Sigil, codePointList: List<Int>): OtpErlangObject = elixirString(codePointList)

    private fun addStringCodePoints(maybeCodePointList: MutableList<Int>?, string: String): MutableList<Int> {
        val codePointList = ensureCodePointList(maybeCodePointList)
        val filteredString = filterEscapedEOL(string)

        for (codePoint in codePoints(filteredString)) {
            codePointList.add(codePoint)
        }

        return codePointList
    }

    /*
     * @todo use String.codePoints in Java 8 when IntelliJ is using it
     * @see https://stackoverflow.com/questions/1527856/how-can-i-iterate-through-the-unicode-codepoints-of-a-java-string/21791059#21791059
     */
    private fun codePoints(string: String): Iterable<Int> =
            Iterable {
                object : Iterator<Int> {
                    internal var nextIndex = 0

                    override fun hasNext(): Boolean {
                        return nextIndex < string.length
                    }

                    override fun next(): Int {
                        val result = string.codePointAt(nextIndex)
                        nextIndex += Character.charCount(result)
                        return result
                    }
                }
            }

    private fun elixirCodePointList(codePointList: List<Int>): OtpErlangList =
            codePointList.map { OtpErlangLong(it.toLong()) }.toTypedArray().let { OtpErlangList(it) }

    private fun ensureCodePointList(codePointList: MutableList<Int>?): MutableList<Int> =
        codePointList ?: mutableListOf()

    private fun filterEscapedEOL(unfiltered: String): String = unfiltered.replace("\\\n", "")

    private fun isErlangPrintable(erlangList: OtpErlangList): Boolean {
        var isErlangPrintable = true

        for (erlangObject in erlangList) {
            if (erlangObject is OtpErlangLong) {
                val codePoint: Int

                try {
                    codePoint = erlangObject.intValue()
                } catch (e: OtpErlangRangeException) {
                    isErlangPrintable = false
                    break
                }

                if (!isErlangPrintable(codePoint)) {
                    isErlangPrintable = false
                    break
                }
            } else {
                isErlangPrintable = false
                break
            }
        }

        if (erlangList.arity() == 0) {
            isErlangPrintable = false
        }

        return isErlangPrintable
    }

    @Contract(pure = true)
    private fun isErlangPrintable(codePoint: Int): Boolean = codePoint in 0..255
}
