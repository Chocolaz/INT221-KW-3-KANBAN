package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.BoardDTO;
import com.example.integradeproject.project_management.pm_dtos.NewTask2DTO;
import com.example.integradeproject.project_management.pm_dtos.Task2DTO;
import com.example.integradeproject.project_management.pm_dtos.Task2IdDTO;
import com.example.integradeproject.security.JwtTokenUtil;
import com.example.integradeproject.services.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173"})
public class BoardController {

    @Autowired
    private BoardService boardService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

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
    public ResponseEntity<?> getBoards(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7); // Remove "Bearer " prefix

        try {
            List<BoardDTO> boardDTOs = boardService.getBoardsForUser(jwtToken);
            if (boardDTOs.isEmpty()) {
                return ResponseEntity.noContent().build();
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
        if (updateRequest == null || !updateRequest.containsKey("visibility")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Visibility is required"));
        }

        String visibility = updateRequest.get("visibility");

        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            String jwtToken = token.substring(7);
            BoardDTO updatedBoard = boardService.updateBoardVisibility(id, visibility, jwtToken);
            return ResponseEntity.ok(updatedBoard);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update board visibility: " + e.getMessage()));
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

    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> createTask(@PathVariable String id,
                                        @RequestBody(required = false) NewTask2DTO newTaskDTO,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        if (newTaskDTO == null || newTaskDTO.getTitle() == null || newTaskDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Task title is required"));
        }

        String jwtToken = token != null ? token.substring(7) : null;
        try {
            if (jwtToken == null || !boardService.isUserBoardOwner(id, jwtTokenUtil.getUidFromToken(jwtToken))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Only board owner can create tasks"));
            }
            NewTask2DTO createdTask = boardService.createTask(id, newTaskDTO, jwtToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @GetMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable String boardId, @PathVariable Integer taskId,
                                         @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            // Fetch the task
            Task2IdDTO taskDTO = boardService.getTaskById(boardId, taskId, jwtToken);
            return ResponseEntity.ok(taskDTO);
        } catch (ResponseStatusException e) {
            // Handle exceptions and return appropriate error status
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }





    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable String boardId,
                                        @PathVariable Integer taskId,
                                        @RequestBody(required = false) NewTask2DTO updateTaskDTO,
                                        @RequestHeader(value = "Authorization", required = false) String token) {
        if (updateTaskDTO == null || updateTaskDTO.getTitle() == null || updateTaskDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Task title is required"));
        }

        String jwtToken = token != null ? token.substring(7) : null;
        try {
            NewTask2DTO updatedTask = boardService.updateTask(boardId, taskId, updateTaskDTO, jwtToken);
            return ResponseEntity.ok(updatedTask);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update task: " + e.getMessage()));
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