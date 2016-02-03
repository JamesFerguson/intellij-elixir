package org.elixir_lang.navigation.item_presentation;

import com.intellij.navigation.ItemPresentation;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import org.elixir_lang.icons.ElixirIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class Macro implements ItemPresentation {
    /*
     * Fields
     */

    private final int arity;
    @NotNull
    private final String name;
    @NotNull
    private final Parent parent;

    public Macro(@NotNull Parent parent, @NotNull String name, int arity) {
        this.arity = arity;
        this.name = name;
        this.parent = parent;
    }

    /**
     * The name/arity of this function.
     *
     * @return {@code name}/{@code arity}
     */
    @NotNull
    @Override
    public String getPresentableText() {
        return name + "/" + arity;
    }

    /**
     * Returns the location of the object (for example, the package of a class). The location
     * string is used by some renderers and usually displayed as grayed text next to the item name.
     *
     * @return the location description, or null if none is applicable.
     */
    @NotNull
    @Override
    public String getLocationString() {
        return parent.getLocatedPresentableText();
    }

    /**
     * Returns the icon representing the object.
     *
     * @param unused Used to mean if open/close icons for tree renderer. No longer in use. The parameter is only there for API compatibility reason.
     */
    @NotNull
    @Override
    public Icon getIcon(boolean unused) {
        RowIcon rowIcon = new RowIcon(2);
        rowIcon.setIcon(ElixirIcons.MACRO, 0);
        rowIcon.setIcon(PlatformIcons.PUBLIC_ICON, 1);

        return rowIcon;
    }
}
