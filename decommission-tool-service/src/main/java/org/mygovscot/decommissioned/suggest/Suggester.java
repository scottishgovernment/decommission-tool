package org.mygovscot.decommissioned.suggest;

import java.util.List;

public interface Suggester {

    List<String> suggestions(String searchPhrase);
}
