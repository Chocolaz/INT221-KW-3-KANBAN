package com.example.integradeproject.project_management.pm_entities;

import io.viascom.nanoid.NanoId;
import jakarta.persistence.*;
import lombok.*;


import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor


@Table(name = "boards", schema = "project_management")
public class Board {
    @Id
    @Column(name = "boardId", unique = true, nullable = false)
    private String id;

    @Column(name = "boardName", nullable = false)
    private String name;

    @Column(name = "ownerOid", nullable = false)
    private String ownerOid;
    @OneToMany(mappedBy = "boardId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Status> statuses;

    @OneToMany(mappedBy = "boardId")
    private List<Task2> tasks;
    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = NanoId.generate(10);
        }
    }
}