package org.mygovscot.decommissioned.importer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by z418868 on 02/07/2015.
 */
public class ImportResult {

    private List<ImportTypeResult> results;

    public ImportResult(List<ImportRecordResult> results) {
        Map<String, List<ImportRecordResult>> resultsByType
                = results.stream().collect(Collectors.groupingBy(t -> t.getType().toString()+t.getMsg()));

        this.results = new ArrayList<>();

        for (Map.Entry<String, List<ImportRecordResult>> e : resultsByType.entrySet()) {
            List<Long> lineNumbers = e.getValue().stream().map(t -> t.getLineNumber()).collect(Collectors.toList());
            Collections.sort(lineNumbers);
            this.results.add(new ImportTypeResult(e.getValue().get(0).getType(), e.getValue().get(0).getMsg(), lineNumbers));
        }
    }

    public List<ImportTypeResult> getResults() {
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ImportResult)) {
            return false;
        }

        ImportResult that = (ImportResult) o;

        return new EqualsBuilder()
                .append(results, that.results)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(results)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ImportResult{" +
                "results=" + results +
                '}';
    }
}
