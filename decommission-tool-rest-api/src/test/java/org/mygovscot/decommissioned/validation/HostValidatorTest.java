package org.mygovscot.decommissioned.validation;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HostValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        HostValidator hostValidator = new HostValidator();
        hostValidator.initialize(null);

        assertTrue(hostValidator.isValid("server", null));
        assertTrue(hostValidator.isValid("server.com", null));
        assertTrue(hostValidator.isValid("www.server.com", null));
        assertTrue(hostValidator.isValid("1.2.3.4", null));

        assertFalse(hostValidator.isValid("http://www.server.com", null));
        assertFalse(hostValidator.isValid(":www.server.com", null));
        assertFalse(hostValidator.isValid("www.server.com/", null));
    }
}