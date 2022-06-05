package org.elixir_lang.beam.psi.impl

import com.intellij.lang.ASTNode
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.StubBasedPsiElement
import com.intellij.psi.impl.source.SourceTreeToPsiMap
import com.intellij.psi.impl.source.tree.TreeElement
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.stubs.IStubElementType
import com.intellij.util.IncorrectOperationException
import org.elixir_lang.beam.psi.Module
import org.elixir_lang.beam.psi.stubs.ModuleStub
import org.elixir_lang.beam.psi.stubs.ModuleStubElementTypes
import org.elixir_lang.psi.CallDefinitionClause
import org.elixir_lang.psi.ElixirUnmatchedAtUnqualifiedNoParenthesesCall
import org.elixir_lang.psi.Modular.callDefinitionClauseCallWhile
import org.elixir_lang.psi.call.Call
import org.elixir_lang.psi.impl.call.macroChildCallSequence
import org.elixir_lang.psi.impl.getModuleName
import org.elixir_lang.psi.putInitialVisitedElement
import org.elixir_lang.structure_view.element.CallDefinitionHead
import org.elixir_lang.structure_view.element.Type
import org.jetbrains.annotations.Contract
import org.jetbrains.annotations.NonNls

// See com.intellij.psi.impl.compiled.ClsClassImpl
class ModuleImpl<T : ModuleStub<*>?>(private val stub: T) : ModuleElementImpl(), Module, StubBasedPsiElement<T> {
    override fun getName(): String = stub!!.name

    /**
     * Returns the array of children for the PSI element.
     * Important: In some implementations children are only composite elements, i.e. not a leaf elements
     *
     * @return the array of child elements.
     */
    override fun getChildren(): Array<PsiElement> = stub!!.childrenStubs.map { it.psi }.toTypedArray()

    /**
     * Returns the parent of the PSI element.
     *
     * @return the parent of the element, or null if the element has no parent.
     */
    override fun getParent(): PsiElement = stub!!.getParentStub().psi

    override fun getElementType(): IStubElementType<*, *> = stub!!.getStubType()

    override fun getStub(): T = stub

    override fun appendMirrorText(buffer: StringBuilder, indentLevel: Int) {
    }

    override fun setMirror(element: TreeElement) {
        setMirrorCheckingType(element, null)

        val typeByArityByName = typeDefinitionByArityByName(element)

        for (typeDefinitionStub in typeDefinitions()) {
            val name = typeDefinitionStub.name
            val typeByArity = typeByArityByName[name]

            if (typeByArity != null) {
                val arity = typeDefinitionStub.arity
                val type = typeByArity[arity]

                if (type != null) {
                    (typeDefinitionStub as ModuleElementImpl).setMirror(SourceTreeToPsiMap.psiToTreeNotNull(type))
                } else {
                    LOGGER.error(
                        "No decompiled source type with name/arity (${moduleName(element)}.${name}/${arity})"
                    )
                }
            } else if (typeDefinitionStub.visibility in
                arrayOf(org.elixir_lang.type.Visibility.PUBLIC, org.elixir_lang.type.Visibility.OPAQUE)
            ) {
                LOGGER.error("No decompiled source type with name (${moduleName(element)}.$name)")
            }
        }

        val callDefinitionClauseByArityByName = callDefinitionClauseByArityByName(element)
        val state = ResolveState.initial()

        for (callDefinitionStub in callDefinitions()) {
            val name = callDefinitionStub.exportedName()
            val callDefinitionClauseByArity = callDefinitionClauseByArityByName[name]

            if (callDefinitionClauseByArity != null) {
                val arity = callDefinitionStub.exportedArity(state)
                val callDefinitionClause = callDefinitionClauseByArity[arity]

                if (callDefinitionClause != null) {
                    (callDefinitionStub as ModuleElementImpl).setMirror(
                        SourceTreeToPsiMap.psiToTreeNotNull(
                            callDefinitionClause
                        )
                    )
                } else if (callDefinitionStub.isExported) {
                    LOGGER.error("No decompiled source function with name/arity (${moduleName(element)}.${name}/${arity})")
                }
            } else if (callDefinitionStub.isExported) {
                LOGGER.error("No decompiled source function with name (${moduleName(element)}.$name)")
            }
        }
    }

