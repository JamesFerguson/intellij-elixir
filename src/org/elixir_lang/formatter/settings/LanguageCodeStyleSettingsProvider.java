package org.elixir_lang.formatter.settings;

import com.intellij.application.options.IndentOptionsEditor;
import com.intellij.application.options.SmartIndentOptionsEditor;
import com.intellij.lang.Language;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.elixir_lang.ElixirLanguage;
import org.elixir_lang.code_style.CodeStyleSettings;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable.SPACES_WITHIN;

public class LanguageCodeStyleSettingsProvider extends com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider {
    private static final String INDENT_CODE_SAMPLE =
            "defmodule Foo do\n" +
            "  @spec foo :: one |\n" +
            "               two |\n" +
            "               three\n" +
            "  def foo do\n" +
            "    receive do\n" +
            "      {:ok, value} -> value\n" +
            "      {:error, reason} -> reason\n" +
            "    end\n" +
            "  end\n" +
            "end";
    private static final String SPACE_AFTER_OPERATORS = "After Operators";
    private static final String SPACING_CODE_SAMPLE =
            "# Addition Operators\n" +
            "1 + 1\n" +
            "1 - 1\n" +
            "\n" +
            "# And Operators\n" +
            "& &1 && &2\n" +
            "true && true\n" +
            "& &1 &&& &2\n" +
            "0b11 &&& 0b01\n" +
            "\n" +
            "# Arrow Operators\n" +
            "a <~ b\n" +
            "a\n" +
            "|> b()\n" +
            "a ~> b\n" +
            "a <<< b\n" +
            "a <<~ b\n" +
            "a <|> b\n" +
            "a <~> b\n" +
            "a >>> b\n" +
            "a ~>> b\n" +
            "\n" +
            "# Association Operator\n" +
            "%{key => value}\n" +
            "%{map | key => updated_value}\n" +
            "\n" +
            "# Bit Strings and Binaries\n" +
            "<<one>>\n" +
            "\n" +
            "# Brackets\n" +
            "map[:key]\n" +
            "[head | tail]\n" +
            "\n" +
            "# Capture Operators\n" +
            "& &1 + &2\n" +
            "&Kernel.||/2\n" +
            "&foo/0\n" +
            "\n" +
            "# Comma\n" +
            "one(two: 2,)\n" +
            "one(two, three)\n" +
            "one two, three\n" +
            "one two, three: 3\n" +
            "[one: 1,]\n" +
            "[one: 1, two: 2]\n" +
            "%{one: 1,}\n" +
            "%{one: 1, two: 2}\n" +
            "%{one => 1,}\n" +
            "%{one => 1, two => 2}\n" +
            "\n" +
            "# Comparison Operators\n" +
            "1 != 2\n" +
            "1 == 1.0\n" +
            "\"abcd\" =~ ~r/c(d)/\n" +
            "1 !== 1.0\n" +
            "1.00 === 1.0\n" +
            "\n" +
            "# Curly Braces\n" +
            "{one}\n" +
            "%{key: :value}\n" +
            "%{key => value}\n" +
            "%One{field: value}\n" +
            "%One{field => value}\n" +
            "\n" +
            "# In Match Operators\n" +
            "param \\\\ default\n" +
            "{:ok, value} <- elements\n" +
            "\n" +
            "# Match Operator\n" +
            "a = 1\n" +
            "\n" +
            "# Multiplication Operators\n" +
            "0 * 1\n" +
            "0 / 1\n" +
            "\n" +
            "# Or Operators\n" +
            "false || true\n" +
            "0b10 ||| 0b01\n" +
            "\n" +
            "# Parentheses\n" +
            "()\n" +
            "(one)\n" +
            "one()\n" +
            "one(two)\n" +
            "\n" +
            "# Pipe Operator\n" +
            "[head | tail]\n" +
            "%{map | a: 1}\n" +
            "\n" +
            "# Range Operator\n" +
            "first..last\n" +
            "\n" +
            "# Relationship Operators\n" +
            "3 < 4\n" +
            "3 <= 3\n" +
            "3 >= 3\n" +
            "4 > 3\n" +
            "\n" +
            "# Stab Operator\n" +
            "fn a -> a == true end\n" +
            "\n" +
            "# Type Operator\n" +
            "@type a :: term\n" +
            "@type a(b) :: b\n" +
            "\n" +
            "# Two Operators\n" +
            "first_list ++ second_list\n" +
            "full_list -- removal_list\n" +
            "\"Hello\" <> \", world!\"\n" +
            "\n" +
            "# Three Operator\n" +
            "register ^^^ register\n" +
            "\n" +
            "# Unary Operators\n" +
            "\n" +
            "## Numeric Operands\n" +
            "+1\n" +
            "-1\n" +
            "!1\n" +
            "^1\n" +
            "~~~1\n" +
            "\n" +
            "## Non-numeric Operands\n" +
            "+negative\n" +
            "-positive\n" +
            "!false\n" +
            "^pinned\n" +
            "~~~mask\n" +
            "\n" +
            "# Word Operators\n" +
            "true and true\n" +
            "false or true\n" +
            "\n" +
            "## Numeric Operands\n" +
            "not 1\n" +
            "\n" +
            "## Non-numeric Operands\n" +
            "not true\n" +
            "";
    private static final String WRAPPING_AND_BRACES_SETTINGS_CODE_SAMPLE =
            "defmodule Calcinator do\n" +
            "  def can(%__MODULE__{authorization_module: authorization_module, subject: subject}, action, target)\n" +
            "      when action in @actions and\n" +
            "           not is_nil(authorization_module) and\n" +
            "           (is_atom(target) or is_map(target) or is_list(target)) do\n" +
            "  end\n" +
            "\n" +
            "  defp authorized(%__MODULE__{authorization_module: authorization_module, subject: subject}, unfiltered, pagination)\n" +
            "       when is_list(unfiltered) and\n" +
            "            (is_nil(pagination) or is_map(pagination)) do\n" +
            "    {shallow_filtered, filtered_pagination} = case authorization_module.filter_can(unfiltered, subject, :show) do\n" +
            "      ^unfiltered ->\n" +
            "        {unfiltered, pagination}\n" +
            "      filtered_can ->\n" +
            "        {filtered_can, pagination}\n" +
            "    end\n" +
            "\n" +
            "    deep_filtered = authorization_module.filter_associations_can(shallow_filtered, subject, :show)\n" +
            "\n" +
            "    {deep_filtered, filtered_pagination}\n" +
            "  end\n" +
            "end\n" +
            "\n" +
            "defmodule Calcinator.Resources.Page do\n" +
            "  defstruct ~w(number size)a\n" +
            "\n" +
            "  @type t :: %__MODULE__{\n" +
            "               number: pos_integer,\n" +
            "               size: pos_integer,\n" +
            "             }\n" +
            "end\n";

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            customizeSpaceSettings(consumer);
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            customizeWrappingAndBracesSettings(consumer);
        }
    }

    private void customizeSpaceSettings(@NotNull CodeStyleSettingsCustomizable consumer) {
        consumer.showStandardOptions(
                // SPACE_BEFORE_PARENTHESES group

                /* SPACE_BEFORE_METHOD_PARENTHESES - Disabled because space between function name and call arguments
                   is invalid for parenthesized arguments */
                /* SPACE_BEFORE_METHOD_CALL_PARENTHESES - Disabled because space between function name and call
                   arguments is invalid for parenthesized arguments */
                /* SPACE_BEFORE_IF_PARENTHESES - Disabled because `if` is not special, so no specific setting for it
                   since it's not a keyword unlike in Java */
                /* SPACE_BEFORE_FOR_PARENTHESES - Disabled because `for` is a special form, but you wouldn't put
                   parentheses around the `<-` clauses */
                /* SPACE_BEFORE_WHILE_PARENTHESES - There is no `while` in Elixir */
                /* SPACE_BEFORE_SWITCH_PARENTHESES - `switch` in Java would be `case` in Elixir and no style
                   recommends parentheses for it */
                /* SPACE_BEFORE_CATCH_PARENTHESES - `catch` in Java would be `rescue` in Elixir, but `rescue` uses
                   `->` clauses, so its rules would apply */
                /* SPACE_BEFORE_SYNCHRONIZED_PARENTHESES - no `synchronized` in Elixir */
                /* SPACE_BEFORE_ANNOTATION_PARAMETER_LIST - module attribute calls follow normal call rules and a
                   space isn't allowed between the function name and parenthesized arguments */

                // SPACE_AROUND_OPERATORS group

                "SPACE_AROUND_ASSIGNMENT_OPERATORS",
                /* SPACE_AROUND_LOGICAL_OPERATORS - logical operators are mixed with bitwise operator in
                   AND_OPERATOR (and, &&, &&) and OR_OPERATOR (or, ||, |||), so there's no way to just space around
                   the logical version without inspecting the text value, but SpacingBuilder works at the ASTNode
                   level.  Additionally, all the operators can be overridden, so they don't HAVE to be logical even if Kernel defines them that way. */
                "SPACE_AROUND_EQUALITY_OPERATORS",
                "SPACE_AROUND_RELATIONAL_OPERATORS",
                /* SPACE_AROUND_BITWISE_OPERATORS - Bitwise operators are mixed into tokens used for other purposes,
                   so they can't be spaced independently */
                "SPACE_AROUND_ADDITIVE_OPERATORS",
                "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                /* SPACE_AROUND_SHIFT_OPERATORS - no dedicated shift operators in Elixir */
                "SPACE_AROUND_UNARY_OPERATOR",
                "SPACE_AROUND_LAMBDA_ARROW",

                // SPACES_WITHIN group

                "SPACE_WITHIN_BRACES",
                "SPACE_WITHIN_BRACKETS",
                "SPACE_WITHIN_PARENTHESES",

                // OTHER group

                "SPACE_BEFORE_COMMA",
                "SPACE_AFTER_COMMA"
        );

        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AFTER_CAPTURE_OPERATOR",
                "Capture operator (&)",
                SPACE_AFTER_OPERATORS);

        consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Match operator (=)");
        consumer.renameStandardOption(
                "SPACE_AROUND_EQUALITY_OPERATORS",
                "Comparison operators (!=, ==, =~, !==, ===)"
        );
        consumer.renameStandardOption("SPACE_AROUND_MULTIPLICATIVE_OPERATORS", "Multiplicative operators (*, /)");
        consumer.renameStandardOption("SPACE_AROUND_UNARY_OPERATOR", "Unary operators (!, ^, ~~~)");
        consumer.renameStandardOption("SPACE_AROUND_LAMBDA_ARROW", "Stab operator (->)");
        consumer.renameStandardOption("SPACE_WITHIN_BRACES", "Map (%{}), Struct (%Alias{}), and Tuple ({}) braces");
        consumer.renameStandardOption("SPACE_WITHIN_PARENTHESES", "Parentheses");

        String spacesAroundOperators = CodeStyleSettingsCustomizableOptions.getInstance().SPACES_AROUND_OPERATORS;

        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_AND_OPERATORS",
                "And operators (&&, &&&)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_ARROW_OPERATORS",
                "Arrow operators (<~, |>, ~>, <<<, <<~, <|>, <~>, >>>, ~>>)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_ASSOCIATION_OPERATOR",
                "Association operator (=>)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_IN_MATCH_OPERATORS",
                "In match operators (<-, \\\\)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_OR_OPERATORS",
                "Or operators (||, |||)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_PIPE_OPERATOR",
                "Pipe operator (|)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_RANGE_OPERATOR",
                "Range operator (..)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_THREE_OPERATOR",
                "Three operator (^^^)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_TWO_OPERATORS",
                "Two operator (++, --, <>)",
                spacesAroundOperators
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_AROUND_TYPE_OPERATOR",
                "Type operator (::)",
                spacesAroundOperators
        );

        consumer.showCustomOption(
                CodeStyleSettings.class,
                "SPACE_WITHIN_BITS",
                "Bit strings and binaries (<<>>)",
                SPACES_WITHIN
        );
    }

    private void customizeWrappingAndBracesSettings(@NotNull CodeStyleSettingsCustomizable consumer) {
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "ALIGN_BOOLEAN_OPERANDS",
                "Align operands of `and` and `or` operators",
                null
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "ALIGN_PIPE_OPERANDS",
                "Align operands of pipe operator (|)",
                null
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "ALIGN_UNMATCHED_CALL_DO_BLOCKS",
                "Align unmatched call `do` blocks to",
                null,
                new String[]{
                        CodeStyleSettings.UnmatchedCallDoBlockAlignment.CALL.name,
                        CodeStyleSettings.UnmatchedCallDoBlockAlignment.LINE.name,
                },
                new int[]{
                        CodeStyleSettings.UnmatchedCallDoBlockAlignment.CALL.value,
                        CodeStyleSettings.UnmatchedCallDoBlockAlignment.LINE.value
                }
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "ALIGN_TWO_OPERANDS",
                "Align operands of two operator (++, --, <>)",
                null
        );
        consumer.showCustomOption(
                CodeStyleSettings.class,
                "ALIGN_TYPE_DEFINITION_TO_RIGHT_OF_OPERATOR",
                "Align type definition to right of operator (::)",
                null
        );
    }

    @NotNull
    @Override
    public String getCodeSample(@NotNull SettingsType settingsType) {
        String codeSample = null;

        switch (settingsType) {
            case BLANK_LINES_SETTINGS:
                codeSample = "Blank Line Settings Code Sample";
                break;
            case SPACING_SETTINGS:
                codeSample = SPACING_CODE_SAMPLE;
                break;
            case WRAPPING_AND_BRACES_SETTINGS:
                codeSample = WRAPPING_AND_BRACES_SETTINGS_CODE_SAMPLE;
                break;
            case INDENT_SETTINGS:
                codeSample = INDENT_CODE_SAMPLE;
                break;
            case LANGUAGE_SPECIFIC:
                codeSample = "Language Specific Code Sample";
                break;
        }

        //noinspection ConstantConditions
        return codeSample;
    }

    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultCommonSettings = new CommonCodeStyleSettings(getLanguage());

        CommonCodeStyleSettings.IndentOptions indentOptions = defaultCommonSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 2;
        indentOptions.CONTINUATION_INDENT_SIZE = 2;
        indentOptions.TAB_SIZE = 2;

        return defaultCommonSettings;
    }

    @NotNull
    @Override
    public IndentOptionsEditor getIndentOptionsEditor() {
        return new SmartIndentOptionsEditor();
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return ElixirLanguage.INSTANCE;
    }
}
