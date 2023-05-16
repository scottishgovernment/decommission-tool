package org.mygovscot.decommissioned.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

public class SiteTest {

    @Test
    public void siteEquals() {
        Site site1 = new Site();
        site1.setId("id1");
        Site site2 = new Site();
        site2.setId("id2");
        EqualsVerifier.simple()
                .forClass(Site.class)
                .suppress(Warning.SURROGATE_OR_BUSINESS_KEY)
                .withPrefabValues(Site.class, site1, site2)
                .verify();
    }

}
