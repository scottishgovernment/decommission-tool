package org.mygovscot.decommissioned.repository;


import org.mygovscot.decommissioned.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, String> {

    List<Page> findAll();

    Page findOne(String id);

    Page findOneBySiteIdAndSrcUrl(@Param("siteId")String siteId, @Param("srcUrl")String srcUrl);

    Page findOneBySiteId(@Param("siteId")String siteId);

    Page findOneBySrcUrl(@Param("srcUrl")String srcUrl);

}