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
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@Component
public class ImportService {

    private static final Logger LOG = LoggerFactory.getLogger(ImportService.class);

    @Inject
    private PageRepository pageRepository;

    @Inject
    private SiteRepository siteRepository;

    @Inject
    private WhitelistedHostRepository whitelistedHostRepository;

    public ImportResult importRedirects(String siteId, String csvSource) {
        InputStream is = new ByteArrayInputStream(csvSource.getBytes(StandardCharsets.UTF_8));
        Site site = siteRepository.getById(siteId);

        if (site == null) {
            throw new IllegalArgumentException("No such site "+siteId);
        }

        try {
            List<ImportRecordResult> results = new ArrayList<>();

            // parse the csv and remove duplicates
            CSVParser parser = new CSVParser(new InputStreamReader(is, StandardCharsets.UTF_8), CSVFormat.EXCEL);
            List<CSVRecord> records = parser.getRecords().stream().distinct(). collect(toList());

            // fetch all pages that are mentioned in one gulp and map them by their src url
            Map<String, Page> seenPagesBySrcUrl = pageRepository.findBySiteId(site.getId())
                    .stream()
                    .collect(toMap(p -> p.getSrcUrl(), Function.identity()));
            Map<String, Page> seenPagesBySrcUrlThisImport = new HashMap<>();

            results = records.stream()
                    .map(record -> {
                        if (acceptableRecordSize(record)) {
                            return processRecord(site, record, seenPagesBySrcUrl, seenPagesBySrcUrlThisImport);
                        }
                        return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Wrong Number of Fields", record.getRecordNumber());
                    }).collect(toList());

            return new ImportResult(results);
        } catch (IOException ioe) {
            throw new IllegalArgumentException("Unable to read csv", ioe);
        }
    }

    private boolean acceptableRecordSize(CSVRecord record) {
        return record.size() >= 1 && record.size() <= 4;
    }

    private ImportRecordResult processRecord(Site site, CSVRecord record, Map<String, Page> seenPagesBySrcUrl, Map<String, Page> seenPagesBySrcUrlThisRun) {

        // trim and tidy the urls
        String srcUrl = "";
        try {
            srcUrl = srcUrl(record, site);
        } catch (Exception e) {
            LOG.info("Invalid src URI", e);
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid srcUrl", record.getRecordNumber());
        }

        // have we seen it in this csv file already?
        if (seenPagesBySrcUrlThisRun.get(srcUrl) != null) {
            return new ImportRecordResult(ImportRecordResult.Type.DUPLICATE,
                    "srcUrl appears more than once in this file", record.getRecordNumber());
        }

        // default the target url to "/" and only override it if it is not empty
        String targetUrl = "/";
        try {
            targetUrl = targetUrl(record, site);
        } catch (Exception e) {
            LOG.info("Invalid target URI", e);
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid targetUrl", record.getRecordNumber());
        }

        Page.RedirectType redirectType;
        try {
            redirectType = redirectType(record);
        } catch(IllegalArgumentException e) {
            LOG.info("Invalid redirect type", e);
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid redirect type", record.getRecordNumber());
        }
        
        return process(srcUrl, targetUrl, site, record, seenPagesBySrcUrl, seenPagesBySrcUrlThisRun, redirectType);

    }

    private ImportRecordResult process(String srcUrl, String targetUrl, Site site, CSVRecord record,
                                       Map<String, Page> seenPagesBySrcUrl, Map<String, Page> seenPagesBySrcUrlThisRun,
                                       Page.RedirectType redirectType) {
        Page.MatchType matchType;
        try {
            matchType = matchType(record);
        } catch(IllegalArgumentException e) {
            LOG.info("Invalid match type", e);
            return new ImportRecordResult(ImportRecordResult.Type.ERROR, "Invalid match type", record.getRecordNumber());
        }
        
        // see if this is already in the db or not
        Page page = seenPagesBySrcUrl.get(srcUrl);
        if (page != null) {
            ImportRecordResult unchangedResult = detectNoChangeOrLocked(targetUrl, redirectType, matchType, record, page);
            seenPagesBySrcUrlThisRun.put(srcUrl, page);
            if (unchangedResult != null) {
                return unchangedResult;
            }
        } else {
            page = new Page();
        }

        // if the page was not present then create a new one
        page.setSite(site);
        page.setSrcUrl(srcUrl);
        page.setTargetUrl(targetUrl);
        page.setType(matchType);
        page.setRedirectType(redirectType);
        LOG.debug("page: {} -> {} {}", page.getSrcUrl(), page.getTargetUrl(), redirectType.toString());

        seenPagesBySrcUrlThisRun.put(srcUrl, page);

        pageRepository.save(page);

        return new ImportRecordResult(ImportRecordResult.Type.SUCCESS, "", record.getRecordNumber());
    }

    private ImportRecordResult detectNoChangeOrLocked(String targetUrl, Page.RedirectType targetRedirectType, Page.MatchType targetMatchType, CSVRecord record, Page page) {
        // ignore locked items
        if (page.isLocked()) {
            LOG.info("Src url already mapped and locked: {} (mapped to {})", page.getSrcUrl(), page.getTargetUrl());
            return new ImportRecordResult(ImportRecordResult.Type.NOCHANGE, "Locked", record.getRecordNumber());
        }

        // ignore if the imported item is the same as the one already in the db
        if (targetUrl.equals(page.getTargetUrl()) &&
                targetRedirectType.equals(page.getRedirectType()) &&
                targetMatchType.equals(page.getType())) {
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
                .stream().map(t -> t.getHost()).collect(toSet());
        acceptableHosts.add(site.getHost());

        if (!acceptableHosts.contains(uri.getHost())) {
            throw new IllegalArgumentException("Host not in whitelist");
        }

        return raw;
    }

    private Set<String> getHosts(Site site) {
        return Arrays.stream(site.getHost().split(" ")).collect(toSet());
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

        // we want to strip paramaters but retain fragment identifier
        String path = srcUrl;

        if (uri.getHost() != null) {
            path = StringUtils.substringAfter(srcUrl, uri.getHost());
        }
        if (!StringUtils.isEmpty(uri.getQuery())) {
            path = StringUtils.substringBefore(path, "?");

            if (!StringUtils.isEmpty(uri.getFragment())) {
                path = path + '#'+ uri.getFragment();
            }
        }

        // the path should always start with a /
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path does not start with /");
        }
        return path;
    }

    private Page.MatchType matchType(CSVRecord record) {
        
        if(record.size() < 4) {
            return Page.MatchType.EXACT;
        }
        
        String raw = record.get(3).trim().toUpperCase();
        
        if("REGEXP".equals(raw)) {
            return Page.MatchType.PCRE_REGEXP;
        } else {
            //Default to exact
            return Page.MatchType.EXACT;
        }
    }
    
    private Page.RedirectType redirectType(CSVRecord record) {

        //If there is no redirect type specified default to a PERMANENT redirect.
        if(record.size() < 3) {
            return Page.RedirectType.PERMANENT;
        }

        String raw = record.get(2).trim().toUpperCase();

        // "TEMPORARY" should be interpreted as REDIRECT
        if("TEMPORARY".equals(raw)) {
            return Page.RedirectType.REDIRECT;
        } else {
            //Default to PERMANENT if not recognised
            return Page.RedirectType.PERMANENT;
        }
    }
}
