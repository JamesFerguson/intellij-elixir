package org.elixir_lang.parser_definition;

/**
 * sigil is invalid to the right of `.`, so unlike in {@link MatchedDotOperationParsingTestCase}, this tests only when
 * sigil is left of `.` and the right operand varies based on the test name.
 */
public class SigilDotOperationParsingTestCase extends ParsingTestCase {
    /*
     * matchedDotOperand
     */

    public void testAtNonNumericOperation() {
        assertParsedAndQuotedCorrectly();
    }

    public void testMatchedCallOperation() {
        assertParsedAndQuotedCorrectly();
    }

    public void testVariable() {
        assertParsedAndQuotedCorrectly();
    }

    /*
     * accessExpression
     */

    public void testAtNumericOperation() {
        assertParsedAndQuotedCorrectly();
    }

    public void testCaptureNumericOperation() {
        assertParsedAndQuotedCorrectly();
    }

    public void testUnaryNumericOperation() {
        assertParsedAndQuotedCorrectly();
    }

    public void testList() {
        assertParsedWithErrors();
    }

    public void testSigil() {
        assertParsedWithErrors();
    }

    public void testAtomKeyword() {
        assertParsedAndQuotedCorrectly();
    }

    public void testAtom() {
        assertParsedWithErrors();
    }

    public void testAlias() {
        assertParsedAndQuotedCorrectly();
    }

    /*
     * numeric
     */

    public void testCharToken() {
        assertParsedWithErrors();
    }

    public void testBinaryWholeNumber() {
        assertParsedWithErrors();
    }

    public void testHexadecimalWholeNumber() {
        assertParsedWithErrors();
    }

    public void testOctalWholeNumber() {
        assertParsedWithErrors();
    }

    public void testUnknownBaseWholeNumber() {
        assertParsedWithErrors();
    }

    public void testDecimalFloat() {
        assertParsedWithErrors();
    }

    public void testDecimalWholeNumber() {
        assertParsedWithErrors();
    }

    public void testStringLine() {
        assertParsedAndQuotedCorrectly();
    }

    public void testStringHeredoc() {
        assertParsedWithErrors();
    }

    public void testCharListLine() {
        assertParsedAndQuotedCorrectly();
    }

    public void testCharListHeredoc() {
        assertParsedWithErrors();
    }

    @Override
    protected String getTestDataPath() {
        return super.getTestDataPath() + "/sigil_dot_operation_parsing_test_case";
    }
}
