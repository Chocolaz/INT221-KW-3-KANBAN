package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.project_management.pm_entities.Task2;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskV3Repository extends JpaRepository<TaskV3, Integer> {
    List<TaskV3> findByStatusId(Status string);
    @Query("SELECT t FROM TaskV3 t WHERE t.statusId.statusName IN :statusNames")
    List<Task2> findByStatusNames(@Param("statusNames") List<String> statusNames);


    List<Task2> findAllByOrderByStatusId_StatusNameAsc();
    List<Task2> findAllByOrderByStatusId_StatusNameDesc();

    List<Task2> findByStatusId_StatusNameInOrderByStatusId_StatusNameAsc(List<String> statusNames);

    List<Task2> findByStatusId_StatusNameInOrderByStatusId_StatusNameDesc(List<String> statusNames);

    default boolean existsByStatusId(Integer statusId) {
        return false;
    }

    List<TaskV3> findByBoardId(Board board);

    Optional<TaskV3> findByBoardIdAndTaskId(Board board, Integer taskId);

    List<TaskV3> findByBoardIdAndStatusIdStatusNameIn(Board board, List<String> statusNames);

    Optional<Object> findByTaskIdAndBoardId(Integer taskId, Board board);
}
