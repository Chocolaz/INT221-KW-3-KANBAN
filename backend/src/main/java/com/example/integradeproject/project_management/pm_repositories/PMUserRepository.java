package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.PMUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PMUserRepository extends JpaRepository<PMUser, String> {
    Optional<PMUser> findByOid(String oid);
    Optional<PMUser> findByEmail(String email);

}