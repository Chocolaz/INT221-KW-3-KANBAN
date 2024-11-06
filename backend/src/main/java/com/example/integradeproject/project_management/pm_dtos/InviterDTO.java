package com.example.integradeproject.project_management.pm_dtos;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class InviterDTO {
 private String oid ;
 private String ownerName ;
 private String boardName ;
 private String access_right ;
 private String invitation ;
}
