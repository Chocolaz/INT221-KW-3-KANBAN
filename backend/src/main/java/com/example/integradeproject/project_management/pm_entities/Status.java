package com.example.integradeproject.project_management.pm_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "statuses" , schema = "project_management")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statusId ;
    private String statusName;
    private String statusDescription;
    @ManyToOne
    @JoinColumn(name = "boardId", nullable = false)
    private Board boardId;

    @OneToMany(mappedBy = "statusId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task2> tasks;

    public void setName(String name) {
        this.statusName = name == null ? null : name.trim();
    }

    public void setDescription(String description) {
        this.statusDescription = description == null ? null : description.trim();
    }


}
