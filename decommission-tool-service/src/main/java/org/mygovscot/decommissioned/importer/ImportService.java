package org.mygovscot.decommissioned.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@Component
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Transactional
    public ImportResult importRedirects(String siteId, String csvSource) {
        InputStream is = new ByteArrayInputStream(csvSource.getBytes(StandardCharsets.UTF_8));
        Site site = siteRepository.findOne(siteId);

        if (site == null) {
            throw new IllegalArgumentException("No such site "+siteId);
        }

        try {
            int added = 0;
            int skipped = 0;

            CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), CSVFormat.newFormat(','));
            for (CSVRecord record : parser.getRecords()) {
                if (processRecord(site, record)) {
                    added++;
                } else {
                    skipped++;
                }
            }
            return new ImportResult(added, skipped);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to read csv", ioe);
        }
    }

    private boolean processRecord(Site site, CSVRecord record) {

        if (record.size() != 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid record (line %d) expected 2 fields, got %d",
                            record.getRecordNumber(), record.size()));
        }

        String srcUrl = record.get(0).trim();
        String targetUrl = record.get(1).trim();

        // if the target url is empty then default to the home page
        if (targetUrl.isEmpty()) {
            targetUrl = "/";
        }
        srcUrl = cleanSourceUrl(site, srcUrl);

        // check if there are any pages with this srcUrl
        Page page = pageRepository.findOneBySiteIdAndSrcUrl(site.getId(), srcUrl);

        if (page != null) {
            LOG.info("Src url already mapped: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
            return false;
        }

        page = new Page();
        page.setSite(site);
        page.setSrcUrl(srcUrl);
        page.setTargetUrl(targetUrl);
        LOG.info("Creating page: {} -> {}", page.getSrcUrl(), page.getTargetUrl());
        pageRepository.save(page);
        return true;

    }

    private String cleanSourceUrl(Site site, String srcUrl) {
        try {
            URI uri= new URI(srcUrl);
            if (uri.getHost() != null) {
                if (!uri.getHost().equals(site.getHost())) {
                    throw new IllegalArgumentException(
                            String.format("Host does not match site: >%s< != >%s<", uri.getHost(), site.getHost()));
                }
                return uri.getPath();
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid srcUrl", e);
        }

        return srcUrl;
    }
}
