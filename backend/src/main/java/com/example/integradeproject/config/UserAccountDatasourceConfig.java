package com.example.integradeproject.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.integradeproject.user_account",
        entityManagerFactoryRef = "userAccountEntityManager",
        transactionManagerRef = "userAccountTransactionManager"
)
public class UserAccountDatasourceConfig {

    //สร้าง DataSourceProperties โดยโหลดค่าคอนฟิกจากไฟล์ application.properties
    //คืนค่าเป็น DataSourceProperties ที่ใช้กำหนดค่าของ DataSource สำหรับฐานข้อมูล User Account

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ua")
    public DataSourceProperties userAccountDataSourceProperties() {
        return new DataSourceProperties();
    }


    //ใช้ DataSourceProperties เพื่อสร้าง DataSource
    //สร้าง DataSource สำหรับฐานข้อมูล User Account

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.ua.configuration")
    public DataSource userAccountDataSource() {
        return userAccountDataSourceProperties()
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }


    //สร้าง EntityManagerFactoryBuilder ซึ่งช่วยในการสร้าง EntityManagerFactory
    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        // Disable automatic generation of tables
        vendorAdapter.setGenerateDdl(false);
        return new EntityManagerFactoryBuilder(vendorAdapter, new HashMap<>(), null);
    }

    //สร้าง EntityManagerFactory สำหรับจัดการ Entity ของ User Account
    @Bean(name = "userAccountEntityManager")
    public LocalContainerEntityManagerFactoryBean userAccountEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(userAccountDataSource())
                .packages("com.example.integradeproject.user_account")
                .build();
    }

    //ใช้ JpaTransactionManager เพื่อจัดการธุรกรรม (Transaction) ของฐานข้อมูล User Account
    //คืนค่า TransactionManager สำหรับฐานข้อมูล User Account
    @Bean(name = "userAccountTransactionManager")
    public PlatformTransactionManager userAccountTransactionManager(
            final @Qualifier("userAccountEntityManager")
            LocalContainerEntityManagerFactoryBean userAccountEntityManager) {
        return new JpaTransactionManager(
                // Use to throw NullPointerException if userAccountEntityManagerFactory.getObject() is null
                Objects.requireNonNull(
                        userAccountEntityManager.getObject()
                )
        );
    }
}

