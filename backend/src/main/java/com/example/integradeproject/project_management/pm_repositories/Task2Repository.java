package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.project_management.pm_entities.Task2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Task2Repository extends JpaRepository<Task2, Integer> {
    List<Task2> findByStatusId(Status string);
    @Query("SELECT t FROM Task2 t WHERE t.statusId.statusName IN :statusNames")
    List<Task2> findByStatusNames(@Param("statusNames") List<String> statusNames);


    List<Task2> findAllByOrderByStatusId_StatusNameAsc();
    List<Task2> findAllByOrderByStatusId_StatusNameDesc();

    List<Task2> findByStatusId_StatusNameInOrderByStatusId_StatusNameAsc(List<String> statusNames);

    List<Task2> findByStatusId_StatusNameInOrderByStatusId_StatusNameDesc(List<String> statusNames);

    default boolean existsByStatusId(Integer statusId) {
        return false;
    }
}
