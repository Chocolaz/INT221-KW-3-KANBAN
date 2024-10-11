package com.example.integradeproject.project_management.pm_entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BoardCollaboratorsId implements Serializable {
    private String boardId;
    private String userOid;
}
