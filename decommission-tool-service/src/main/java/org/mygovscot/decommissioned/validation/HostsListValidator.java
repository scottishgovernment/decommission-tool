package org.mygovscot.decommissioned.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

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

        // space separated list of valid hostnames
        return Arrays.stream(host.trim().split(" ")).allMatch(this::validHostnameOrIPAddress);
    }

    boolean validHostnameOrIPAddress(String str) {
        return str.matches(HostValidator.IP_ADDRESS_REGEX) || str.matches(HostValidator.HOST_REGEX);
    }

}
