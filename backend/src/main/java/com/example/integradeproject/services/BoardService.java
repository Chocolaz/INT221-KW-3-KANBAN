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
        String ownerName = jwtTokenUtil.getNameFromToken(token);


        Board board = new Board();
        board.setName(boardName);
        board.setOwnerOid(ownerOid);

        Board savedBoard = boardRepository.save(board);

        // Create default statuses
        List<Status> defaultStatuses = createDefaultStatuses(savedBoard);
        statusRepository.saveAll(defaultStatuses);

        return convertToDTO(savedBoard, ownerName);
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
        String ownerName = jwtTokenUtil.getNameFromToken(token);



        List<Board> boards = boardRepository.findAllByOwnerOid(ownerOid);

        return boards.stream().map(board -> convertToDTO(board, ownerName)).collect(Collectors.toList());
    }

    public Optional<BoardDTO> getBoardById(String boardId, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        String ownerName = jwtTokenUtil.getNameFromToken(token);

        Optional<Board> boardOptional = boardRepository.findById(boardId);
        if (boardOptional.isPresent() && boardOptional.get().getOwnerOid().equals(ownerOid)) {
            return Optional.of(convertToDTO(boardOptional.get(), ownerName));
        } else {
            return Optional.empty();
        }
    }

    public List<Task2DTO> getTasksForBoard(String boardId, String token, String sortBy, List<String> filterStatuses) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Optional<Board> boardOptional = boardRepository.findByIdAndOwnerOid(boardId, ownerOid);

        if (boardOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access");
        }

        List<TaskV3> tasks = taskRepository.findByBoardId(boardOptional.get());

        // Apply filtering and sorting logic here

        return tasks.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }
    public Task2IdDTO getTaskById(String boardId, Integer taskId, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = (TaskV3) taskRepository.findByTaskIdAndBoardId(taskId, board)
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
        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
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
        }

        TaskV3 savedTask = taskRepository.save(task);
        return convertToNewTaskDTO(savedTask);
    }
    @Transactional
    public NewTask2DTO updateTask(String boardId, Integer taskId, NewTask2DTO updateTaskDTO, String token) {
        String ownerOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = (TaskV3) taskRepository.findByTaskIdAndBoardId(taskId, board)
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
        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));

        TaskV3 task = (TaskV3) taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        taskRepository.delete(task);
    }

    private BoardDTO convertToDTO(Board board, String ownerName) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setId(board.getId());
        boardDTO.setName(board.getName());

        BoardDTO.OwnerDTO ownerDTO = new BoardDTO.OwnerDTO();
        ownerDTO.setOid(board.getOwnerOid());
        ownerDTO.setName(ownerName);

        boardDTO.setOwner(ownerDTO);
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