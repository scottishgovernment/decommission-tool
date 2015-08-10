package org.mygovscot.decommissioned.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class PathOrURLValidatorTest {

    @Test
    public void testIsValid() throws Exception {
        PathOrURLValidator sut = new PathOrURLValidator();
        assertTrue(sut.isValid("/", null));
        assertTrue(sut.isValid("/path", null));
        assertTrue(sut.isValid("/path/path1", null));
        assertTrue(sut.isValid("/path/path1/page.html", null));
        assertTrue(sut.isValid("/path/path1/page.html#anchor", null));
        assertTrue(sut.isValid("/path/path1/page.html?param=value", null));
        assertTrue(sut.isValid("http://www.mygov.scot/", null));
        assertTrue(sut.isValid("https://www.mygov.scot/", null));

        assertFalse(sut.isValid("", null));
        assertFalse(sut.isValid("/space /path1", null));
        assertFalse(sut.isValid("page.html", null));
        assertFalse(sut.isValid("?parameter=value", null));
    }
}
