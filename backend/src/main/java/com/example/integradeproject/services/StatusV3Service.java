package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
import com.example.integradeproject.project_management.pm_entities.*;

import com.example.integradeproject.project_management.pm_repositories.*;
import com.example.integradeproject.security.JwtTokenUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StatusV3Service {
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private TaskV3Repository taskV3Repository;
    @Autowired
    private CollabRepository collabRepository;
    @Autowired
    private PMUserRepository pmUserRepository;
    @Autowired
    ModelMapper mapper;
    @Autowired
    ListMapper listMapper;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @PersistenceContext
    private EntityManager entityManager;

    public List<StatusDTO> findAllStatusesByBoardId(String boardId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getVisibility() == Board.BoardVisibility.PUBLIC || isUserAuthorized(token, board, Collab.AccessRight.READ)) {
            return getStatusesForBoard(board);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to board");
    }

    public StatusDTO getStatusById(String boardId, Integer statusId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getVisibility() == Board.BoardVisibility.PUBLIC || isUserAuthorized(token, board, Collab.AccessRight.WRITE)) {
            return findStatusInBoard(statusId, board);
        }

        if (isUserAuthorized(token, board, Collab.AccessRight.READ)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found");
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to board");
    }

    public StatusDTO createNewStatus(Status status, String boardId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!isUserAuthorized(token, board, Collab.AccessRight.WRITE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
        }
        checkBoardAccess(board, token, true);


        validateStatus(status, board);
        status.setBoardId(board);
        Status savedStatus = statusRepository.save(status);
        return mapper.map(savedStatus, StatusDTO.class);
    }

    public StatusDTO updateStatus(String boardId, Integer statusId, Status updatedStatus, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!isUserAuthorized(token, board, Collab.AccessRight.WRITE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");


        }

        checkBoardAccess(board, token, true);

        Status existingStatus = statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found in this board"));

        validateStatus(updatedStatus, board);
        existingStatus.setStatusName(updatedStatus.getStatusName());
        existingStatus.setStatusDescription(updatedStatus.getStatusDescription());
        Status savedStatus = statusRepository.save(existingStatus);
        return mapper.map(savedStatus, StatusDTO.class);
    }

    @Transactional
    public void deleteStatus(String boardId, Integer statusId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!isUserAuthorized(token, board, Collab.AccessRight.WRITE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
        }

        Status status = statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found in this board"));

        if (status.getStatusName().equals("No Status") || status.getStatusName().equals("Done")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete 'No Status' or 'Done' status");
        }

        if (taskV3Repository.existsByStatusId(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete Status as it is currently in use");
        }

        statusRepository.delete(status);
    }

    @Transactional
    public void deleteStatusAndTransferTasks(String boardId, Integer statusId, Integer newStatusId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!isUserAuthorized(token, board, Collab.AccessRight.WRITE)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
        }

        Status currentStatus = statusRepository.findByStatusIdAndBoardId(statusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current status not found in this board"));

        Status newStatus = statusRepository.findByStatusIdAndBoardId(newStatusId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "New status not found in this board"));

        if (statusId.equals(newStatusId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination status must be different from current status");
        }

        if (currentStatus.getStatusName().equals("No Status") || currentStatus.getStatusName().equals("Done")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot delete 'No Status' or 'Done' status");
        }

        List<TaskV3> tasksWithCurrentStatus = taskV3Repository.findByStatusId(currentStatus);
        for (TaskV3 task : tasksWithCurrentStatus) {
            task.setStatusId(newStatus);
        }
        taskV3Repository.saveAll(tasksWithCurrentStatus);

        statusRepository.delete(currentStatus);
    }

    private boolean isUserAuthorized(String token, Board board, Collab.AccessRight requiredAccess) {
        if (token == null) {
            return false;
        }

        try {
            String userOid = jwtTokenUtil.getUidFromToken(token);
            if (board.getOwnerOid().getOid().equals(userOid)) {
                return true;
            }

            PMUser user = pmUserRepository.findByOid(userOid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
            return collaboration.isPresent() &&
                    (collaboration.get().getAccess_right() == Collab.AccessRight.WRITE ||
                            (requiredAccess == Collab.AccessRight.READ && collaboration.get().getAccess_right() == Collab.AccessRight.READ));
        } catch (Exception e) {
            return false;
        }
    }

    private List<StatusDTO> getStatusesForBoard(Board board) {
        List<Status> statuses = statusRepository.findByBoardId(board);
        return listMapper.mapList(statuses, StatusDTO.class);
    }

    private StatusDTO findStatusInBoard(Integer statusId, Board board) {
        return statusRepository.findByStatusIdAndBoardId(statusId, board)
                .map(status -> mapper.map(status, StatusDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status not found in this board"));
    }

    private void validateStatus(Status status, Board board) {
        if (status.getStatusName() == null || status.getStatusName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name must not be null or empty");
        }

        if (status.getStatusName().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name size must be between 1 and 50");
        }

        if (status.getStatusDescription() != null && status.getStatusDescription().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description size must be between 0 and 200");
        }

        boolean exists = statusRepository.existsByStatusNameAndBoardId(status.getStatusName(), board);
        if (exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status name must be unique within the board");
        }
    }

    private void checkBoardAccess(Board board, String token, boolean requireWriteAccess) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // If write access is required, only owner and write collaborators can proceed
        if (requireWriteAccess) {
            if (board.getOwnerOid().getOid().equals(userOid)) {
                return;
            }

            Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
            if (collaboration.isEmpty() || collaboration.get().getAccess_right() != Collab.AccessRight.WRITE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
            }
        } else {
            // For read access, check if user is owner or any type of collaborator
            if (board.getOwnerOid().getOid().equals(userOid)) {
                return;
            }

            Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
            if (collaboration.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
            }
        }
    }

}