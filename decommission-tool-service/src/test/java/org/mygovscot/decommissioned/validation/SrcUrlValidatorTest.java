package org.mygovscot.decommissioned.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.mygovscot.decommissioned.model.Page;

public class SrcUrlValidatorTest {

    @Test
    public void testIsValid() {
        SrcUrlValidator validator = new SrcUrlValidator();
        assertTrue(validator.isValid(page(Page.MatchType.EXACT, "/"), null));
        assertTrue(validator.isValid(page(Page.MatchType.EXACT, "/path/with/filename.html"), null));
        assertTrue(validator.isValid(page(Page.MatchType.PCRE_REGEXP, "[a..z]"), null));
        assertTrue(validator.isValid(page(Page.MatchType.PCRE_REGEXP, "/*."), null));

        assertFalse(validator.isValid(page(Page.MatchType.PCRE_REGEXP, "["), null));
        assertFalse(validator.isValid(page(Page.MatchType.PCRE_REGEXP, "["), null));
    }

    private Page page(Page.MatchType type, String srcUrl) {
        Page p = new Page();
        p.setType(type);
        p.setSrcUrl(srcUrl);
        return p;
    }
}