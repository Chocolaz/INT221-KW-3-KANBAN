package com.example.integradeproject.project_management.pm_dtos;

import com.example.integradeproject.project_management.pm_entities.Collab;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@Data
public class CollabDTO {
    private String oid;
    private String name;
    private String email;
    private Collab.AccessRight access_right;
    private Date added_on;

}
