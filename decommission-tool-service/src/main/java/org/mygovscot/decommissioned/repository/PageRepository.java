package org.mygovscot.decommissioned.repository;


import org.mygovscot.decommissioned.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import javax.transaction.Transactional;
import java.util.List;

@RepositoryRestResource
public interface PageRepository extends JpaRepository<Page, String> {

    List<Page> findAll();

    Page findOneBySiteIdAndSrcUrl(String siteId, String srcUrl);

    List<Page> findBySiteId(String siteId);

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Modifying
    @Query("delete from Page where id = :id and locked = false")
    void delete(@Param("id")String id);

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Modifying
    @Query("delete from Page where id in (:ids) and locked = false")
    void bulkDelete(@Param("ids") List<String> ids);

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Modifying
    @Query("update Page set targetUrl = :targeturl where id in (:ids) and locked = false")
    void bulkSetTarget(@Param("ids") List<String> ids, @Param("targeturl") String targetUrl);

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    @Modifying
    @Query("update Page set locked = :locked where id in (:ids)")
    void bulkSetLock(@Param("ids") List<String> ids, @Param("locked") boolean locked);
}
