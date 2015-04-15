package org.mygovscot.decommissioned.repository;

import org.mygovscot.decommissioned.model.Site;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.repository.query.Param;
        import org.springframework.data.rest.core.annotation.RepositoryRestResource;

        import java.util.List;

public interface SiteRepository extends JpaRepository<Site, String> {

    List<Site> findAll();

    Site findOneByHost(@Param("host") String host);
}