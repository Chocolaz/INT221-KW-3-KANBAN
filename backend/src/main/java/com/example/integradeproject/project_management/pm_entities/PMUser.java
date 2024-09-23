package com.example.integradeproject.project_management.pm_entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Data
@Table(name = "users", schema = "project_management")
public class PMUser {
    @Id
    @Column(name = "oid", unique = true, nullable = false)
    private String oid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "createdOn", updatable = false, insertable = false)
    private Date createdOn;

    @Column(name = "updatedOn", updatable = false, insertable = false)
    private Date updatedOn;

    @OneToMany(mappedBy = "ownerOid", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Board> boards;
}