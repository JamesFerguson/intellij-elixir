/*
 * Copyright 2012-2014 Sergey Ignatov
 * Copyright 2017 Jake Becker
 * Copyright 2017 Luke Imhoff
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.elixir_lang.debugger.stack_frame.value

import org.elixir_lang.utils.ElixirModulesUtil.erlangModuleNameToElixir
import com.intellij.xdebugger.frame.presentation.XValuePresentation
import com.ericsson.otp.erlang.OtpErlangMap
import com.ericsson.otp.erlang.OtpErlangAtom
import com.ericsson.otp.erlang.OtpErlangTuple
import com.ericsson.otp.erlang.OtpErlangList
import com.ericsson.otp.erlang.OtpErlangBitstr
import com.ericsson.otp.erlang.OtpErlangString
import com.ericsson.otp.erlang.OtpErlangDouble
import com.ericsson.otp.erlang.OtpErlangLong
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CharacterCodingException
import kotlin.String

class Presentation(private val myValue: Any) : XValuePresentation() {
    override fun renderValue(renderer: XValueTextRenderer) = renderObject(myValue, renderer)

    companion object {
        private fun renderObject(o: Any, renderer: XValueTextRenderer) {
            when (o) {
                is OtpErlangAtom -> renderAtom(o, renderer)
                is OtpErlangBitstr -> renderBitstr(o, renderer)
                is OtpErlangDouble, is OtpErlangLong -> renderer.renderNumericValue(o.toString())
                is OtpErlangList -> renderList(o, renderer)
                is OtpErlangMap -> renderMap(o, renderer)
                is OtpErlangString -> renderErlangString(o, renderer)
                is OtpErlangTuple -> renderTuple(o, renderer)
                else -> renderer.renderValue(o.toString())
            }
        }

        private fun renderMap(map: OtpErlangMap, renderer: XValueTextRenderer) {
            renderer.renderSpecialSymbol("%")

            if (isStruct(map)) {
                val structType = structType(map)!!
                renderer.renderKeywordValue(structType)
            }

            renderer.renderSpecialSymbol("{")

            var first = true
            val symbolKeys = hasSymbolKeys(map)

            for ((key, value) in map.entrySet()) {
                if (!(isStruct(map) && key is OtpErlangAtom && key.atomValue() == "__struct__")) {
                    if (first) {
                        first = false
                    } else {
                        renderer.renderSpecialSymbol(", ")
                    }
                    if (symbolKeys) {
                        assert(key is OtpErlangAtom)
                        renderer.renderKeywordValue((key as OtpErlangAtom).atomValue())
                        renderer.renderKeywordValue(": ")
                    } else {
                        renderObject(key, renderer)
                        renderer.renderSpecialSymbol(" => ")
                    }
                    renderObject(value, renderer)
                }
            }

            renderer.renderSpecialSymbol("}")
        }

        private fun renderAtom(atom: OtpErlangAtom, renderer: XValueTextRenderer) {
            renderer.renderKeywordValue(erlangModuleNameToElixir(atom.atomValue()))
        }

        private fun renderTuple(tuple: OtpErlangTuple, renderer: XValueTextRenderer) {
            renderer.renderSpecialSymbol("{")
            var i = 0
            while (i < tuple.arity()) {
                if (i > 0) {
                    renderer.renderSpecialSymbol(", ")
                }
                renderObject(tuple.elementAt(i), renderer)
                i++
            }
            renderer.renderSpecialSymbol("}")
        }

        private fun renderList(list: OtpErlangList, renderer: XValueTextRenderer) {
            renderer.renderSpecialSymbol("[")
            var i = 0
            while (i < list.arity()) {
                if (i > 0) {
                    renderer.renderSpecialSymbol(", ")
                }
                renderObject(list.elementAt(i), renderer)
                i++
            }

            // Improper lists have a lastTail
            val lastTail = list.lastTail
            if (lastTail != null) {
                // Improper lists need to render the head tail joiner, `|`, explicitly
                renderer.renderSpecialSymbol(" | ")
                renderObject(lastTail, renderer)
            }
            renderer.renderSpecialSymbol("]")
        }

        private fun renderBitstr(bitstr: OtpErlangBitstr, renderer: XValueTextRenderer) {
            val utf8String = toUtf8String(bitstr)

            if (utf8String != null) {
                renderer.renderStringValue(utf8String)
            } else {
                renderer.renderSpecialSymbol("<<")
                var first = true
                for (b in bitstr.binaryValue()) {
                    if (!first) renderer.renderSpecialSymbol(", ")
                    renderer.renderValue("" + (b.toInt() and 0xFF))
                    first = false
                }
                if (bitstr.pad_bits() > 0) {
                    renderer.renderSpecialSymbol("::size(" + (8 - bitstr.pad_bits()) + ")")
                }
                renderer.renderSpecialSymbol(">>")
            }
        }

        fun isPrintable(s: OtpErlangString): Boolean =
            s.toString().all { isPrintable(it) }

        private fun isPrintable(c: Char): Boolean {
            return (c.toInt() in 32..126
                    || c == '\n' || c == '\r' || c == '\t' || c.toInt() == 11 || c == '\b' || c == '\u000c' || c.toInt() == 27 || c.toInt() == 7) /* bell */
        }

        private fun renderErlangString(str: OtpErlangString, renderer: XValueTextRenderer) {
            if (isPrintable(str)) {
                renderer.renderSpecialSymbol("'")
                renderer.renderValue(
                        str
                                .stringValue()
                                .replace("\\x", "\\\\x")
                                .replace("'", "\\'")
                )
                renderer.renderSpecialSymbol("'")
            } else {
                renderObject(OtpErlangList(str.stringValue()), renderer)
            }
        }

        fun toUtf8String(bitstr: OtpErlangBitstr): String? = if (bitstr.pad_bits() > 0) null else try {
            Charset.availableCharsets()["UTF-8"]!!.newDecoder().decode(ByteBuffer.wrap(bitstr.binaryValue())).toString()
        } catch (e: CharacterCodingException) {
            null
        }

        private fun structType(map: OtpErlangMap): String? {
            val structValue = map[OtpErlangAtom("__struct__")]
            return if (structValue is OtpErlangAtom) {
                erlangModuleNameToElixir(structValue.atomValue())
            } else {
                null
            }
        }

        private fun isStruct(map: OtpErlangMap): Boolean = structType(map) != null

        fun hasSymbolKeys(map: OtpErlangMap): Boolean =
            map.keys().all { key ->
                key is OtpErlangAtom && !key.atomValue().startsWith("Elixir.")
            }
    }
}
