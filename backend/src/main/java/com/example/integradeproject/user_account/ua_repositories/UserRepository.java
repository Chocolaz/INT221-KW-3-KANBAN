package com.example.integradeproject.user_account.ua_repositories;

import com.example.integradeproject.user_account.ua_entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

    User findByOid(String ownerId);
}