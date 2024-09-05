//package com.example.integradeproject.services;
//
//import com.example.integradeproject.project_management.pm_dtos.BoardDTO;
//import com.example.integradeproject.project_management.pm_dtos.NewTask2DTO;
//import com.example.integradeproject.project_management.pm_dtos.Task2DTO;
//import com.example.integradeproject.project_management.pm_entities.Board;
//import com.example.integradeproject.project_management.pm_entities.Status;
//import com.example.integradeproject.project_management.pm_entities.TaskV3;
//import com.example.integradeproject.project_management.pm_repositories.BoardRepository;
//import com.example.integradeproject.project_management.pm_repositories.StatusRepository;
//import com.example.integradeproject.project_management.pm_repositories.TaskV3Repository;
//import com.example.integradeproject.security.JwtTokenUtil;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class TaskV3Service {
//
//    @Autowired
//    private TaskV3Repository taskRepository;
//    @Autowired
//    private BoardRepository boardRepository;
//    @Autowired
//    private StatusRepository statusRepository;
//    @Autowired
//    private ModelMapper modelMapper;
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    public List<Task2DTO> getTasksForBoard(String boardId, String token, String sortBy, List<String> filterStatuses) {
//        String ownerOid = jwtTokenUtil.getUidFromToken(token);
//        Optional<Board> boardOptional = boardRepository.findByIdAndOwnerOid(boardId, ownerOid);
//
//        if (boardOptional.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access");
//        }
//
//        List<TaskV3> tasks = taskRepository.findByBoardId(boardOptional.get());
//
//        // Apply filtering and sorting logic here
//
//        return tasks.stream()
//                .map(this::convertToTaskDTO)
//                .collect(Collectors.toList());
//    }
//
//    public NewTask2DTO createTask(String boardId, NewTask2DTO newTaskDTO, String token) {
//        String ownerOid = jwtTokenUtil.getUidFromToken(token);
//        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));
//
//        TaskV3 task = new TaskV3();
//        task.setTitle(newTaskDTO.getTitle());
//        task.setDescription(newTaskDTO.getDescription());
//        task.setAssignees(newTaskDTO.getAssignees());
//        task.setBoardId(board);
//
//        if (newTaskDTO.getStatusName() != null) {
//            Status status = statusRepository.findByStatusName(newTaskDTO.getStatusName())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found"));
//            task.setStatusId(status);
//        }
//
//        TaskV3 savedTask = taskRepository.save(task);
//        return convertToNewTaskDTO(savedTask);
//    }
//
//    public NewTask2DTO updateTask(String boardId, Integer taskId, NewTask2DTO updateTaskDTO, String token) {
//        String ownerOid = jwtTokenUtil.getUidFromToken(token);
//        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));
//
//        TaskV3 task = (TaskV3) taskRepository.findByTaskIdAndBoardId(taskId, board)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
//
//        task.setTitle(updateTaskDTO.getTitle());
//        task.setDescription(updateTaskDTO.getDescription());
//        task.setAssignees(updateTaskDTO.getAssignees());
//
//        if (updateTaskDTO.getStatusName() != null) {
//            Status status = statusRepository.findByStatusName(updateTaskDTO.getStatusName())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found"));
//            task.setStatusId(status);
//        }
//
//        TaskV3 updatedTask = taskRepository.save(task);
//        return convertToNewTaskDTO(updatedTask);
//    }
//
//    public NewTask2DTO deleteTask(String boardId, Integer taskId, String token) {
//        String ownerOid = jwtTokenUtil.getUidFromToken(token);
//        Board board = boardRepository.findByIdAndOwnerOid(boardId, ownerOid)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found or you don't have access"));
//
//        TaskV3 task = (TaskV3) taskRepository.findByTaskIdAndBoardId(taskId, board)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
//
//        NewTask2DTO deletedTaskDTO = convertToNewTaskDTO(task);
//        taskRepository.delete(task);
//        return deletedTaskDTO;
//    }
//
//    private BoardDTO convertToDTO(Board board, String ownerName) {
//        BoardDTO boardDTO = new BoardDTO();
//        boardDTO.setId(board.getId());
//        boardDTO.setName(board.getName());
//
//        BoardDTO.OwnerDTO ownerDTO = new BoardDTO.OwnerDTO();
//        ownerDTO.setOid(board.getOwnerOid());
//        ownerDTO.setName(ownerName);
//
//        boardDTO.setOwner(ownerDTO);
//        return boardDTO;
//    }
//
//    private Task2DTO convertToTaskDTO(TaskV3 task) {
//        return modelMapper.map(task, Task2DTO.class);
//    }
//
//    private NewTask2DTO convertToNewTaskDTO(TaskV3 task) {
//        NewTask2DTO dto = new NewTask2DTO();
//        dto.setTaskId(task.getTaskId());
//        dto.setTitle(task.getTitle());
//        dto.setDescription(task.getDescription());
//        dto.setAssignees(task.getAssignees());
//        dto.setStatusName(task.getStatusId() != null ? task.getStatusId().getStatusName() : null);
//        return dto;
//    }
//}