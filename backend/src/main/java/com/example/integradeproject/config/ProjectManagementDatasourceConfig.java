package com.example.integradeproject.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.integradeproject.project_management",
        entityManagerFactoryRef = "projectManagementEntityManager",
        transactionManagerRef = "projectManagementTransactionManager"
)
public class ProjectManagementDatasourceConfig {


    //สร้าง DataSourceProperties ที่โหลดค่าคอนฟิกจากไฟล์ application.properties หรือ application.yml
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.pm")
    public DataSourceProperties projectManagementDataSourceProperties() {
        return new DataSourceProperties();
    }


    //ใช้ DataSourceProperties จาก projectManagementDataSourceProperties() เพื่อสร้าง DataSource
    //ระบุประเภทของ DataSource เป็น HikariDataSource (default connection pool ของ Spring Boot)
    //คืนค่าเป็น DataSource ที่พร้อมใช้งาน
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.pm.configuration")
    public DataSource projectManagementDataSource() {
        return projectManagementDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }


    //สร้าง EntityManagerFactory สำหรับจัดการ Entity ของ Project Management
    @Bean(name = "projectManagementEntityManager")
    @Primary
    public LocalContainerEntityManagerFactoryBean projectManagementEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(projectManagementDataSource())
                .packages("com.example.integradeproject.project_management")
                .build();
    }



    //สร้าง JpaTransactionManager ซึ่งเป็นตัวจัดการ Transaction ของ JPA
    //คืนค่า TransactionManager สำหรับจัดการธุรกรรมของ Project Management

@Bean(name = "projectManagementTransactionManager")
@Primary
public PlatformTransactionManager projectManagementTransactionManager(
        @Qualifier("projectManagementEntityManager") LocalContainerEntityManagerFactoryBean projectManagementEntityManager) {
    return new JpaTransactionManager(Objects.requireNonNull(projectManagementEntityManager.getObject()));
}
}
