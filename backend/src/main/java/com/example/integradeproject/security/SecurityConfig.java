package com.example.integradeproject.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login","/token").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v3/boards/**").permitAll()
                        .anyRequest().authenticated() //ต้องพิสูจน์ตัวตนสำหรับคำขออื่น ๆ
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                //sessionManagement: ตั้งค่านโยบายการสร้างเซสชันเป็น STATELESS
                //Stateless หมายถึงไม่มีการเก็บสถานะเซสชันบนเซิร์ฟเวอร์ เพราะ JWT จะถูกส่งในแต่ละคำขอ
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                );
        //exceptionHandling: ใช้ jwtAuthenticationEntryPoint เพื่อจัดการข้อผิดพลาดกรณีไม่มีสิทธิ์ (401 Unauthorized)

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // เพิ่มฟิลเตอร์ JwtRequestFilter ก่อนฟิลเตอร์ UsernamePasswordAuthenticationFilter เพื่อแยกและตรวจสอบ JWT ในคำขอ
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
    //ใช้ Argon2PasswordEncoder สำหรับการเข้ารหัสรหัสผ่าน:
}