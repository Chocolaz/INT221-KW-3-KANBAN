package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.project_management.pm_entities.Task2;
import com.example.integradeproject.project_management.pm_repositories.BoardRepository;
import com.example.integradeproject.project_management.pm_repositories.StatusRepository;
import com.example.integradeproject.project_management.pm_repositories.Task2Repository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class StatusV3Service {
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private Task2Repository task2Repository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private ModelMapper modelMapper;

    public List<Status> findAllStatusesForBoard(String boardId) {
        Board board = getBoardById(boardId);
        return statusRepository.findAllByBoardId(board);
    }

    @Transactional
    public Status createNewStatus(String boardId, Status status) {
        Board board = getBoardById(boardId);
        validateStatusName(status.getStatusName());
        validateStatusDescription(status.getStatusDescription());
        status.setBoardId(board);
        return statusRepository.save(status);
    }

    @Transactional
    public Status updateStatus(String boardId, Integer statusId, Status updatedStatus) {
        Board board = getBoardById(boardId);
        Status existingStatus = getStatusById(statusId, board);

        if (!existingStatus.getStatusName().equals(updatedStatus.getStatusName())) {
            validateStatusName(updatedStatus.getStatusName());
        }
        validateStatusDescription(updatedStatus.getStatusDescription());

        existingStatus.setStatusName(updatedStatus.getStatusName());
        existingStatus.setStatusDescription(updatedStatus.getStatusDescription());
        return statusRepository.save(existingStatus);
    }

    @Transactional
    public StatusDTO deleteStatus(String boardId, Integer statusId) {
        Board board = getBoardById(boardId);
        Status status = getStatusById(statusId, board);

        if (statusId == 1 || statusId == 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This status cannot be deleted");
        }

        if (isStatusInUse(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete Status as it is currently in use.");
        }

        statusRepository.delete(status);
        return modelMapper.map(status, StatusDTO.class);
    }

    @Transactional
    public void deleteStatusAndTransferTasks(String boardId, int statusId, int newStatusId) {
        Board board = getBoardById(boardId);
        Status currentStatus = getStatusById(statusId, board);
        Status newStatus = getStatusById(newStatusId, board);

        if (statusId == newStatusId) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination status for task transfer must be different from current status");
        }
        if (statusId == 1 || statusId == 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This status cannot be deleted");
        }

        List<Task2> tasksWithCurrentStatus = task2Repository.findByStatusId(currentStatus);
        tasksWithCurrentStatus.forEach(task -> task.setStatusId(newStatus));
        task2Repository.saveAll(tasksWithCurrentStatus);

        statusRepository.delete(currentStatus);
    }

    private Board getBoardById(String boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));
    }

    private Status getStatusById(Integer statusId, Board board) {
        return (Status) statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found in the specified board"));
    }

    private void validateStatusName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be null or empty");
        }
        if (name.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name size must be between 0 and 50");
        }
        if (statusRepository.existsByStatusName(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name must be unique");
        }
    }

    private void validateStatusDescription(String description) {
        if (description != null && description.length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description size must be between 0 and 200");
        }
    }

    private boolean isStatusInUse(Status status) {
        return task2Repository.existsByStatusId(status.getStatusId());
    }
}