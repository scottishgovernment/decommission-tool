package org.mygovscot.decommissioned.validation;

import org.springframework.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HostValidator implements ConstraintValidator<Host, String> {


    private static final String IP_ADDRESS_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final String HOST_REGEX = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    private Pattern hostPattern;
    private Pattern ipPattern;

    @Override
    public void initialize(Host host) {
        hostPattern = Pattern.compile(HOST_REGEX, Pattern.CASE_INSENSITIVE);
        ipPattern = Pattern.compile(IP_ADDRESS_REGEX, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean isValid(String host, ConstraintValidatorContext context) {
        if (StringUtils.isEmpty(host)) {
            return false;
        } else {
            Matcher hostMatcher = hostPattern.matcher(host);
            Matcher ipMatcher = ipPattern.matcher(host);

            return hostMatcher.matches() || ipMatcher.matches();
        }
    }

}