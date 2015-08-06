package org.mygovscot.decommissioned.validation;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HostValidator implements ConstraintValidator<Host, String> {

    public static final String IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    public static final String HOST_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    @Override
    public void initialize(Host host) {
        // No initialisation required.
    }

    @Override
    public boolean isValid(String host, ConstraintValidatorContext context) {

        // should not be empty or null
        if (StringUtils.isEmpty(host)) {
            return false;
        }

        return host.matches(IP_ADDRESS_REGEX) || host.matches(HOST_REGEX);
    }

}