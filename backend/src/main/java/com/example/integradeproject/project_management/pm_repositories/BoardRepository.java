package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.PMUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findAllByOwnerOid(PMUser ownerOid);

    Optional<Board> findById(String boardId);

    Optional<Board> findByIdAndOwnerOid(String boardId, PMUser ownerOid);
}