package org.elixir_lang.beam.decompiler

import org.elixir_lang.NameArity
import java.lang.StringBuilder

open class Default : MacroNameArity() {
    /**
     * Whether the decompiler accepts the `macroNameArity`
     *
     * @return `true`
     */
    override fun accept(beamLanguage: String, nameArity: NameArity): Boolean =
        true

    /**
     * Append the decompiled source for `macroNameArity` to `decompiled`.
     *
     * @param decompiled the decompiled source so far
     */
    override fun append(decompiled: StringBuilder, macroNameArity: org.elixir_lang.beam.MacroNameArity) {
        appendMacro(decompiled, macroNameArity)
        val parameters = parameters(macroNameArity)
        appendSignature(decompiled, macroNameArity, macroNameArity.name, parameters)
        appendBody(decompiled)
    }

    private fun appendMacro(decompiled: StringBuilder, macroNameArity: org.elixir_lang.beam.MacroNameArity) {
        decompiled
                .append("  ")
                .append(macroNameArity.macro)
                .append(" ")
    }

    open fun parameters(macroNameArity: org.elixir_lang.beam.MacroNameArity): Array<String> =
            (0 until  macroNameArity.arity)
                    .map { i ->
                        "p${i}"
                    }
                    .toTypedArray()

    override fun appendSignature(decompiled: StringBuilder,
                                 macroNameArity: org.elixir_lang.beam.MacroNameArity,
                                 name: String,
                                 parameters: Array<String>) {
        appendName(decompiled, macroNameArity.name)
        decompiled.append('(')
        for (i in parameters.indices) {
            if (i != 0) {
                decompiled.append(", ")
            }
            decompiled.append(parameters[i])
        }
        decompiled.append(')')
    }

    override fun appendName(decompiled: StringBuilder, name: String) {
        decompiled.append(name)
    }

    companion object {
        val INSTANCE = Default()
    }
}
