package org.mygovscot.decommissioned.validation;

import org.mygovscot.decommissioned.model.Page;
import org.mygovscot.decommissioned.model.Site;
import org.mygovscot.decommissioned.model.WhitelistedHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@ControllerAdvice
public class SiteExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SiteExceptionHandler.class);

    @ExceptionHandler({TransactionSystemException.class})
    protected ResponseEntity<Object> handleValidationError(RuntimeException e, WebRequest request) {
        TransactionSystemException ire = (TransactionSystemException) e;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Throwable originalException = ire.getOriginalException();
        if (originalException.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) originalException.getCause();
            Collection<SiteViolation> violations = getSiteViolations(constraintViolationException.getConstraintViolations());
            return handleExceptionInternal(constraintViolationException, violations, headers, HttpStatus.BAD_REQUEST, request);
        } else {
            LOG.error("Unable to process request.", e);
            return handleExceptionInternal(e, e.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    protected ResponseEntity<Object> handleDbError(RuntimeException e, WebRequest request) {

        DataIntegrityViolationException cve = (DataIntegrityViolationException) e;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(e, cve.getCause().getMessage(), headers, HttpStatus.BAD_REQUEST, request);
    }

    private Collection<SiteViolation> getSiteViolations(Set<ConstraintViolation<?>> constraintViolations) {
        Set<SiteViolation> violations = new HashSet<>();
        for (ConstraintViolation<?> violation : constraintViolations) {
            SiteViolation siteViolation = new SiteViolation();

            if (violation.getRootBean() instanceof Page) {
                Page page = (Page) violation.getRootBean();
                siteViolation.setSite(page.getSite().getId());
                siteViolation.setPage(page.getId());
            } else if (violation.getRootBean() instanceof WhitelistedHost) {
                WhitelistedHost whitelistedHost = (WhitelistedHost) violation.getRootBean();
                siteViolation.setHost(whitelistedHost.getHost());
            } else {
                Site site = (Site) violation.getRootBean();
                siteViolation.setSite(site.getId());
            }
            siteViolation.setViolation(violation.getMessage());
            violations.add(siteViolation);
        }
        return violations;
    }

    private static class SiteViolation {

        private String site;

        private String violation;

        private String path;

        private String page;

        private String host;

        public String getSite() {
            return site;
        }

        public void setSite(String site) {
            this.site = site;
        }

        public String getViolation() {
            return violation;
        }

        public void setViolation(String violation) {
            this.violation = violation;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public void setPage(String page) {
            this.page = page;
        }

        public String getPage() {
            return page;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }
    }

}
