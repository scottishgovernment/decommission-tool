package org.mygovscot.decommissioned.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URI;
import java.net.URISyntaxException;

public class PathValidator implements ConstraintValidator<Path, String> {

    private static final Logger LOG = LoggerFactory.getLogger(PathValidator.class);

    @Override
    public void initialize(Path path) {
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {

        if (StringUtils.isEmpty(path)) {
            return false;
        }

        try {
            URI validUrl = new URI(path);
            LOG.trace("Path is valid generic URI {}", validUrl);
            return path.startsWith("/");
        } catch (URISyntaxException e) {
            LOG.debug("Invalid path {}", path, e);
            return false;
        }
    }

}