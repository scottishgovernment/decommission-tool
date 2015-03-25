package org.mygovscot.decommissioned.repository;


import org.mygovscot.decommissioned.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface PageRepository extends JpaRepository<Page, String> {

    List<Page> findAll();

    Page findOne(String id);
}