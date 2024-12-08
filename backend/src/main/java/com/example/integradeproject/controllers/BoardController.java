package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.*;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import com.example.integradeproject.project_management.pm_repositories.BoardRepository;
import com.example.integradeproject.project_management.pm_repositories.TaskV3Repository;
import com.example.integradeproject.security.JwtTokenUtil;
import com.example.integradeproject.services.BoardService;
import com.example.integradeproject.services.TaskAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173","https://ip23kw3.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th", "https://localhost:5174"})
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private TaskAttachmentService taskAttachmentService;

    @Autowired
    private TaskV3Repository taskRepository;

    @Autowired
    BoardRepository boardRepository ;


    @PostMapping("")
    public ResponseEntity<?> createBoard(@RequestBody(required = false) Map<String, String> boardRequest, @RequestHeader("Authorization") String token) {
        if (boardRequest == null || !boardRequest.containsKey("name")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Board name is required"));
        }

        String boardName = boardRequest.get("name");

        try {
            String jwtToken = token.substring(7); // Remove "Bearer " prefix
            BoardDTO boardDTO = boardService.createBoard(boardName, jwtToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(boardDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create board: " + e.getMessage()));
        }
    }



    @GetMapping("")
    public ResponseEntity<?> getBoards(@RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            List<BoardDTO> boardDTOs = boardService.getBoardsForUser(jwtToken);
            if (boardDTOs.isEmpty()) {
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.ok(boardDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized access"));
        }
    }


    @PatchMapping("/{id}")
    public ResponseEntity<?> updateBoardVisibility(@PathVariable String id,
                                                   @RequestBody(required = false) Map<String, String> updateRequest,
                                                   @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            BoardDTO updatedBoard = boardService.updateBoardVisibility(id, updateRequest, jwtToken);
            return ResponseEntity.ok(updatedBoard);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update board visibility: " + e.getMessage()));
        }
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable String id,
                                         @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            boardService.deleteBoard(id, jwtToken);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete board: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getTasksForBoard(@PathVariable String id,
                                              @RequestHeader(value = "Authorization", required = false) String token,
                                              @RequestParam(required = false) String sortBy,
                                              @RequestParam(required = false) List<String> filterStatuses) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            List<Task2DTO> tasks = boardService.getTasksForBoard(id, jwtToken, sortBy, filterStatuses);
            return ResponseEntity.ok(tasks);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBoardById(@PathVariable String id, @RequestHeader(value = "Authorization", required = false) String token) {

        try {
            String jwtToken = token != null ? token.substring(7) : null;
            BoardDTO boardDTO = boardService.getBoardById(id, jwtToken);
            return ResponseEntity.ok(boardDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }
    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> createTask(@PathVariable String id,
                                        @RequestPart(required = false) NewTask2DTO newTaskDTO,
                                        @RequestPart(required = false) List<MultipartFile> addAttachments,
                                        @RequestParam(required = false) List<Integer> deleteAttachments,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            NewTask2DTO createdTask = boardService.createTask(id, newTaskDTO, jwtToken, addAttachments, deleteAttachments);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create task: " + e.getMessage()));
        }
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> updateTask(
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestPart(required = false) NewTask2DTO updateTaskDTO,
            @RequestPart(required = false) List<MultipartFile> addAttachments,
            @RequestParam(required = false) List<Integer> deleteAttachments,
            @RequestHeader(value = "Authorization", required = false) String token) {

        try {
            String jwtToken = token != null ? token.substring(7) : null;

            // First update the basic task information
            boardService.updateTask(boardId, taskId, updateTaskDTO, jwtToken);

            // Get fresh task instance to handle attachments
            TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, boardRepository.findById(boardId).get())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

            // Handle attachment deletions if any
            if (deleteAttachments != null && !deleteAttachments.isEmpty()) {
                for (Integer attachmentId : deleteAttachments) {
                    taskAttachmentService.deleteAttachment(task, attachmentId);
                }
            }


            List<AttachmentDTO> newAttachments = null;
            if (addAttachments != null && !addAttachments.isEmpty()) {
                newAttachments = addAttachments.stream()
                        .map(file -> taskAttachmentService.validateAndAddAttachment(task, file))
                        .collect(Collectors.toList());
            }

            // Convert updated task with latest attachments to DTO
            NewTask2DTO finalUpdatedTask = boardService.convertToNewTaskDTO(task);

            // If new attachments were added, include them in the response
            if (newAttachments != null && !newAttachments.isEmpty()) {
                // Assuming NewTask2DTO has a method to set attachments or a constructor that can include them
                finalUpdatedTask.setAttachments(newAttachments);
            }

            return ResponseEntity.ok(finalUpdatedTask);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update task: " + e.getMessage()));
        }
    }
    @GetMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable String boardId, @PathVariable Integer taskId,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            Task2IdDTO taskDTO = boardService.getTaskById(boardId, taskId, jwtToken);
            return ResponseEntity.ok(taskDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }






    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable String boardId,
                                        @PathVariable Integer taskId,
                                        @RequestHeader("Authorization") String token) {
        // Extract the JWT token
        String jwtToken = token.substring(7);
        try {
            // Attempt to delete the task
            boardService.deleteTask(boardId, taskId, jwtToken);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            // Handle specific exceptions (like board not found)
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            // Handle any other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete task: " + e.getMessage()));
        }
    }




}