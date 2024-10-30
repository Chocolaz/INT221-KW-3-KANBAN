package com.example.integradeproject.controllers;

import com.example.integradeproject.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173","https://ip23kw3.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th", "https://localhost:5174"})


@RestController
public class JwtController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/decode-token")
    public Map<String, Object> decodeToken(@RequestHeader("Authorization") String token) {
        // Remove the "Bearer " prefix from the token
        token = token.substring(7);

        return jwtTokenUtil.getClaimsFromToken(token);
    }
}
