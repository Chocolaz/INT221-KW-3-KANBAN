package com.example.integradeproject.project_management.pm_dtos;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@Getter
@Setter
public class Task2IdDTO {
        private String title;
        private String description;
        private String assignees;
        private String statusName;
        private Timestamp createdOn;
        private Timestamp updatedOn;

}
