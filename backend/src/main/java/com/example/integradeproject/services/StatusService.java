package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.project_management.pm_entities.Task2;
import com.example.integradeproject.project_management.pm_repositories.StatusRepository;
import com.example.integradeproject.project_management.pm_repositories.Task2Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;

@Service
public class StatusService {
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private Task2Repository task2Repository ;
    @Autowired
    ModelMapper mapper;
    @Autowired
    ListMapper listMapper;
    @PersistenceContext
    private EntityManager entityManager;

    public List<Status> findAllStatus() {
        return statusRepository.findAll();
    }

    public Status createNewStatus(Status status) {
        if (status.getStatusName() == null || status.getStatusName().trim().toLowerCase().isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Name must not be null");
        }
        // valid name exist
        // เช็ค ว่า T or F
        boolean exists = statusRepository.existsByStatusName(status.getStatusName());
        if (exists) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Name must be unique. ");
        }


        StringBuilder error = new StringBuilder();
        if (status.getStatusName().length() > 50) {
            error.append("name size must be between 0 and 50.");
        }

        if (status.getStatusDescription().length() > 200) {
            error.append("description size must be between 0 and 200.");
        }
        if (error.length() > 0) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, error.toString());
        }

        return statusRepository.saveAndFlush(status);
    }



    @Transactional
    public Status updateByStatusId(Integer id, Status updatedStatus) {
        Optional<Status> optionalStatus = statusRepository.findById(id);
        if (optionalStatus.isPresent()) {
            Status existingStatus = optionalStatus.get();

            // Check status name ถูกแก้ไขไหม
            if (!existingStatus.getStatusName().equals(updatedStatus.getStatusName())) {
                // ถ้าถูกแก้ไขจะเช็คจาก boolean
                boolean exists = statusRepository.existsByStatusName(updatedStatus.getStatusName());
                if (exists) {
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Status name must be unique.");
                }
            }

            StringBuilder error = new StringBuilder();
            if (updatedStatus.getStatusName().length() > 50) {
                error.append("name size must be between 0 and 50.");
            }

            if (updatedStatus.getStatusDescription().length() > 200) {
                error.append("description size must be between 0 and 200.");
            }
            if (error.length() > 0) {
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, error.toString());
            }

            // Update the status
            existingStatus.setStatusName(updatedStatus.getStatusName());
            existingStatus.setStatusDescription(updatedStatus.getStatusDescription());
            return statusRepository.save(existingStatus);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Status not found with id: " + id);
        }
    }


    @Transactional
    public StatusDTO deleteById(Status id) {
        // validate
        if (id.getStatusId() == 1){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST ,  "No Status Cannot be deleted");
        }
        if (id.getStatusId() == 6){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST ,  "Done Cannot be deleted");
        }
        Status status = statusRepository.findById(id.getStatusId())
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "ID " + id + " DOES NOT EXIST !!!"));

        if (isStatusInUse(id)) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Cannot delete Status with ID " + id + " as it is currently in use.");
        }


        statusRepository.deleteById(id.getStatusId());
        StatusDTO deletedStatusDTO = mapper.map(status, StatusDTO.class);

        return deletedStatusDTO;
    }
    public boolean isStatusInUse(Status status) {
        return task2Repository.existsByStatusId(status.getStatusId());
    }

    @Transactional
    public void deleteStatusAndTransferTasks(int id, int newStatusId) {
        if (id == newStatusId){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "destination status for task transfer must be different from current status");
        }
        if (id == 1){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST ,  "No Status Cannot be deleted");
        }
        if (id == 6){
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST ,  "Done Cannot be deleted");
        }

        Status currentStatus = statusRepository.findById(id)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Status with ID " + id + " does not exist"));
        Status newStatus = statusRepository.findById(newStatusId)
                .orElseThrow(() -> new HttpClientErrorException(HttpStatus.BAD_REQUEST, "the specified status for task transfer does not exist"));

        List<Task2> tasksWithCurrentStatus = task2Repository.findByStatusId(currentStatus);
        tasksWithCurrentStatus.forEach(task -> task.setStatusId(newStatus));
        task2Repository.saveAll(tasksWithCurrentStatus);

        statusRepository.delete(currentStatus);
    }

}
