package org.elixir_lang.beam.chunk.debug_info.v1.erl_abstract_code.abstract_code_compiler_options.abstract_code

import com.ericsson.otp.erlang.OtpErlangAtom
import com.ericsson.otp.erlang.OtpErlangLong
import com.ericsson.otp.erlang.OtpErlangObject
import com.ericsson.otp.erlang.OtpErlangTuple
import org.elixir_lang.beam.decompiler.Options

fun Node?.toString(options: Options) = this?.toMacroString(options) ?: "?"

abstract class Node(term: OtpErlangTuple): ToMacroString {
    val line by lazy { (term.elementAt(1) as? OtpErlangLong)?.longValue() }

    abstract override fun toMacroString(options: Options): String

    companion object {
        inline fun <T : Node> ifTag(term: OtpErlangObject, tag: String, ifTrue: (OtpErlangTuple) -> T?): T? =
            when (term) {
                is OtpErlangTuple -> ifTag(term, tag, ifTrue)
                else -> null
            }

        inline fun <T: Node> ifTag(term: OtpErlangTuple, tag: String, ifTrue: (OtpErlangTuple) -> T?): T? =
            (term.elementAt(0) as? OtpErlangAtom)?.let { actualTag ->
                if (actualTag.atomValue() == tag) {
                    ifTrue(term)
                } else {
                    null
                }
            }
    }
}
