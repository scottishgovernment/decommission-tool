package org.mygovscot.decommissioned.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class HostValidatorTest {

    @Test
    public void testIsValid() {
        HostValidator sut = new HostValidator();
        sut.initialize(null);

        assertTrue(sut.isValid("server", null));
        assertTrue(sut.isValid("server.com", null));
        assertTrue(sut.isValid("www.server.com", null));
        assertTrue(sut.isValid("1.2.3.4", null));

        assertFalse(sut.isValid("server server2", null));
        assertFalse(sut.isValid("1.2.3.4 5.6.7.8 ", null));
        assertFalse(sut.isValid("http://www.server.com", null));
        assertFalse(sut.isValid(":www.server.com", null));
        assertFalse(sut.isValid("www.server.com/", null));
    }
}