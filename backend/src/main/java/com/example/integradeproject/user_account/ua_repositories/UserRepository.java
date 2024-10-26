package com.example.integradeproject.user_account.ua_repositories;

import com.example.integradeproject.user_account.ua_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

    User findByOid(String ownerId);

    Optional<Object> findByEmail(String email);
}