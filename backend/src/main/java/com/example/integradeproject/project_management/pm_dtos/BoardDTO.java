package com.example.integradeproject.project_management.pm_dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data

public class BoardDTO {
    private String id;  // This is now the uid
    @NotBlank(message = "Board name cannot be empty")
    @Size(max = 120, message = "Board name cannot exceed 120 characters")
    private String name;
    private PMUserDTO owner;

    @Data
    public static class PMUserDTO {
        private String oid;
        private String name;
    }
}
