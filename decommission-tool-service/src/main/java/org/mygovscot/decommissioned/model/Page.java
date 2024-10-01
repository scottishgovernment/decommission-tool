package org.mygovscot.decommissioned.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.mygovscot.decommissioned.validation.PathOrURL;
import org.mygovscot.decommissioned.validation.SrcUrl;

@Entity
@SrcUrl(message="Page source should be a valid URL, path or Perl compatible Regexp")
public class Page {

    public enum MatchType {
        EXACT, PCRE_REGEXP
    }

    public enum RedirectType {
        REDIRECT, PERMANENT
    }

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @ManyToOne
    @NotNull
    private Site site;

    @NotNull(message = "Page source must be specified")
    private String srcUrl;

    @NotNull(message = "Page target must be specified")
    @PathOrURL(message = "Target url must be a valid path or fully qualified url with a whitelisted host")
    private String targetUrl;

    private boolean locked;

    @Enumerated(EnumType.STRING)
    private MatchType type;

    @Enumerated(EnumType.STRING)
    private RedirectType redirectType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSrcUrl() {
        return srcUrl;
    }

    public void setSrcUrl(String srcUrl) {
        this.srcUrl = srcUrl;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public RedirectType getRedirectType() {
        return redirectType;
    }

    public void setRedirectType(RedirectType redirectType) {
        this.redirectType = redirectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Page)) {
            return false;
        }

        Page page = (Page) o;

        return new EqualsBuilder()
                .append(locked, page.locked)
                .append(getId(), page.getId())
                .append(getSite(), page.getSite())
                .append(srcUrl, page.srcUrl)
                .append(targetUrl, page.targetUrl)
                .append(type, page.type)
                .append(redirectType, page.redirectType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getId())
                .append(getSite())
                .append(srcUrl)
                .append(targetUrl)
                .append(locked)
                .append(type)
                .append(redirectType)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "Page{" +
                "id='" + id + '\'' +
                ", site=" + site +
                ", srcUrl='" + srcUrl + '\'' +
                ", targetUrl='" + targetUrl + '\'' +
                ", locked=" + locked +
                ", type=" + type +
                ", redirectType=" + redirectType +
                '}';
    }
}
