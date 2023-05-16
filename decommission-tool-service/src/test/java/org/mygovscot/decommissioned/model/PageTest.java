package org.mygovscot.decommissioned.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class PageTest {

    @Test
    public void pageEquals() {
        Page page1 = new Page();
        page1.setId("id1");
        Page page2 = new Page();
        page2.setId("id2");
        EqualsVerifier.simple()
                .forClass(Page.class)
                .suppress(Warning.SURROGATE_OR_BUSINESS_KEY)
                .withPrefabValues(Page.class, page1, page2)
                .verify();
    }

}
