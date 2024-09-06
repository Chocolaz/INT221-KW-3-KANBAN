package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.project_management.pm_entities.Task2;
import com.example.integradeproject.project_management.pm_repositories.BoardRepository;
import com.example.integradeproject.project_management.pm_repositories.StatusRepository;
import com.example.integradeproject.project_management.pm_repositories.Task2Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusService {
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private Task2Repository task2Repository;
    @Autowired
    ModelMapper mapper;
    @Autowired
    ListMapper listMapper;
    @PersistenceContext
    private EntityManager entityManager;

    public List<StatusDTO> findAllStatusesByBoardId(String boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));
        List<Status> statuses = statusRepository.findByBoardId(board);
        return listMapper.mapList(statuses, StatusDTO.class);
    }

    public StatusDTO createNewStatus(Status status, String boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (status.getStatusName() == null || status.getStatusName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be null or empty");
        }

        boolean exists = statusRepository.existsByStatusNameAndBoardId(status.getStatusName(), board);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name must be unique within the board");
        }

        if (status.getStatusName().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name size must be between 1 and 50");
        }

        if (status.getStatusDescription() != null && status.getStatusDescription().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description size must be between 0 and 200");
        }

        status.setBoardId(board);
        Status savedStatus = statusRepository.save(status);
        return mapper.map(savedStatus, StatusDTO.class);
    }

    @Transactional
    public StatusDTO updateStatus(String boardId, Integer statusId, Status updatedStatus) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        Status existingStatus = (Status) statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found in this board"));

        if (!existingStatus.getStatusName().equals(updatedStatus.getStatusName())) {
            boolean exists = statusRepository.existsByStatusNameAndBoardId(updatedStatus.getStatusName(), board);
            if (exists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name must be unique within the board");
            }
        }

        if (updatedStatus.getStatusName().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name size must be between 1 and 50");
        }

        if (updatedStatus.getStatusDescription() != null && updatedStatus.getStatusDescription().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description size must be between 0 and 200");
        }

        existingStatus.setStatusName(updatedStatus.getStatusName());
        existingStatus.setStatusDescription(updatedStatus.getStatusDescription());
        Status savedStatus = statusRepository.save(existingStatus);
        return mapper.map(savedStatus, StatusDTO.class);
    }

    @Transactional
    public void deleteStatus(String boardId, Integer statusId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        Status status = (Status) statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found in this board"));

        if (status.getStatusName().equals("No Status") || status.getStatusName().equals("Done")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete 'No Status' or 'Done' status");
        }

        if (task2Repository.existsByStatusId(status.getStatusId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete Status as it is currently in use");
        }

        statusRepository.delete(status);
    }

    @Transactional
    public void deleteStatusAndTransferTasks(String boardId, Integer statusId, Integer newStatusId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        Status currentStatus = (Status) statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current status not found in this board"));

        Status newStatus = (Status) statusRepository.findByStatusIdAndBoardId(newStatusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New status not found in this board"));

        if (statusId.equals(newStatusId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination status must be different from current status");
        }

        if (currentStatus.getStatusName().equals("No Status") || currentStatus.getStatusName().equals("Done")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete 'No Status' or 'Done' status");
        }

        List<Task2> tasksWithCurrentStatus = task2Repository.findByStatusId(currentStatus);
        tasksWithCurrentStatus.forEach(task -> task.setStatusId(newStatus));
        task2Repository.saveAll(tasksWithCurrentStatus);

        statusRepository.delete(currentStatus);
    }
}