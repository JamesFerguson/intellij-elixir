package org.elixir_lang.parser_definition;

import org.elixir_lang.sdk.elixir.Release;

/**
 * atom is invalid to the right of `.`, so unlike in {@link MatchedDotOperationParsingTestCase}, this tests only when
 * atom is left of `.` and the right operand varies based on the test name.
 */
public class MatchedQualifiedMultipleAliasesParsingTestCase extends ParsingTestCase {
    /*
     * matchedExpression
     */

    public void testMatchedCaptureNonNumericOperation() {
        assertParsedAndQuotedCorrectlyInOneThree(false);
    }

    public void testMatchedInMatchOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedWhenNoParenthesesKeywordsOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedWhenOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedTypeOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedPipeOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedMatchOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedOrOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedAndOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedComparisonOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedRelationalOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedArrowOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedInOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedThreeOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedTwoOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedAdditionOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedMultiplicationOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedUnaryNonNumericOperation() {
        assertParsedAndQuotedCorrectlyInOneThree(false);
    }

    public void testMatchedDotCallOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedQualifiedAliasOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedQualifiedCallOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testMatchedAtNonNumericOperation() {
        assertParsedAndQuotedCorrectlyInOneThree(false);
    }

    public void testMatchedUnqualifiedCallOperation() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testVariable() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    /*
     * accessExpression
     */

    public void testAtNumericOperation() {
        assertParsedAndQuotedCorrectlyInOneThree(false);
    }

    public void testCaptureNumericOperation() {
        assertParsedAndQuotedCorrectlyInOneThree(false);
    }

    public void testUnaryNumericOperation() {
        assertParsedAndQuotedCorrectlyInOneThree(false);
    }

    public void testList() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testSigil() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testAtomKeyword() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testAlias() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    /*
     * numeric
     */

    public void testCharToken() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testBinaryWholeNumber() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testHexadecimalWholeNumber() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testOctalWholeNumber() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testUnknownBaseWholeNumber() {
        assertParsedAndQuotedAroundError();
    }

    public void testDecimalFloat() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testDecimalWholeNumber() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testStringLine() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testStringHeredoc() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testCharListLine() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    public void testCharListHeredoc() {
        assertParsedAndQuotedCorrectlyInOneThree();
    }

    /*
     * Protected Instance Methods
     */

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "/matched_qualified_multiple_aliases_parsing_test_case";
    }

    private void assertParsedAndQuotedCorrectlyInOneThree() {
        assertParsedAndQuotedCorrectlyInOneThree(true);
    }

    private void assertParsedAndQuotedCorrectlyInOneThree(boolean checkResult) {
        assertParsedAndQuotedCorrectly(checkResult);
    }

    /*
     * Private Instance Methods
     */

    private Release elixirSdkRelease() {
        String elixirVersion = elixirVersion();
        Release elixirSdkRelease = Release.fromString((elixirVersion));

        assertNotNull(
                "ELIXIR_VERSION (" + elixirVersion  + ") could not be parsed into an ElixirSdkRelease",
                elixirSdkRelease
        );

        return elixirSdkRelease;
    }

    private String elixirVersion() {
        String elixirVersion = System.getenv("ELIXIR_VERSION");

        assertNotNull("ELIXIR_VERSION is not set", elixirVersion);

        return elixirVersion;
    }
}
