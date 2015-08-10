package org.mygovscot.decommissioned.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.mygovscot.decommissioned.validation.Path;
import org.mygovscot.decommissioned.validation.PathOrURL;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class Page {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @ManyToOne
    @NotNull
    private Site site;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL)
    private List<PageSuggestion> pageSuggestions;

    @NotNull(message = "Page source must be specified")
    @Path(message = "Source url must be a valid path")
    private String srcUrl;

    @NotNull(message = "Page target must be specified")
    @PathOrURL(message = "Target url must be a valid path or fully qualified url with a whitelisted host")
    private String targetUrl;

    private boolean locked;

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
                .append(id, page.id)
                .append(site, page.site)
                .append(pageSuggestions, page.pageSuggestions)
                .append(srcUrl, page.srcUrl)
                .append(targetUrl, page.targetUrl)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(site)
                .append(pageSuggestions)
                .append(srcUrl)
                .append(targetUrl)
                .append(locked)
                .toHashCode();
    }
}
