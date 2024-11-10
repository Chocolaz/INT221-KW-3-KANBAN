package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_entities.Attachment;
import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import com.example.integradeproject.project_management.pm_repositories.AttachmentRepository;
import com.example.integradeproject.services.TaskAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/v3/attachments")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173","https://ip23kw3.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th", "https://localhost:5174"})
public class FileServingController {

    private final Path uploadPath;
    private final AttachmentRepository attachmentRepository;
    private final TaskAttachmentService taskAttachmentService;

    @Autowired
    public FileServingController(Path uploadPath,
                                 AttachmentRepository attachmentRepository,
                                 TaskAttachmentService taskAttachmentService) {
        this.uploadPath = uploadPath;
        this.attachmentRepository = attachmentRepository;
        this.taskAttachmentService = taskAttachmentService;
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<Resource> serveFile(@PathVariable Integer attachmentId,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // Find attachment
            Attachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found"));

            // Check permissions (assuming task is in a board)
            TaskV3 task = attachment.getTask();
            Board board = task.getBoardId();

            // If token provided, validate access
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                taskAttachmentService.getTaskByBoardIdAndTaskId(board.getId(), task.getTaskId(), jwtToken);
            } else if (board.getVisibility() != Board.BoardVisibility.PUBLIC) {
                // If no token and board is private, deny access
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board attachments");
            }

            // Load file
            Path file = uploadPath.resolve(attachment.getFile());
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                // Detect content type
                String contentType = Files.probeContentType(file);
                if (contentType == null) {
                    // Try to determine content type from file extension
                    String fileName = file.getFileName().toString().toLowerCase();
                    if (fileName.endsWith(".png")) {
                        contentType = "image/png";
                    } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                        contentType = "image/jpeg";
                    } else if (fileName.endsWith(".gif")) {
                        contentType = "image/gif";
                    } else if (fileName.endsWith(".pdf")) {
                        contentType = "application/pdf";
                    } else {
                        contentType = "application/octet-stream";
                    }
                }

                // Set response headers for inline display (especially for images)
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(contentType));

                // For images, set inline disposition to display in browser
                if (contentType.startsWith("image/")) {
                    headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFile() + "\"");
                } else {
                    // For other files, set as attachment
                    headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFile() + "\"");
                }

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(resource);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not load file", e);
        }
    }
}