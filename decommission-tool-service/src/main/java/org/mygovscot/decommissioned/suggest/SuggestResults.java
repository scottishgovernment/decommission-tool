package org.mygovscot.decommissioned.suggest;

public class SuggestResults {

    private int suggestionsCount;

    public SuggestResults(int suggestionsCount) {
        this.suggestionsCount = suggestionsCount;
    }

    public int getSuggestionsCount() {
        return suggestionsCount;
    }

    public void setSuggestionsCount(int suggestionsCount) {
        this.suggestionsCount = suggestionsCount;
    }
}
