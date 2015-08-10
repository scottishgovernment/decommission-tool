package org.mygovscot.decommissioned.importer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * Created by z418868 on 07/08/2015.
 */
public class ImportTypeResult {

    private ImportRecordResult.Type type;

    private String msg;

    private List<Long> lineNumbers;

    public ImportTypeResult(ImportRecordResult.Type type, String msg, List<Long>lineNumbers) {
        this.type = type;
        this.msg = msg;
        this.lineNumbers = lineNumbers;
    }

    public ImportRecordResult.Type getType() {
        return type;
    }

    public void setType(ImportRecordResult.Type type) {
        this.type = type;
    }

    public List<Long> getLineNumbers() {
        return lineNumbers;
    }

    public void setLineNumbers(List<Long> lineNumbers) {
        this.lineNumbers = lineNumbers;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ImportTypeResult)) {
            return false;
        }

        ImportTypeResult that = (ImportTypeResult) o;

        return new EqualsBuilder()
                .append(type, that.type)
                .append(msg, that.msg)
                .append(lineNumbers, that.lineNumbers)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(msg)
                .append(lineNumbers)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ImportTypeResult{" +
                "type=" + type +
                ", msg='" + msg + '\'' +
                ", lineNumbers=" + lineNumbers +
                '}';
    }
}
