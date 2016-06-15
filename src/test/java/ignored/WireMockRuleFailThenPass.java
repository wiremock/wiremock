package ignored;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WireMockRuleFailThenPass {

    // Generates a failure to illustrate a Rule bug whereby a failed test would cause BindExceptions on subsequent (otherwise passing) tests
    @Test
    public void fail() {
        assertTrue(false);
    }

    @Test
    public void succeed() {
        assertTrue(true);
    }

}
