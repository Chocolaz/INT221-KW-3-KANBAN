package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends JpaRepository  <Status , Integer>{
    Optional<Status> findByStatusName(String statusName);

    List<Status> findByStatusNameContains( String Name );
    boolean existsByStatusName(String name);

    List<Status> findAllByBoardId(Board board);

    boolean existsByStatusNameAndBoardId(String statusName, Board boardId);
    Status findByStatusNameAndBoardId(String statusName, Board boardId);

    List<Status> findByBoardId(Board board);

    Optional<Object> findByStatusIdAndBoardId(Integer statusId, Board board);
}
