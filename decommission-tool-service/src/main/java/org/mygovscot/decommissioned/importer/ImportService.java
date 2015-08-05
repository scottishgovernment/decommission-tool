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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

            // parse the csv and remove duplicates
            CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), CSVFormat.newFormat(','));
            List<CSVRecord> records = parser.getRecords().stream()
                    .distinct().collect(Collectors.toList());

            // get a list of all src urls mentioned
            List<String> srcUrls = records.stream().map(t -> t.get(0)).collect(Collectors.toList());

            // fetch all pages that are mentioned in one gulp and map them by their src url
            Map<String, Page> seenPagesBySrcUrl = pageRepository.findBySiteIdAndSrcUrlIn(site.getId(), srcUrls)
                    .stream()
                    .collect(Collectors.toMap(p -> cleanSourceUrl(site, p.getSrcUrl()), Function.identity()));

            // group the records in the csv by their srcUrl
            Map<String, List<CSVRecord>> srcToRecord = records.stream().collect(Collectors.groupingBy(t -> t.get(0)));

            // process each record
            for (Map.Entry<String, List<CSVRecord>> entry : srcToRecord.entrySet()) {

                // take the last entry for each srcUrl
                CSVRecord lastRecord = entry.getValue().get(entry.getValue().size() - 1);

                if (processRecord(site, lastRecord, seenPagesBySrcUrl)) {
                    added++;
                }
            }

            return new ImportResult(added, records.size() - added);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to read csv", ioe);
        }
    }

    private boolean processRecord(Site site, CSVRecord record, Map<String, Page> seenPagesBySrcUrl) {

        if (record.size() < 1 || record.size() > 2) {
            throw new IllegalArgumentException(
                    String.format("Invalid record (line %d) expected either 1 or 2 fields, got %d",
                            record.getRecordNumber(), record.size()));
        }

        // trim and tidy the urls
        String srcUrl = srcUrl(record, site);

        // default the target url to "/" and only override it if it is not empty
        String targetUrl = targetUrl(record);

        // see if this is already in the db or not
        Page page = seenPagesBySrcUrl.get(srcUrl);

        if (page == null) {
            page = new Page();
        } else {
            // ignore locked items
            if (page.isLocked()) {
                LOG.info("Src url already mapped and locked: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
                return false;
            }

            // ignore if the imported item is the same as the one already in the db
            if (targetUrl.equals(page.getTargetUrl())) {
                LOG.info("Src url already mapped and unchanged: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
                return false;
            }
        }
        // if the page was not present then create a new one
        page.setSite(site);
        page.setSrcUrl(srcUrl);
        page.setTargetUrl(targetUrl);
        LOG.debug("page: {} -> {}", page.getSrcUrl(), page.getTargetUrl());

        pageRepository.save(page);

        return true;

    }

    private String srcUrl(CSVRecord record, Site site) {
        String srcUrl = record.get(0).trim();
        srcUrl = cleanSourceUrl(site, srcUrl);
        return srcUrl;
    }

    private String targetUrl(CSVRecord record) {
        String targetUrl = "/";
        if (record.size() == 2 && !record.get(1).trim().isEmpty()) {
            targetUrl = record.get(1).trim();
        }
        return targetUrl;
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
