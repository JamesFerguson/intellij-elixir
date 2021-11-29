package org.elixir_lang.code_insight.completion.contributor;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CallDefinitionClauseTest extends LightPlatformCodeInsightFixtureTestCase {
    /*
     * Tests
     */

    public void testPrivateFunction() {
        myFixture.configureByFiles("private_function_usage.ex", "private_function_declaration.ex");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull("Completion lookup not shown", strings);
        assertCompletion("private_function1", strings);
        assertCompletion("private_function2", strings);
        assertEquals("Wrong number of completions", 2, strings.size());
    }

    public void testPublicFunction() {
        myFixture.configureByFiles("public_function_usage.ex", "public_function_declaration.ex");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull("Completion lookup not shown", strings);
        assertCompletion("public_function1", strings);
        assertCompletion("public_function2", strings);
        assertEquals("Wrong number of completions", 2, strings.size());
    }

    public void testPrivateMacroFunction() {
        myFixture.configureByFiles("private_macro_usage.ex", "private_macro_declaration.ex");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull("Completion lookup not shown", strings);
        assertCompletion("private_macro1", strings);
        assertCompletion("private_macro2", strings);
        assertEquals("Wrong number of completions", 2, strings.size());
    }

    public void testPublicMacroFunction() {
        myFixture.configureByFiles("public_macro_usage.ex", "public_macro_declaration.ex");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull("Completion lookup not shown", strings);
        assertCompletion("public_macro1", strings);
        assertCompletion("public_macro2", strings);
        assertEquals("Wrong number of completions", 2, strings.size());
    }

    public void testMixesWithNestedModules() {
        myFixture.configureByFiles("mixed_usage.ex", "mixed_declaration.ex");
        myFixture.complete(CompletionType.BASIC, 1);
        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull("Completion lookup not shown", strings);
        assertCompletion("public_macro", strings);
        assertCompletion("private_macro", strings);
        assertCompletion("public_function", strings);
        assertCompletion("private_function", strings);
        assertCompletion("Nested", strings);
        assertEquals("Wrong number of completions", 5, strings.size());
    }

    public void testIssue2122() {
        myFixture.configureByFiles("defdelegate.ex", "to.ex");
        myFixture.complete(CompletionType.BASIC, 1);

        List<String> strings = myFixture.getLookupElementStrings();
        assertNotNull("Completion lookup not shown", strings);
        assertCompletion("source", strings);
        assertCompletion("source_as", strings);
        assertCompletion("usage", strings);
        assertEquals("Wrong number of completions", 3, strings.size());

        LookupElement[] lookuoEelements = myFixture.getLookupElements();

        LookupElementPresentation lookupElementPresentation = new LookupElementPresentation();
        lookuoEelements[0].renderElement(lookupElementPresentation);
        assertEquals("source", lookupElementPresentation.getItemText());
        assertEquals(" (defdelegate.ex defmodule Defdelegate)", lookupElementPresentation.getTailText());

        lookupElementPresentation = new LookupElementPresentation();
        lookuoEelements[1].renderElement(lookupElementPresentation);
        assertEquals("source_as", lookupElementPresentation.getItemText());
        assertEquals(" (defdelegate.ex defmodule Defdelegate)", lookupElementPresentation.getTailText());

        lookupElementPresentation = new LookupElementPresentation();
        lookuoEelements[2].renderElement(lookupElementPresentation);
        assertEquals("usage", lookupElementPresentation.getItemText());
        assertEquals(" (defdelegate.ex defmodule Defdelegate)", lookupElementPresentation.getTailText());
    }

    /*
     * Protected Instance Methods
     */

    @Override
    protected String getTestDataPath() {
        return "testData/org/elixir_lang/code_insight/completion/contributor/call_definition_clause";
    }

    /*
     * Private Instance Methods
     */

    void assertCompletion(@NotNull String expectedCompletion, @NotNull List<String> actualCompletions) {
        assertTrue("Did not complete with \"" + expectedCompletion + "\"", actualCompletions.contains(expectedCompletion));
    }
}
