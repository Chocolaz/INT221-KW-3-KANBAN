package com.example.integradeproject.project_management.pm_dtos;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
public class Task2IdDTO {
        private String title;
        private String description;
        private String assignees;
        private String statusName;
        private List<AttachmentDTO> attachments;
        private Timestamp createdOn;
        private Timestamp updatedOn;

}
