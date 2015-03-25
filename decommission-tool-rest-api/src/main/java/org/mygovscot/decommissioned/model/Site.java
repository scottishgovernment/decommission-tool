package org.mygovscot.decommissioned.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Site {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    private String id;

    private String host;

    private String name;

    private String description;

    private String siteMatchMsg;

    private String categoryMatchMsg;

    private String pageMatchMsg;

    @OneToMany(mappedBy = "site", fetch = FetchType.LAZY)
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

    public String getSiteMatchMsg() {
        return siteMatchMsg;
    }

    public void setSiteMatchMsg(String siteMatchMsg) {
        this.siteMatchMsg = siteMatchMsg;
    }

    public String getCategoryMatchMsg() {
        return categoryMatchMsg;
    }

    public void setCategoryMatchMsg(String categoryMatchMsg) {
        this.categoryMatchMsg = categoryMatchMsg;
    }

    public String getPageMatchMsg() {
        return pageMatchMsg;
    }

    public void setPageMatchMsg(String pageMatchMsg) {
        this.pageMatchMsg = pageMatchMsg;
    }

//    public List<Page> getPages() {
//        return pages;
//    }
//
//    public void setPages(List<Page> pages) {
//        this.pages = pages;
//    }
}

