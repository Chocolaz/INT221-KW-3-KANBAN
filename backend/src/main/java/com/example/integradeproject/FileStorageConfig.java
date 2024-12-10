package com.example.integradeproject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {
    @Value("${app.upload.dir:uploads}")  //ดึงค่าจาก app.prop
    private String uploadDir;

    //create file uploads และสร้างไดเรกทอรีนั้นหากยังไม่มี เมธอด uploadPath() คืนค่า Path ของไดเรกทอรีเพื่อให้ Spring ใช้งานในการจัดการไฟล์.
    @Bean
    public Path uploadPath() {
        try {
            Path path = Paths.get(uploadDir);
            Files.createDirectories(path);
            return path;
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
}