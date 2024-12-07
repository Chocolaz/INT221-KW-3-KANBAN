package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.AttachmentDTO;
import com.example.integradeproject.project_management.pm_entities.Attachment;
import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import com.example.integradeproject.project_management.pm_repositories.AttachmentRepository;
import com.example.integradeproject.project_management.pm_repositories.BoardRepository;
import com.example.integradeproject.project_management.pm_repositories.TaskV3Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class TaskAttachmentService {
    private static final int MAX_FILES = 10;
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB

    private final Path uploadPath;
    private final AttachmentRepository attachmentRepository;
    private final TaskV3Repository taskRepository;
    private final BoardRepository boardRepository;

    @Autowired
    public TaskAttachmentService(Path uploadPath, AttachmentRepository attachmentRepository, TaskV3Repository taskRepository, BoardRepository boardRepository) {
        this.uploadPath = uploadPath;
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
    }

    private AttachmentDTO convertToDTO(Attachment attachment) {
        return new AttachmentDTO(
                attachment.getAttachmentId(),
                attachment.getFile(),
                attachment.getUploadedOn()
        );
    }


    private String saveFile(MultipartFile file) {
        try {
            // Create unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

            // Create full path
            Path targetPath = uploadPath.resolve(fileName);

            // Copy file using NIO
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            String errorMessage = String.format("Failed to save file %s: %s",
                    file.getOriginalFilename(), e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorMessage,
                    e
            );
        }
    }

    private void deleteFile(String fileName) {
        try {
            Path filePath = uploadPath.resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            String errorMessage = String.format("Failed to delete file %s: %s",
                    fileName, e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    errorMessage,
                    e
            );
        }
    }

    public TaskV3 getTaskByBoardIdAndTaskId(String boardId, Integer taskId, String jwtToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        return taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    public AttachmentDTO validateAndAddAttachment(TaskV3 task, MultipartFile file) {
        // Validate file presence
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File cannot be empty"
            );
        }

        // Validate file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Filename contains invalid path sequence"
            );
        }

        // Check total attachments size
        long totalCurrentSize = task.getAttachments().stream()
                .mapToLong(attachment -> {
                    try {
                        Path filePath = uploadPath.resolve(attachment.getFile());
                        return Files.size(filePath);
                    } catch (IOException e) {
                        return 0L;
                    }
                })
                .sum();

        long newFileSize = file.getSize();
        if (totalCurrentSize + newFileSize > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total attachment size would exceed 20MB limit"
            );
        }

        // Check max files
        if (task.getAttachments().size() >= MAX_FILES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum number of attachments (" + MAX_FILES + ") reached"
            );
        }

        try {
            // Save file and create attachment
            String savedFileName = saveFile(file);

            Attachment attachment = new Attachment();
            attachment.setTask(task);
            attachment.setFile(savedFileName);
            attachment.setUploadedOn(new Date());

            task.getAttachments().add(attachment);
            attachment = attachmentRepository.save(attachment);

            return convertToDTO(attachment);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save file: " + e.getMessage()
            );
        }
    }
    public void deleteAttachment(TaskV3 task, Integer attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Attachment not found"
                ));

        // Delete physical file
        deleteFile(attachment.getFile());

        // Remove from task's attachments list
        task.getAttachments().remove(attachment);

        // Delete from database
        attachmentRepository.delete(attachment);
    }
}