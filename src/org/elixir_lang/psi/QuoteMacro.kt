package org.elixir_lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import org.elixir_lang.psi.call.Call
import org.elixir_lang.psi.call.name.Function.*
import org.elixir_lang.psi.call.name.Module.KERNEL
import org.elixir_lang.psi.impl.call.macroChildCallSequence
import org.elixir_lang.psi.impl.call.whileInStabBodyChildExpressions
import org.elixir_lang.psi.scope.WhileIn.whileIn

object QuoteMacro {
    fun treeWalkUp(quoteCall: Call, resolveState: ResolveState, keepProcessing: (PsiElement, ResolveState) -> Boolean): Boolean =
            if (!resolveState.containsAncestorUnquote(quoteCall)) {
                quoteCall
                        .macroChildCallSequence()
                        .filter { !resolveState.hasBeenVisited(it) }
                        .let { treeWalkUp(it, resolveState.putVisitedElement(quoteCall), keepProcessing) }
            } else {
                true
            }

    fun treeWalkUp(childCallSequence: Sequence<Call>,
                   resolveState: ResolveState,
                   keepProcessing: (PsiElement, ResolveState) -> Boolean): Boolean {
        var accumulatorKeepProcessing = true

        for (childCall in childCallSequence) {
            accumulatorKeepProcessing = when {
                If.`is`(childCall) || Unless.`is`(childCall) -> {
                    val branches = Branches(childCall)

                    val primaryKeepProcessing = whileIn(branches.primaryChildExpressions) {
                        keepProcessing(it, resolveState)
                    }

                    val alternativeKeepProcessing = whileIn(branches.alternativeChildExpressions) {
                        keepProcessing(it, resolveState)
                    }

                    primaryKeepProcessing && alternativeKeepProcessing
                }
                Import.`is`(childCall) -> Import.treeWalkUp(childCall, resolveState, keepProcessing)
                Unquote.`is`(childCall) -> Unquote.treeWalkUp(childCall, resolveState, keepProcessing)
                Use.`is`(childCall) -> Use.treeWalkUp(childCall, resolveState, keepProcessing)
                childCall.isCalling(KERNEL, TRY) -> {
                    childCall.whileInStabBodyChildExpressions { grandChildExpression ->
                        keepProcessing(grandChildExpression, resolveState)
                    }
                }
                else -> keepProcessing(childCall, resolveState)
            }

            if (!accumulatorKeepProcessing) {
                break
            }
        }

        return accumulatorKeepProcessing
    }

    @JvmStatic
    fun `is`(call: Call): Boolean {
        // TODO change Elixir.Kernel to Elixir.Kernel.SpecialForms when resolving works
        return call.isCallingMacro(KERNEL, QUOTE, 1) || // without keyword arguments
                call.isCallingMacro(KERNEL, QUOTE, 2) // with keyword arguments
    }

}
