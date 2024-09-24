package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_entities.PMUser;
import com.example.integradeproject.project_management.pm_repositories.PMUserRepository;
import com.example.integradeproject.security.JwtTokenUtil;
import com.example.integradeproject.user_account.ua_dtos.UserLoginDTO;
import com.example.integradeproject.user_account.ua_entities.User;
import com.example.integradeproject.user_account.ua_repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173"})
@AllArgsConstructor
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PMUserRepository pmUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        if (refreshToken != null && refreshToken.startsWith("Bearer ")) {
            refreshToken = refreshToken.substring(7);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing refresh_token in request header"));
        }

        if (jwtTokenUtil.validateToken(refreshToken)) {
            String oid = jwtTokenUtil.getUidFromToken(refreshToken);
            User user = userRepository.findByOid(oid);

            if (user != null) {
                String newAccessToken = jwtTokenUtil.generateAccessToken(user);
                return ResponseEntity.ok(Map.of("access_token", newAccessToken));
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token is invalid or expired"));
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO loginRequest) {
        List<String> errors = new ArrayList<>();

        // Validation logic remains the same...

        User user = userRepository.findByUsername(loginRequest.getUserName());

        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            // Check if PMUser exists, if not create one
            PMUser pmUser = pmUserRepository.findByOid(user.getOid()).orElse(null);
            if (pmUser == null) {
                pmUser = new PMUser();
                pmUser.setOid(user.getOid());
                pmUser.setName(user.getName());
                pmUser.setUsername(user.getUsername());
                pmUser.setEmail(user.getEmail());
                pmUserRepository.save(pmUser);
            }

            // Generate JWT tokens
            String accessToken = jwtTokenUtil.generateAccessToken(user);
            String refreshToken = jwtTokenUtil.generateRefreshToken(user);

            return ResponseEntity.ok(Map.of(
                    "access_token", accessToken,
                    "refresh_token", refreshToken
            ));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "The username or password is incorrect."));
        }
}
}