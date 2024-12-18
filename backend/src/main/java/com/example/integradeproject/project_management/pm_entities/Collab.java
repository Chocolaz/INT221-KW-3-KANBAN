package com.example.integradeproject.project_management.pm_entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;


@Getter
@Setter
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "collabs", schema = "project_management")
public class Collab {
    @EmbeddedId
    private BoardCollaboratorsId id;

    @ManyToOne
    @MapsId("boardId")
    @JoinColumn(name = "boardId", nullable = false)
    private Board board;

    @ManyToOne
    @MapsId("userOid")
    @JoinColumn(name = "userOid", nullable = false)
    private PMUser oid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_right", nullable = false)
    private AccessRight access_right;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation")
    private Invitation invitation ;


    @Column(name = "added_on", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date added_on;

    @PrePersist
    protected void onCreate() {
        added_on = new Date();
    }
    public enum AccessRight {
        READ, WRITE
    }
    public enum Invitation {
        PENDING, ACCEPTED, DECLINED
    }

}