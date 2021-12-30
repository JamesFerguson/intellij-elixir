// This is a generated file. Not intended for manual editing.
package org.elixir_lang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ElixirLineBody extends Body {

  @NotNull
  List<ElixirEscapedCharacter> getEscapedCharacterList();

  @NotNull
  List<ElixirEscapedEOL> getEscapedEOLList();

  @NotNull
  List<ElixirEscapedLineTerminator> getEscapedLineTerminatorList();

  @NotNull
  List<ElixirInterpolation> getInterpolationList();

  @NotNull
  List<ElixirQuoteHexadecimalEscapeSequence> getQuoteHexadecimalEscapeSequenceList();

}
