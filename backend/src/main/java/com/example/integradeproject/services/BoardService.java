package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.*;
import com.example.integradeproject.project_management.pm_entities.*;
import com.example.integradeproject.project_management.pm_repositories.*;
import com.example.integradeproject.security.JwtTokenUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private PMUserRepository pmUserRepository;

    @Autowired
    private TaskV3Repository taskRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public BoardDTO createBoard(String boardName, String token) {
        if (boardName == null || boardName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name cannot be empty");
        }

        if (boardName.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name cannot exceed 120 characters");
        }

        String ownerOid = jwtTokenUtil.getUidFromToken(token);

        PMUser owner = pmUserRepository.findByOid(ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = new Board();
        board.setName(boardName);
        board.setOwnerOid(owner);

        Board savedBoard = boardRepository.save(board);

        // Create default statuses
        List<Status> defaultStatuses = createDefaultStatuses(savedBoard);
        statusRepository.saveAll(defaultStatuses);

        return convertToDTO(savedBoard);
    }

    private List<Status> createDefaultStatuses(Board board) {
        List<String> defaultStatusNames = Arrays.asList("No Status", "To Do", "Doing", "Done");
        List<String> defaultStatusDescriptions = Arrays.asList(
                "The default status",
                "The task is included in the project",
                "The task is being worked on",
                "Finished"
        );

        List<Status> statuses = new ArrayList<>();
        for (int i = 0; i < defaultStatusNames.size(); i++) {
            Status status = new Status();
            status.setStatusName(defaultStatusNames.get(i));
            status.setStatusDescription(defaultStatusDescriptions.get(i));
            status.setBoardId(board);
            statuses.add(status);
        }
        return statuses;
    }
    public List<BoardDTO> getBoardsForUser(String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        PMUser owner = pmUserRepository.findByOid(ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Board> boards = boardRepository.findAllByOwnerOid(owner);

        return boards.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<BoardDTO> getBoardById(String boardId, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        PMUser owner = pmUserRepository.findByOid(ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<Board> boardOptional = boardRepository.findByIdAndOwnerOid(boardId, owner);
        return boardOptional.map(this::convertToDTO);
    }

    public List<Task2DTO> getTasksForBoard(String boardId, String token, String sortBy, List<String> filterStatuses) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, pmUserRepository.findByOid(ownerOid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        List<TaskV3> tasks;
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            tasks = taskRepository.findByBoardIdAndStatusId_StatusNameIn(board, filterStatuses);
        } else {
            tasks = taskRepository.findByBoardId(board);
        }

        // Apply sorting logic here if needed

        return tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }
    public Task2IdDTO getTaskById(String boardId, Integer taskId, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, pmUserRepository.findByOid(ownerOid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        return convertToTask2IdDTO(task);
    }

    private Task2IdDTO convertToTask2IdDTO(TaskV3 task) {
        Task2IdDTO dto = new Task2IdDTO();
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setAssignees(task.getAssignees());
        dto.setStatusName(task.getStatusId().getStatusName());
        dto.setCreatedOn((Timestamp) task.getCreatedOn());
        dto.setUpdatedOn((Timestamp) task.getUpdatedOn());
        return dto;
    }

    public NewTask2DTO createTask(String boardId, NewTask2DTO newTaskDTO, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, pmUserRepository.findByOid(ownerOid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = new TaskV3();
        task.setTitle(newTaskDTO.getTitle());
        task.setDescription(newTaskDTO.getDescription());
        task.setAssignees(newTaskDTO.getAssignees());
        task.setBoardId(board);

        if (newTaskDTO.getStatusName() != null) {
            Status status = statusRepository.findByStatusNameAndBoardId(newTaskDTO.getStatusName(), board);
            if (status == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found in this board");
            }
            task.setStatusId(status);
        } else {
            // Set default status if not provided
            Status defaultStatus = statusRepository.findByStatusNameAndBoardId("No Status", board);
            task.setStatusId(defaultStatus);
        }


        TaskV3 savedTask = taskRepository.save(task);
        return convertToNewTaskDTO(savedTask);
    }

    @Transactional
    public NewTask2DTO updateTask(String boardId, Integer taskId, NewTask2DTO updateTaskDTO, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, pmUserRepository.findByOid(ownerOid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        task.setTitle(updateTaskDTO.getTitle());
        task.setDescription(updateTaskDTO.getDescription());
        task.setAssignees(updateTaskDTO.getAssignees());

        if (updateTaskDTO.getStatusName() != null) {
            Status status = statusRepository.findByStatusNameAndBoardId(updateTaskDTO.getStatusName(), board);
            if (status == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found in this board");
            }
            task.setStatusId(status);
        }

        TaskV3 updatedTask = taskRepository.save(task);
        return convertToNewTaskDTO(updatedTask);
    }

    public void deleteTask(String boardId, Integer taskId, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, pmUserRepository.findByOid(ownerOid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        taskRepository.delete(task);
    }

    private BoardDTO convertToDTO(Board board) {
        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);
        boardDTO.setOwner(modelMapper.map(board.getOwnerOid(), BoardDTO.PMUserDTO.class));
        return boardDTO;
    }

    private Task2DTO convertToTaskDTO(TaskV3 task) {
        return modelMapper.map(task, Task2DTO.class);
    }

    private NewTask2DTO convertToNewTaskDTO(TaskV3 task) {
        NewTask2DTO dto = modelMapper.map(task, NewTask2DTO.class);
        dto.setStatusName(task.getStatusId().getStatusName());
        return dto;
    }
}