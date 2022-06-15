package com.example.shardingsphere.jdbc.demo;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

public class ShardingJdbcTests {

    @Test
    void test() throws SQLException {
        // 配置第 1 个数据源
        HikariDataSource dataSource1 = new HikariDataSource();
        dataSource1.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource1.setJdbcUrl("jdbc:mysql://localhost:3306/ds0");
        dataSource1.setUsername("root");
        dataSource1.setPassword("123456");

        // 配置 t_order 表规则
        ShardingTableRuleConfiguration orderTableRuleConfig = new ShardingTableRuleConfiguration("t_order", "ds0.t_order${0..1}");
        // 配置分表策略
        orderTableRuleConfig.setTableShardingStrategy(new StandardShardingStrategyConfiguration("order_id", "tableShardingAlgorithm"));

        // 配置分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(orderTableRuleConfig);

        // 配置分表算法
        Properties tableShardingAlgorithmrProps = new Properties();
        tableShardingAlgorithmrProps.setProperty("algorithm-expression", "t_order${order_id % 2}");
        shardingRuleConfig.getShardingAlgorithms().put("tableShardingAlgorithm", new ShardingSphereAlgorithmConfiguration("INLINE", tableShardingAlgorithmrProps));

        // 创建 ShardingSphereDataSource
        DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource("ds0", dataSource1, Collections.<RuleConfiguration>singleton(shardingRuleConfig), new Properties());

        /* 查询逻辑 */

        String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
// 进入 t_order1 表
//            ps.setInt(1, 2);
//            ps.setInt(2, 1001);
// 进入 t_order0 表
            ps.setInt(1, 1);
            ps.setInt(2, 1000);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("rs = " + rs.getInt(1));
                }
            }
        }

        /* 插入逻辑 */
        String sql2 = "INSERT INTO t_order (user_id, order_id) VALUES (?,?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(sql2)) {
// 进入 t_order1 表
//            ps.setInt(1, 6);
//            ps.setInt(2, 1003);
// 进入 t_order0 表
            ps.setInt(1, 6);
            ps.setInt(2, 1004);
            int rows = ps.executeUpdate();
            System.out.println("rows = " + rows);
        }
    }

}
