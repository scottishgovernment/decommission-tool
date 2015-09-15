package org.mygovscot.decommissioned.validation;

import org.mygovscot.decommissioned.model.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SrcUrlValidator implements ConstraintValidator<SrcUrl, Page> {

    private static final Logger LOG = LoggerFactory.getLogger(PathOrURLValidator.class);

    @Override
    public void initialize(SrcUrl srcUrl) {
        // no initialisation needed
    }

    @Override
    public boolean isValid(Page page, ConstraintValidatorContext context) {

        String srcUrl = page.getSrcUrl();

        if (StringUtils.isEmpty(srcUrl)) {
            return false;
        }

        if (page.getType() == Page.MatchType.EXACT) {
            return isValidPath(srcUrl);
        }

        if (page.getType() == Page.MatchType.PCRE_REGEXP) {
            return isValidPerlRegexp(srcUrl);
        }

        throw new IllegalArgumentException("Unknown MatchType:"+page.getType());

    }

    private boolean isValidPath(String path) {
        try {
            URI validUrl = new URI(path);
            LOG.trace("Path is valid generic URI {}", validUrl);
            return path.startsWith("/");
        } catch (URISyntaxException e) {
            LOG.debug("Invalid path {}", path, e);
            return false;
        }
    }

    private boolean isValidPerlRegexp(String regexp) {
        try {
            Pattern.compile(regexp);
            return true;
        } catch (PatternSyntaxException e) {
            LOG.debug("Invalid PCRE: "+regexp, e);
            return false;
        }
    }
}