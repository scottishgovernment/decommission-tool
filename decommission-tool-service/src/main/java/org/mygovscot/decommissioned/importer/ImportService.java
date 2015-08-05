package org.mygovscot.decommissioned.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private SiteRepository siteRepository;

    public ImportResult importRedirects(String siteId, String csvSource) {
        InputStream is = new ByteArrayInputStream(csvSource.getBytes(StandardCharsets.UTF_8));
        Site site = siteRepository.findOne(siteId);

        if (site == null) {
            throw new IllegalArgumentException("No such site "+siteId);
        }

        try {
            int added = 0;
            int skipped = 0;

            //TODO: remove dups using streams
            CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), CSVFormat.newFormat(','));
            for (CSVRecord record : parser.getRecords()) {
                LOG.info("line "+(added + skipped));
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

        if (record.size() < 1 || record.size() > 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid record (line %d) expected either 1 or 2 fields, got %d",
                            record.getRecordNumber(), record.size()));
        }

        String srcUrl = record.get(0).trim();
        // default the target url to "/" and only override it if it is not empty
        String targetUrl = "/";
        if (record.size() == 2 && !record.get(1).trim().isEmpty()) {
            targetUrl = record.get(1).trim();
        }

        srcUrl = cleanSourceUrl(site, srcUrl);

        // check if there are any pages with this srcUrl
        Page page = pageRepository.findOneBySiteIdAndSrcUrl(site.getId(), srcUrl);

        if (page != null && page.isLocked()) {
            LOG.info("Src url already mapped and locked: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
            return false;
        }

        if (page == null) {
            page = new Page();
        }
        page.setSite(site);
        page.setSrcUrl(srcUrl);
        page.setTargetUrl(targetUrl);
        LOG.info("page: {} -> {}", page.getSrcUrl(), page.getTargetUrl());
        pageRepository.save(page);
        return true;

    }

    private Set<String> getHosts(Site site) {
        return Arrays.stream(site.getHost().split(" ")).collect(Collectors.toSet());
    }

    private String cleanSourceUrl(Site site, String srcUrl) {
        try {
            URI uri= new URI(srcUrl);
            if (uri.getHost() != null && !getHosts(site).contains(uri.getHost())) {
                throw new IllegalArgumentException(
                        String.format("Host does not match site: >%s< != >%s<", uri.getHost(), site.getHost()));
            }

            if (uri.getHost() != null) {
                return StringUtils.substringAfter(srcUrl, uri.getHost());
            } else {
                return srcUrl;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid srcUrl", e);
        }
    }
}
