package org.mygovscot.decommissioned.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

public class PathOrURLValidator implements ConstraintValidator<PathOrURL, String> {

    private static final Logger LOG = LoggerFactory.getLogger(PathOrURLValidator.class);

    @Override
    public void initialize(PathOrURL path) {
        // no initialisation needed
    }

    @Override
    public boolean isValid(String pathOrURL, ConstraintValidatorContext context) {

        if (StringUtils.isEmpty(pathOrURL)) {
            return false;
        }

        try {
            URI validUrl = new URI(pathOrURL);
            if (validUrl.getHost() != null) {
                // fully qualified url - let this pass.  client validation should ensure that the host is whitelisted.
                return true;
            }
            return pathOrURL.startsWith("/");
        } catch (URISyntaxException e) {
            LOG.debug("Invalid path {}", pathOrURL, e);
            return false;
        }
    }

}