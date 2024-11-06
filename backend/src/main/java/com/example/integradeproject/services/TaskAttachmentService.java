package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_entities.Attachment;
import com.example.integradeproject.project_management.pm_entities.TaskV3;
import com.example.integradeproject.project_management.pm_repositories.AttachmentRepository;
import com.example.integradeproject.project_management.pm_repositories.CollabRepository;
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


    @Autowired
    public TaskAttachmentService(Path uploadPath, AttachmentRepository attachmentRepository) {
        this.uploadPath = uploadPath;
        this.attachmentRepository = attachmentRepository;
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

    public void validateAndAddAttachment(TaskV3 task, MultipartFile file) {
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

        // Check max files
        if (task.getAttachments().size() >= MAX_FILES) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Maximum number of attachments (" + MAX_FILES + ") reached"
            );
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size exceeds maximum limit of 20MB"
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
            attachmentRepository.save(attachment);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save file: " + e.getMessage()
            );
        }
    }

    public void deleteAttachment(TaskV3 task, Integer attachmentId) {
        Attachment attachment = task.getAttachments().stream()
                .filter(a -> a.getAttachmentId().equals(attachmentId))
                .findFirst()
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