    private fun moduleName(element: TreeElement): String {
        return element.psi.let { it as? Call }?.getModuleName() ?: "<unknown module>"
    }

    fun callDefinitions(): Array<CallDefinitionImpl<*>> =
        stub!!.getChildrenByType(ModuleStubElementTypes.CALL_DEFINITION, emptyArray())

    fun typeDefinitions(): Array<TypeDefinitionImpl<*>> =
        stub!!.getChildrenByType(ModuleStubElementTypes.TYPE_DEFINITION, emptyArray())

    /**
     * @return `null` if it does not have a canonical name OR if it has more than one canonical name
     */
    override fun canonicalName(): String? {
        assert(stub != null)
        return null
    }

    /**
     * @return empty set if no canonical names
     */
    override fun canonicalNameSet(): Set<String> {
        assert(stub != null)
        return emptySet()
    }

    override fun getNameIdentifier(): PsiElement = this

    /**
     * Renames the element.
     *
     * @param name the new element name.
     * @return the element corresponding to this element after the rename (either `this`
     * or a different element if the rename caused the element to be replaced).
     * @throws IncorrectOperationException if the modification is not supported or not possible for some reason.
     */
    override fun setName(@NonNls name: String): PsiElement =
        throw IncorrectOperationException("Cannot modify module name in Beam files")

    override fun getNavigationElement(): PsiElement = mirror

    override fun processDeclarations(
        processor: PsiScopeProcessor,
        state: ResolveState,
        lastParent: PsiElement?,
        place: PsiElement
    ): Boolean =
        processor.execute(this, state)

    override fun getNode(): ASTNode? = null

    override fun getProject(): Project {
        return parent.project
    }

    companion object {
        private val LOGGER = Logger.getInstance(ModuleImpl::class.java)

        private fun typeDefinitionByArityByName(mirror: TreeElement): Map<String, Map<Int, Call>> {
            val mirrorPsi = mirror.psi

            return if (mirrorPsi is Call) {
                val typeByArityByName = mutableMapOf<String, MutableMap<Int, Call>>()

                val typeDefinitions = mirrorPsi.macroChildCallSequence()
                    .filter { Type.`is`(it) }
                    .filterIsInstance<ElixirUnmatchedAtUnqualifiedNoParenthesesCall>()

                for (typeDefinition in typeDefinitions) {
                    Type
                        .type(typeDefinition)
                        ?.let { org.elixir_lang.navigation.item_presentation.Type.head(it) }
                        ?.let { CallDefinitionHead.nameArityInterval(it, ResolveState.initial()) }
                        ?.let { nameArityInterval ->
                            val typeByArity = typeByArityByName.getOrPut(nameArityInterval.name) {
                                mutableMapOf()
                            }

                            nameArityInterval.arityInterval.closed().forEach { arity ->
                                typeByArity[arity] = typeDefinition
                            }
                        }
                }

                typeByArityByName
            } else {
                emptyMap()
            }
        }

        @Contract(pure = true)
        private fun callDefinitionClauseByArityByName(mirror: TreeElement): Map<String, Map<Int, Call>> {
            val mirrorPsi = mirror.psi

            return if (mirrorPsi is Call) {
                val initialResolveState = ResolveState.initial().putInitialVisitedElement(mirrorPsi)
                val callDefinitionByArityByName = mutableMapOf<String, MutableMap<Int, Call>>()

                callDefinitionClauseCallWhile(
                    mirrorPsi,
                    initialResolveState
                ) { call: Call, accResolvedState: ResolveState ->
                    CallDefinitionClause.nameArityInterval(call, accResolvedState)?.let { nameArityInterval ->
                        val callDefinitionByArity = callDefinitionByArityByName.getOrPut(nameArityInterval.name) {
                            mutableMapOf()
                        }

                        nameArityInterval.arityInterval.closed().forEach { arity ->
                            callDefinitionByArity[arity] = call
                        }
                    }

                    true
                }

                callDefinitionByArityByName
            } else {
                emptyMap()
            }
        }
    }
}
