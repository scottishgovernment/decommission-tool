package org.mygovscot.decommissioned.validation;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class HostsListValidator implements ConstraintValidator<HostsList, String> {

    @Override
    public void initialize(HostsList host) {
        // No initialisation required.
    }

    @Override
    public boolean isValid(String host, ConstraintValidatorContext context) {

        // should not be empty or null
        if (StringUtils.isEmpty(host)) {
            return false;
        }

        // space spearated list of valid hostnames
        if (!Arrays.stream(host.trim()
                .split(" "))
                .allMatch(h -> h.matches(HostValidator.IP_ADDRESS_REGEX) || h.matches(HostValidator.HOST_REGEX))) {
            return false;
        }

        return true;
    }

}