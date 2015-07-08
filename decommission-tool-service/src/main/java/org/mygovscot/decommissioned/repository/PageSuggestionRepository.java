package org.mygovscot.decommissioned.repository;

import org.mygovscot.decommissioned.model.PageSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface PageSuggestionRepository extends JpaRepository<PageSuggestion, String> {

    PageSuggestion findOne(String id);

    List<PageSuggestion> findByPageId(@Param("pageId")String pageId);

}