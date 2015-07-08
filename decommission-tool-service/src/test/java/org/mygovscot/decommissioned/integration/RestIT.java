package org.mygovscot.decommissioned.integration;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mygovscot.decommissioned.DecommissionToolApp;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.parsing.Parser.JSON;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DecommissionToolApp.class})
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class RestIT {

    private static final Logger LOG = LoggerFactory.getLogger(RestIT.class);

    @Value("${local.server.port}")
    private int port;

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    @Before
    public void setUp() {
        RestAssured.port = port;

        siteRepository.deleteAll();

        pageRepository.deleteAll();
    }

    @Test
    public void testCreateSite() {
        Site site = new Site();
        site.setHost("www.host1.com");
        site.setName("name1");

        // Check that the repository has no documents
        assertEquals(0, siteRepository.findAll().size());

        // Check that no documents are returned via rest api
        get("/redirects/sites").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("page.totalElements", equalTo(0));

        // Post site object as json to create a new document
        given().contentType(ContentType.JSON).body(site).post("/redirects/sites").then().statusCode(SC_CREATED);

        // Check that there is one and only one document in the database
        assertEquals(1, siteRepository.findAll().size());

        // Check that the list of documents returned through the rest api is the newly created entry.
        get("/redirects/sites").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("page.totalElements", equalTo(1));
    }

    @Test
    public void testCreateSiteFail() {
        Site site = new Site();
        site.setName("name1");

        // Can't save site because it does not have a host
        given().contentType(ContentType.JSON).body(site).post("/redirects/sites").then().statusCode(SC_BAD_REQUEST);

        // Save one site
        site.setHost("www.host1.com");
        given().contentType(ContentType.JSON).body(site).post("/redirects/sites").then().statusCode(SC_CREATED);

        // Check that there is one and only one document in the database
        assertEquals(1, siteRepository.findAll().size());

        // Post again but check duplicate host constraint stops it from saving
//        given().contentType(ContentType.JSON).body(site).post("/sites").then().statusCode(HttpStatus.SC_BAD_REQUEST);

        // Still only 1 site in the database
        assertEquals(1, siteRepository.findAll().size());
    }

    @Test
    public void testCreateSiteInvalidHostname() {
        Site site = new Site();
        site.setName("name1");
        site.setHost("#www.host1.com///");

        given().contentType(ContentType.JSON).body(site).post("/redirects/sites").then().statusCode(SC_BAD_REQUEST);

        assertEquals(0, siteRepository.findAll().size());
    }

    @Test
    public void testCreatePage() {
        Site site = new Site();
        site.setHost("www.host1.com");
        site.setName("name1");

        site = siteRepository.save(site);

        Page page = new Page();
        page.setSite(site);
        page.setSrcUrl("/mypage");
        page.setTargetUrl("/mypage.html");

        // Check that the repository has no documents
        assertEquals(0, pageRepository.findAll().size());

        // Check that no documents are returned via rest api
        get("/redirects/pages").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("page.totalElements", equalTo(0));

        // Post page object as json to create a new document
        given().contentType(ContentType.JSON).body(toHAL(page)).post("/redirects/pages").then().statusCode(SC_CREATED);

        // Check that there is one and only one document in the database
        assertEquals(1, pageRepository.findAll().size());

        // Check that the list of documents returned through the rest api is the newly created entry.
        get("/redirects/pages").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("page.totalElements", equalTo(1));

        String siteUrl = get("/redirects/sites/" + site.getId()).then().extract().path("_links.self.href");
        get(siteUrl + "/pages").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("_embedded.pages[0].srcUrl", equalTo(page.getSrcUrl()));

    }

    @Test
    public void testCreateInvalidPage() {
        Site site = new Site();
        site.setHost("www.host1.com");
        site.setName("name1");

        site = siteRepository.save(site);

        Page page = new Page();
        page.setSite(site);
        page.setSrcUrl("/  mypage*%%$NOT A URL");
        page.setTargetUrl("/mypage.html");

        // Check that the repository has no documents
        assertEquals(0, pageRepository.findAll().size());

        // Check that no documents are returned via rest api
        get("/redirects/pages").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("page.totalElements", equalTo(0));

        // Post page object as json to create a new document
        given().contentType(ContentType.JSON).body(toHAL(page)).post("/redirects/pages").then().statusCode(SC_BAD_REQUEST);

        // Check that the document was not saved to db
        assertEquals(0, pageRepository.findAll().size());

        // Check that the list of documents returned through the rest api is still empty.
        get("/redirects/pages").then().statusCode(SC_OK).and().using().defaultParser(JSON).assertThat().body("page.totalElements", equalTo(0));

    }

    private String getSiteUrl(Site site) {
        return get("/redirects/sites/" + site.getId()).then().extract().path("_links.self.href");
    }

    private PathRepresentation toHAL(Page page) {
        String siteUrl = getSiteUrl(page.getSite());
        LOG.debug("Base sites url = {}", siteUrl);
        return new PathRepresentation(page, siteUrl);
    }

    /**
     * HAL requires the site to be the URL of the site in REST but the
     * JSON generated by RestAssured has the site object embedded.  This
     * representation is used instead of the Page class to generate the
     * correct HAL JSON.
     */
    private class PathRepresentation {

        private String site;

        private String id;
        private String srcUrl;
        private String targetUrl;
        private String matchLevel;

        public PathRepresentation(Page page, String siteUrl) {
            this.site = siteUrl;

            BeanUtils.copyProperties(page, this, new String[]{"site"});
        }

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getSrcUrl() {
            return srcUrl;
        }

        public void setSrcUrl(String srcUrl) {
            this.srcUrl = srcUrl;
        }

        public String getTargetUrl() {
            return targetUrl;
        }

        public void setTargetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
        }

        public String getMatchLevel() {
            return matchLevel;
        }

        public void setMatchLevel(String matchLevel) {
            this.matchLevel = matchLevel;
        }
    }
}
