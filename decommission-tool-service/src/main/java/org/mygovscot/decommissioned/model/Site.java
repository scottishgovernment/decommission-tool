package org.mygovscot.decommissioned.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.mygovscot.decommissioned.validation.HostsList;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class Site {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    @NotNull(message = "Please specify a host (or hosts)")
    @HostsList(message = "HostsList must be a space separated list of valid host or ip address")
    private String host;

    @NotNull(message = "Please specify a name")
    private String name;

    private String description;

    private boolean httpsSupported;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Page> pages;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isHttpsSupported() {
        return httpsSupported;
    }

    public void setHttpsSupported(boolean httpsSupported) {
        this.httpsSupported = httpsSupported;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Site)) {
            return false;
        }

        Site site = (Site) o;

        return new EqualsBuilder()
                .append(httpsSupported, site.httpsSupported)
                .append(id, site.id)
                .append(host, site.host)
                .append(name, site.name)
                .append(description, site.description)
                .append(pages, site.pages)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(host)
                .append(name)
                .append(description)
                .append(httpsSupported)
                .append(pages)
                .toHashCode();
    }
}

