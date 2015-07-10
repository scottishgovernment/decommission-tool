package org.mygovscot.decommissioned.repository;

import org.mygovscot.decommissioned.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface JobRepository extends JpaRepository<Job, String> {

    List<Job> findAll();

    Job findOne(String id);
}