package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.BoardDTO;
import com.example.integradeproject.project_management.pm_dtos.NewTask2DTO;
import com.example.integradeproject.project_management.pm_dtos.Task2DTO;
import com.example.integradeproject.services.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173"})
public class BoardController {

    @Autowired
    private BoardService boardService;

    @PostMapping("")
    public ResponseEntity<?> createBoard(@RequestBody Map<String, String> boardRequest, @RequestHeader("Authorization") String token) {
        String boardName = boardRequest.get("boardName");

        if (boardName == null || boardName.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "boardName is null, empty, or length exceeds MAX-LENGTH"));
        }

        try {
            String jwtToken = token.substring(7); // Remove "Bearer " prefix
            BoardDTO boardDTO = boardService.createBoard(boardName, jwtToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(boardDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getBoardById(@PathVariable String id, @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7); // Remove "Bearer " prefix

        try {
            Optional<BoardDTO> boardDTOOptional = boardService.getBoardById(id, jwtToken);
            if (boardDTOOptional.isPresent()) {
                return ResponseEntity.ok(boardDTOOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Board not found or you do not have access to it."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve board: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getTasksForBoard(@PathVariable String id,
                                              @RequestHeader("Authorization") String token,
                                              @RequestParam(required = false) String sortBy,
                                              @RequestParam(required = false) List<String> filterStatuses) {
        String jwtToken = token.substring(7);
        try {
            List<Task2DTO> tasks = boardService.getTasksForBoard(id, jwtToken, sortBy, filterStatuses);
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve tasks: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/tasks")
    public ResponseEntity<?> createTask(@PathVariable String id,
                                        @RequestBody NewTask2DTO newTaskDTO,
                                        @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        try {
            NewTask2DTO createdTask = boardService.createTask(id, newTaskDTO, jwtToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create task: " + e.getMessage()));
        }
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable String boardId,
                                        @PathVariable Integer taskId,
                                        @RequestBody NewTask2DTO updateTaskDTO,
                                        @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        try {
            NewTask2DTO updatedTask = boardService.updateTask(boardId, taskId, updateTaskDTO, jwtToken);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update task: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable String boardId,
                                        @PathVariable Integer taskId,
                                        @RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        try {
            boardService.deleteTask(boardId, taskId, jwtToken);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete task: " + e.getMessage()));
        }
    }
}