package com.example.shardingsphere.jdbc.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ShardingsphereJdbcDemoApplicationTests {

    @Test
    void contextLoads() throws SQLException {
        assertTrue(true);
    }

}
