package com.example.integradeproject.project_management.pm_dtos;

import lombok.*;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewTask2DTO {
    private Integer taskId;
    private String title;
    private String description;
    private String assignees;
    private String statusName;
    private List<AttachmentDTO> attachments;



    // เพิ่ม constructor ที่มี attachments

    // อัพเดท setters/getters ที่มีอยู่
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAssignees(String assignees) {
        this.assignees = assignees;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    // เพิ่ม methods สำหรับจัดการ attachments

}