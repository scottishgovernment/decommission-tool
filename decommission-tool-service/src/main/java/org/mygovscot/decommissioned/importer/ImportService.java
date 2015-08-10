package org.mygovscot.decommissioned.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.repository.PageRepository;
import org.mygovscot.decommissioned.repository.SiteRepository;
import org.mygovscot.decommissioned.repository.WhitelistedHostRepository;
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
import java.util.ArrayList;
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

    @Autowired
    private WhitelistedHostRepository whitelistedHostRepository;

    public ImportResult importRedirects(String siteId, String csvSource) {
        InputStream is = new ByteArrayInputStream(csvSource.getBytes(StandardCharsets.UTF_8));
        Site site = siteRepository.findOne(siteId);

        if (site == null) {
            throw new IllegalArgumentException("No such site "+siteId);
        }

        try {
            List<ImportRecordResult> results = new ArrayList<>();

            // parse the csv and remove duplicates
            CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), CSVFormat.newFormat(','));
            List<CSVRecord> records = parser.getRecords().stream().distinct(). collect(Collectors.toList());

            // get a list of all src urls mentioned
            List<String> srcUrls = records.stream().map(t -> t.get(0)).collect(Collectors.toList());

            // fetch all pages that are mentioned in one gulp and map them by their src url
            Map<String, Page> seenPagesBySrcUrl = pageRepository.findBySiteIdAndSrcUrlIn(site.getId(), srcUrls)
                    .stream()
                    .collect(Collectors.toMap(p -> p.getSrcUrl(), Function.identity()));

            // group the records in the csv by their srcUrl
            Map<String, List<CSVRecord>> srcToRecord = records.stream().collect(Collectors.groupingBy(t -> t.get(0)));

            // process each record
            for ( Map.Entry<String, List<CSVRecord>> entry : srcToRecord.entrySet()) {
                CSVRecord record = entry.getValue().get(entry.getValue().size() - 1);
                results.add(processRecord(site, record, seenPagesBySrcUrl));
            }

            return new ImportResult(results);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to read csv", ioe);
        }
    }

    private boolean acceptableRecordSize(CSVRecord record) {
        return record.size() == 1 || record.size() == 2;
    }

    private ImportRecordResult processRecord(Site site, CSVRecord record, Map<String, Page> seenPagesBySrcUrl) {

        if (!acceptableRecordSize(record)) {
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Wrong Number of Fields", record.getRecordNumber());
        }

        // trim and tidy the urls
        String srcUrl = "";
        try {
            srcUrl = srcUrl(record, site);
        } catch (URISyntaxException e) {
            LOG.info("Invalid src URI", e);
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid srcUrl", record.getRecordNumber());
        }

        // default the target url to "/" and only override it if it is not empty
        String targetUrl = "/";
        try {
            targetUrl = targetUrl(record, site);
        } catch (Exception e) {
            LOG.info("Invalid target URI", e);
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid targetUrl", record.getRecordNumber());
        }

        // see if this is already in the db or not
        Page page = seenPagesBySrcUrl.get(srcUrl);

        if (page == null) {
            page = new Page();
        } else {
            ImportRecordResult unchangedResult = detectNoChangeOrLocked(targetUrl, record, page);
            if (unchangedResult != null) {
                return unchangedResult;
            }
        }

        // if the page was not present then create a new one
        page.setSite(site);
        page.setSrcUrl(srcUrl);
        page.setTargetUrl(targetUrl);
        LOG.debug("page: {} -> {}", page.getSrcUrl(), page.getTargetUrl());

        pageRepository.save(page);

        return new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "OK", record.getRecordNumber());

    }

    private ImportRecordResult detectNoChangeOrLocked(String targetUrl, CSVRecord record, Page page) {
        // ignore locked items
        if (page.isLocked()) {
            LOG.info("Src url already mapped and locked: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
            return new ImportRecordResult(ImportRecordResult.Type.NOCHANGE, "Locked", record.getRecordNumber());
        }

        // ignore if the imported item is the same as the one already in the db
        if (targetUrl.equals(page.getTargetUrl())) {
            LOG.info("Src url already mapped and unchanged: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
            return new ImportRecordResult(ImportRecordResult.Type.NOCHANGE, "Unchanged", record.getRecordNumber());
        }

        return null;
    }
    private String targetUrl(CSVRecord record, Site site) throws URISyntaxException {

        // if there is only one entry in the record then default to /
        if (record.size() < 2) {
            return "/";
        }

        // if there is a second record but it is empty, default to /
        String raw = record.get(1).trim();
        if (raw.isEmpty()) {
            return "/";
        }

        // ensure it is a valid URI
        URI uri = new URI(raw);

        // if there is no host then just return the path
        if (uri.getHost() == null) {
            return uri.getPath();
        }

        // ensure host is acceptable (either the site host or a white listed one)
        Set<String> acceptableHosts = whitelistedHostRepository.findAll()
                .stream().map(t -> t.getHost()).collect(Collectors.toSet());
        acceptableHosts.add(site.getHost());

        if (!acceptableHosts.contains(uri.getHost())) {
            throw new IllegalArgumentException("Host not in whitelist");
        }

        return raw;
    }

    private Set<String> getHosts(Site site) {
        return Arrays.stream(site.getHost().split(" ")).collect(Collectors.toSet());
    }

    private String srcUrl(CSVRecord record, Site site) throws URISyntaxException {
        String srcUrl = record.get(0).trim();
        srcUrl = cleanSourceUrl(site, srcUrl);
        return srcUrl;
    }

    private String cleanSourceUrl(Site site, String srcUrl) throws URISyntaxException {
        URI uri= new URI(srcUrl);
        if (uri.getHost() != null && !getHosts(site).contains(uri.getHost())) {
            throw new IllegalArgumentException(
                    String.format("HostsList does not match site: >%s< != >%s<", uri.getHost(), site.getHost()));
        }

        if (uri.getHost() != null) {
            return StringUtils.substringAfter(srcUrl, uri.getHost());
        } else {
            return srcUrl;
        }
    }
}
