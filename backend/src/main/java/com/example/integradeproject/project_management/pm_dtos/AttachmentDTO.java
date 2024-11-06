package com.example.integradeproject.project_management.pm_dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private Integer attachmentId;
    private String file;
    private Date uploadedOn;
}

