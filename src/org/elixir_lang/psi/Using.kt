package org.elixir_lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.ResolveState
import com.intellij.psi.util.siblings
import org.elixir_lang.psi.CallDefinitionClause.nameArityRange
import org.elixir_lang.psi.call.Call
import org.elixir_lang.psi.call.name.Function.*
import org.elixir_lang.psi.call.name.Module.KERNEL
import org.elixir_lang.psi.impl.call.finalArguments
import org.elixir_lang.psi.impl.call.macroChildCallSequence
import org.elixir_lang.psi.impl.maybeModularNameToModular
import org.elixir_lang.psi.impl.stripAccessExpression

object Using {
    fun treeWalkUp(usingCall: Call, useCall: Call?, resolveState: ResolveState, keepProcessing: (PsiElement, ResolveState) -> Boolean): Boolean =
            usingCall
                    .doBlock
                    ?.stab
                    ?.stabBody
                    ?.lastChild
                    ?.let { lastChild ->
                        lastChild
                                .siblings(forward = false, withSelf = true)
                                .filterIsInstance<Call>()
                                .lastOrNull()
                    }
                    ?.let { lastChildCall -> treeWalkUpFromLastChildCall(lastChildCall, useCall, resolveState, keepProcessing) }
                    ?:
                    true

    private fun treeWalkUpFromLastChildCall(lastChildCall: Call, useCall: Call?, resolveState: ResolveState, keepProcessing: (PsiElement, ResolveState) -> Boolean): Boolean {
        val resolvedModuleName = lastChildCall.resolvedModuleName()
        val functionName = lastChildCall.functionName()

        return if (resolvedModuleName != null && functionName != null) {
            when {
                resolvedModuleName == KERNEL && functionName == QUOTE -> {
                    val lastChildCallResolveState = resolveState.putVisitedElement(lastChildCall)

                    QuoteMacro.treeWalkUp(lastChildCall, lastChildCallResolveState, keepProcessing)
                }

                resolvedModuleName == KERNEL && functionName == APPLY -> {
                    lastChildCall.finalArguments()?.let { arguments ->
                        // TODO pipelines to apply/3
                        if (arguments.size == 3) {
                            arguments[0].let { maybeModularName ->
                                maybeModularName.maybeModularNameToModular(maxScope = maybeModularName.containingFile, useCall = useCall)?.let { modular ->
                                    val modularResolveState = resolveState.putVisitedElement(modular)

                                    val name = useCall?.finalArguments()?.let { arguments ->
                                        if (arguments.size == 2) {
                                            when (val which = arguments[1].stripAccessExpression()) {
                                                is ElixirAtom -> if (which.charListLine == null && which.stringLine == null) {
                                                    which.lastChild.text
                                                } else {
                                                    null
                                                }
                                                else -> null
                                            }
                                        } else {
                                            null
                                        }
                                    }

                                    if (name != null) {
                                        Modular.callDefinitionClauseCallFoldWhile(modular, name, modularResolveState) { callDefinitionClauseCall, _, arityRange, accResolveState ->
                                            val finalContinue = treeWalkUp(callDefinitionClauseCall, useCall, accResolveState, keepProcessing)
                                            AccumulatorContinue(accResolveState, finalContinue)
                                        }.`continue`
                                    } else {
                                        Modular.callDefinitionClauseCallWhile(modular, modularResolveState) { callDefinitionClauseCall, accResolveState ->
                                            if (CallDefinitionClause.isFunction(callDefinitionClauseCall)) {
                                                treeWalkUp(callDefinitionClauseCall, useCall, accResolveState, keepProcessing)
                                            } else {
                                                true
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            true
                        }
                    } ?: true
                }
                else -> {
                    var accumulatedKeepProcessing = true

                    for (reference in lastChildCall.references) {
                        val resolvedList: List<PsiElement> = if (reference is PsiPolyVariantReference) {
                            reference
                                    .multiResolve(false)
                                    .mapNotNull { it.element }
                        } else {
                            reference.resolve()?.let { listOf(it) } ?: emptyList()
                        }

                        for (resolved in resolvedList) {
                            accumulatedKeepProcessing = if (resolved is Call && CallDefinitionClause.`is`(resolved)) {
                                val resolvedResolveSet = resolveState.putVisitedElement(resolved)

                                treeWalkUp(
                                        usingCall = resolved,
                                        useCall = useCall,
                                        resolveState = resolvedResolveSet,
                                        keepProcessing = keepProcessing
                                )
                            } else {
                                true
                            }

                            if (!accumulatedKeepProcessing) {
                                break
                            }
                        }

                        if (!accumulatedKeepProcessing) {
                            break
                        }
                    }

                    accumulatedKeepProcessing
                }
            }
        } else {
            true
        }
    }

    fun definers(modularCall: Call): Sequence<Call> =
            modularCall
                    .macroChildCallSequence()
                    .filter { isDefiner(it) }

    private const val ARITY = 1
    private const val USING = "__using__"

    private fun isDefiner(call: Call): Boolean =
            call.isCalling(KERNEL, DEFMACRO) &&
                    nameArityRange(call)?.let { nameArityRange ->
                        nameArityRange.name == USING && nameArityRange.arityRange.contains(ARITY)
                    }
                    ?: false
}
