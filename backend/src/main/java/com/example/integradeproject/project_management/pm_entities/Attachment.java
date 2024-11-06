package com.example.integradeproject.project_management.pm_entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "attachments", schema = "project_management")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attachmentId;

    @ManyToOne
    @JoinColumn(name = "taskId", referencedColumnName = "taskId", nullable = false)
    private TaskV3 task;
    @Column(name = "file", nullable = false)
    private String file;
    @Column(name = "uploaded_on", nullable = false)
    private Date uploadedOn;
}
