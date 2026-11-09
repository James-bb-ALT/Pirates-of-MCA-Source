import org.junit.jupiter.api.Test;

class LedgerSuiteTest {
    @Test void suppliedRegressions() { LedgerRegressions.main(new String[0]); }
    @Test void safetyRegressions() { LedgerSafetyRegressions.main(new String[0]); }
    @Test void originalAlphaSuite() throws Exception { tests_ledger.main(new String[0]); }
}
