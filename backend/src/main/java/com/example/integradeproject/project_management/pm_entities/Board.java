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

    @ManyToOne
    @JoinColumn(name = "ownerOid", nullable = false)
    private PMUser ownerOid;

    @OneToMany(mappedBy = "boardId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Status> statuses;

    @OneToMany(mappedBy = "boardId")
    private List<Task2> tasks;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private BoardVisibility visibility = BoardVisibility.PRIVATE;

    @Column(name = "createdOn", updatable = false, insertable = false)
    private Date createdOn;

    @Column(name = "updatedOn", updatable = false, insertable = false)
    private Date updatedOn;


    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = NanoId.generate(10);
        }
        if (this.visibility == null) {
            this.visibility = BoardVisibility.PRIVATE;
        }
    }

    public enum BoardVisibility {
        PRIVATE, PUBLIC
    }
}