package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskV3Repository extends JpaRepository<TaskV3, Integer> {
    List<TaskV3> findByBoardId(Board board);
    Optional<TaskV3> findByTaskIdAndBoardId(Integer taskId, Board board);
    List<TaskV3> findByBoardIdAndStatusId_StatusNameIn(Board board, List<String> statusNames);
    boolean existsByStatusId(Status status);  // Changed from Integer to Status

    List<TaskV3> findByStatusId(Status status);
}