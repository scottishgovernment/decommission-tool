package org.mygovscot.decommissioned.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@SpringBootTest(
        classes = DecommissionToolApp.class,
        properties = "server.port=0"
)
public class RestIT {

    private static final Logger LOG = LoggerFactory.getLogger(RestIT.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    SiteRepository siteRepository;

    @Autowired
    PageRepository pageRepository;

    @Autowired
    WebApplicationContext context;

    MockMvc mvc;

    @Before
    public void setUp() {
        mvc = webAppContextSetup(context).build();
        siteRepository.deleteAll();
        pageRepository.deleteAll();
    }

    @Test
    public void testCreateSite() throws Exception {
        Site site = new Site();
        site.setHost("www.host1.com");
        site.setName("name1");

        // Check that the repository has no documents
        assertEquals(0, siteRepository.findAll().size());

        // Check that no documents are returned via rest api
        mvc.perform(get("/redirects/sites").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page.totalElements").value(0));

        mvc.perform(post("/redirects/sites")
                .content(mapper.writeValueAsString(site)))
                .andExpect(status().isCreated());

        mvc.perform(get("/redirects/sites").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page.totalElements").value(1));
        assertEquals(1, siteRepository.findAll().size());
    }

    @Test
    public void testCreateSiteFail() throws Exception {
        Site site = new Site();
        site.setName("name1");

        // Can't save site because it does not have a host
        mvc.perform(post("/redirects/sites")
                .content(mapper.writeValueAsString(site)))
                .andExpect(status().isBadRequest());

        // Save one site
        site.setHost("www.host1.com");
        mvc.perform(post("/redirects/sites")
                .content(mapper.writeValueAsString(site)))
                .andExpect(status().isCreated());

        // Check that there is one and only one document in the database
        assertEquals(1, siteRepository.findAll().size());

        // Post again but check duplicate host constraint stops it from saving
        mvc.perform(post("/redirects/sites")
                .content(mapper.writeValueAsString(site)))
                .andExpect(status().is4xxClientError());

        // Still only 1 site in the database
        assertEquals(1, siteRepository.findAll().size());
    }

    @Test
    public void testCreateSiteInvalidHostname() throws Exception {
        Site site = new Site();
        site.setName("name1");
        site.setHost("#www.host1.com///");

        // Can't save site because it does not have a host
        mvc.perform(post("/redirects/sites")
                .content(mapper.writeValueAsString(site)))
                .andExpect(status().isBadRequest());

        assertEquals(0, siteRepository.findAll().size());
    }

    @Test
    public void testCreatePage() throws Exception {
        Site site = new Site();
        site.setHost("www.host1.com");
        site.setName("name1");

        site = siteRepository.save(site);

        Page page = new Page();
        page.setSite(site);
        page.setSrcUrl("/mypage");
        page.setTargetUrl("/mypage.html");
        page.setType(Page.MatchType.EXACT);

        // Check that the repository has no documents
        assertEquals(0, pageRepository.findAll().size());

        // Check that no documents are returned via rest api
        mvc.perform(get("/redirects/pages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page.totalElements").value(0));

        // Post page object as json to create a new document
        mvc.perform(post("/redirects/pages").accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(toHAL(page))))
                .andExpect(status().isCreated());

        // Check that there is one and only one document in the database
        assertEquals(1, pageRepository.findAll().size());

        // Check that the list of documents returned through the rest api is the newly created entry.
        mvc.perform(get("/redirects/pages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page.totalElements").value(1));

        String siteUrl = getSiteUrl(site);
        mvc.perform(get(siteUrl + "/pages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("_embedded.pages[0].srcUrl").value(page.getSrcUrl()));
    }

    @Test
    public void testCreateInvalidPage() throws Exception {
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
        mvc.perform(get("/redirects/pages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page.totalElements").value(0));

        // Post page object as json to create a new document
        mvc.perform(post("/redirects/pages").accept(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(toHAL(page))))
                .andExpect(status().isBadRequest());

        // Check that the document was not saved to db
        assertEquals(0, pageRepository.findAll().size());

        // Check that the list of documents returned through the rest api is still empty.
        mvc.perform(get("/redirects/pages").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("page.totalElements").value(0));
    }

    private String getSiteUrl(Site site) throws Exception {
        String json = mvc.perform(get("/redirects/sites/" + site.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.parse(json).read("_links.self.href");
    }

    private PathRepresentation toHAL(Page page) throws Exception {
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
        private String type;

        public PathRepresentation(Page page, String siteUrl) {
            this.site = siteUrl;
            this.type = Page.MatchType.EXACT.toString();
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
