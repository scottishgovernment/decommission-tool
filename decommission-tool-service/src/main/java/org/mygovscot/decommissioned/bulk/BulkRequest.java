package org.mygovscot.decommissioned.bulk;

import java.util.List;

/**
 * Created by z418868 on 31/07/2015.
 */
public class BulkRequest {

    private List<String> ids;

    private boolean flag;

    private String targetUrl;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }
}
