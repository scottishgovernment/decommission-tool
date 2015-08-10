package org.mygovscot.decommissioned.importer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportRecordResult {

    public enum Type {
        SUCCESS, NOCHANGE, ERROR
    }

    private Type type;

    private String msg;

    private long lineNumber;

    public ImportRecordResult(Type type, String msg, long lineNumber) {
        this.type = type;
        this.msg = msg;
        this.lineNumber = lineNumber;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public long getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(long lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ImportRecordResult)) {
            return false;
        }

        ImportRecordResult that = (ImportRecordResult) o;

        return new EqualsBuilder()
                .append(lineNumber, that.lineNumber)
                .append(type, that.type)
                .append(msg, that.msg)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(type)
                .append(msg)
                .append(lineNumber)
                .toHashCode();
    }
}
