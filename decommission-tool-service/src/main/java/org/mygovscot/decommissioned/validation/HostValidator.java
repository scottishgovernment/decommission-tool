package org.mygovscot.decommissioned.validation;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class HostValidator implements ConstraintValidator<Host, String> {

    private static final String IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final String HOST_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

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

        // space spearated list f valid hostnames
        if (!Arrays.stream(host.trim()
                .split(" "))
                .allMatch(h -> h.matches(IP_ADDRESS_REGEX) || h.matches(HOST_REGEX))) {
            return false;
        }

        return true;
    }

}