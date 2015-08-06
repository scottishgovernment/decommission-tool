package org.mygovscot.decommissioned.repository;

import org.mygovscot.decommissioned.model.WhitelistedHost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface WhitelistedHostRepository extends JpaRepository<WhitelistedHost, String> {

    List<WhitelistedHost> findAll();

    WhitelistedHost findOne(String id);
}