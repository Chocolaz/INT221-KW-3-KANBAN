package com.example.integradeproject.project_management.pm_dtos;

import lombok.*;

//@Getter
//@Setter
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class NewTask2DTO {
//    private Integer taskId ;
//    private String title ;
//    private String description ;
//    private String assignees ;
//    private String  statusName ;

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


public void setTitle(String title){
    this.title = title;
}
    public void setDescription(String description){
        this.description = description;
    }
    public void setAssignees(String assignees){
        this.assignees = assignees;
    }
    public void setStatusName(String statusName){
        this.statusName = statusName;
    }



}
