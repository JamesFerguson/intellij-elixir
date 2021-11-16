package org.elixir_lang

import com.intellij.codeInsight.TargetElementEvaluatorEx2
import com.intellij.psi.*
import org.elixir_lang.psi.AtOperation
import org.elixir_lang.psi.QualifiedNoArgumentsCall
import org.elixir_lang.psi.UnqualifiedNoArgumentsCall
import org.elixir_lang.psi.call.Call
import org.elixir_lang.psi.operation.Multiplication
import org.elixir_lang.psi.operation.capture.NonNumeric
import org.elixir_lang.psi.scope.ancestorTypeSpec
import org.elixir_lang.psi.scope.hasMapFieldOptionalityName
import org.elixir_lang.reference.Module
import org.elixir_lang.reference.Resolver

class TargetElementEvaluator : TargetElementEvaluatorEx2() {
    override fun isAcceptableNamedParent(parent: PsiElement): Boolean = when (parent) {
        // Don't allow the identifier of a module attribute or assign usage be a named parent.
        is UnqualifiedNoArgumentsCall<*> -> when (val grandParent = parent.parent) {
            is AtOperation -> false
            // Don't allow `name` in `&name/arity` to be named parent. The capture needs to be the parent.
            is Multiplication -> when (grandParent.parent) {
                is NonNumeric -> false
                else -> super.isAcceptableNamedParent(parent)
            }
            else -> super.isAcceptableNamedParent(parent)
        }
        // Don't allow `Mod.name` in `&Mod.name/arity` to be named parent.  The capture needs to be the parent.
        is QualifiedNoArgumentsCall<*>  -> when (val grandParent = parent.parent) {
            is Multiplication -> when (grandParent.parent) {
                is NonNumeric -> false
                else -> super.isAcceptableNamedParent(parent)
            }
            else -> super.isAcceptableNamedParent(parent)
        }
        is Call -> {
            if (parent.hasMapFieldOptionalityName() && parent.ancestorTypeSpec() != null) {
                false
            } else {
                super.isAcceptableNamedParent(parent)
            }
        }
        else -> super.isAcceptableNamedParent(parent)
    }

    override fun getTargetCandidates(reference: PsiReference): MutableCollection<PsiElement>? =
        when (reference) {
            // Module references need to resolve to decompiled element in case a call is only defined in the decompiled
            // code and not the source, but users prefer source for the actual Alias Go To Declaration.
            is Module -> {
                reference
                        .multiResolve(false)
                        .mapNotNull(ResolveResult::getElement)
                        .let { Resolver.preferSource(it) }
                        .toMutableList()
            }
            else -> super.getTargetCandidates(reference)
        }
}
