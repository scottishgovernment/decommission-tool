package org.mygovscot.decommissioned.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PathValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        PathValidator pathValidator = new PathValidator();
        assertTrue(pathValidator.isValid("/", null));
        assertTrue(pathValidator.isValid("/path", null));
        assertTrue(pathValidator.isValid("/path/path1", null));
        assertTrue(pathValidator.isValid("/path/path1/page.html", null));
        assertTrue(pathValidator.isValid("/path/path1/page.html#anchor", null));
        assertTrue(pathValidator.isValid("/path/path1/page.html?param=value", null));

        assertFalse(pathValidator.isValid("", null));
        assertFalse(pathValidator.isValid("/space /path1", null));
        assertFalse(pathValidator.isValid("page.html", null));
        assertFalse(pathValidator.isValid("?parameter=value", null));
    }

}