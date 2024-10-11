package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Collab;
import com.example.integradeproject.project_management.pm_entities.PMUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CollabRepository extends JpaRepository<Collab, String> {
    List<Collab> findByBoard(Board board);
    Optional<Collab> findByBoardAndOid(Board board, PMUser oid);
    List<Collab> findByOid(PMUser user);

}
