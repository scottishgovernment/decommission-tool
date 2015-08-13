package org.mygovscot.decommissioned.repository;

import org.mygovscot.decommissioned.model.PageSuggestion;
import org.mygovscot.decommissioned.model.SuggestionsSelector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SuggestionsSelectorRepository extends JpaRepository<SuggestionsSelector, String> {

    SuggestionsSelector findOne(String id);

    List<PageSuggestion> findBySiteId(@Param("siteId")String siteId);

}