package org.mygovscot.decommissioned.importer;

/**
 * Created by z418868 on 02/07/2015.
 */
public class ImportResult {

    private int addedCount;
    private int skippedCount;

    public ImportResult(int addedCount, int skippedCount) {
        this.addedCount = addedCount;
        this.skippedCount = skippedCount;
    }

    public int getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(int addedCount) {
        this.addedCount = addedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }
}
