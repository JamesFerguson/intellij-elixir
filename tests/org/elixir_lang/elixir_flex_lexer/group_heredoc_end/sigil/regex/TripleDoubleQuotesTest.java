package org.elixir_lang.elixir_flex_lexer.group_heredoc_end.sigil.regex;

import com.intellij.psi.tree.IElementType;
import org.elixir_lang.ElixirFlexLexer;
import org.elixir_lang.psi.ElixirTypes;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by luke.imhoff on 9/8/14.
 */
@RunWith(Parameterized.class)
public class TripleDoubleQuotesTest extends org.elixir_lang.elixir_flex_lexer.group_heredoc_end.sigil.regex.Test {
    /*
     * Constructors
     */

    public TripleDoubleQuotesTest(CharSequence charSequence, IElementType tokenType, int lexicalState, boolean consumeAll) {
        super(charSequence, tokenType, lexicalState, consumeAll);
    }

    /*
     * Methods
     */

    @Parameterized.Parameters(
            name = "\"{0}\" parses as {1} token and advances to state {2}"
    )
    public static Collection<Object[]> generateData() {
        return Arrays.asList(
                new Object[][]{
                        {"'''", ElixirTypes.FRAGMENT, ElixirFlexLexer.GROUP_HEREDOC_LINE_BODY, true},
                        {"\"\"\"", ElixirTypes.HEREDOC_TERMINATOR, ElixirFlexLexer.ADDITION_OR_SUBTRACTION_MAYBE, true}
                }
        );
    }

    protected String promoter() {
        return "\"\"\"";
    }
}
