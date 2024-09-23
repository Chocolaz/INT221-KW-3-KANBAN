package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<Status, Integer> {
    List<Status> findByBoardId(Board board);
    Optional<Status> findByStatusIdAndBoardId(Integer statusId, Board board);
    boolean existsByStatusNameAndBoardId(String statusName, Board board);
    Status findByStatusNameAndBoardId(String statusName, Board board);
}