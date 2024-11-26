package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.AttachmentDTO;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import com.example.integradeproject.services.TaskAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173","https://ip23kw3.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th", "https://localhost:5174"})
public class TaskAttachmentController {

    private final TaskAttachmentService taskAttachmentService;

    @Autowired
    public TaskAttachmentController(TaskAttachmentService taskAttachmentService) {
        this.taskAttachmentService = taskAttachmentService;
    }

    @PostMapping("/{boardId}/tasks/{taskId}/attachments")
    public ResponseEntity<?> addAttachment(@PathVariable String boardId,
                                           @PathVariable Integer taskId,
                                           @RequestParam("file") MultipartFile file,
                                           @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            TaskV3 task = taskAttachmentService.getTaskByBoardIdAndTaskId(boardId, taskId, jwtToken);
            AttachmentDTO attachmentDTO = taskAttachmentService.validateAndAddAttachment(task, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(attachmentDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to add attachment: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @PathVariable Integer attachmentId,
            @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            TaskV3 task = taskAttachmentService.getTaskByBoardIdAndTaskId(boardId, taskId, jwtToken);

            // This will handle both the file deletion and database update
            taskAttachmentService.deleteAttachment(task, attachmentId);

            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete attachment: " + e.getMessage()));
        }
    }
}
