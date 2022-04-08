package org.elixir_lang.navigation.item_presentation.modular

import com.intellij.navigation.ItemPresentation
import org.elixir_lang.Icons
import org.elixir_lang.navigation.item_presentation.Parent
import org.elixir_lang.semantic.Modular
import org.jetbrains.annotations.Contract
import javax.swing.Icon

/**
 *
 * @param location the parent name of the Module that scopes `call`; `null` when scope is `quote`.
 * @param semantic a `Kernel.defmodule/2` call nested in `parent`.
 */
open class Module(
    private val location: String?,
    protected val semantic: Modular
) : ItemPresentation, Parent {
    /**
     * The module icon.
     *
     * @param unused Used to mean if open/close icons for tree renderer. No longer in use. The parameter is only there for API compatibility reason.
     */
    override fun getIcon(unused: Boolean): Icon = Icons.MODULE

    /**
     * Combines [.getLocationString] with [.getPresentableText] for when this Module is the parent of
     * another Module and needs to act as the location of the child Module.
     *
     * @return [.getLocationString] + "." + [.getPresentableText] if [.getLocationString] is not
     * `null`; otherwise, [.getPresentableText].
     */
    override fun getLocatedPresentableText(): String = locationString?.let { "$it $presentableText" } ?: presentableText

    /**
     * Returns the location of the object (for example, the package of a class). The location
     * string is used by some renderers and usually displayed as grayed text next to the item name.
     *
     * @return the location description, or null if none is applicable.
     */
    override fun getLocationString(): String? = location

    /**
     * Returns the name of the object to be presented in most renderers across the program.
     *
     * @return the function name.
     */
    override fun getPresentableText(): String = presentableText(definer = definer(), semantic = semantic)

    protected open fun definer(): String = "defmodule"

    companion object {
        @Contract(pure = true)
        fun presentableText(definer: String, semantic: Modular): String = "$definer ${semantic.canonicalName}"
    }
}
