package com.example.integradeproject;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                //กำหนด URL pattern ที่จะจับให้เป็น resource URL เช่น /uploads/ จะเข้าถึงไฟล์ที่อยู่ในไดเรกทอรี uploads/ ในระบบไฟล์ของเครื่อง
                .addResourceLocations("file:uploads/");
        //กำหนดที่ตั้งของ resource ที่จะให้บริการ ซึ่งในที่นี้คือ uploads/ ในเครื่องเซิร์ฟเวอร์ (ไม่ใช่ใน classpath) โดยใช้ file: เพื่อบอกว่าเป็นไฟล์ในระบบ
    }
}
