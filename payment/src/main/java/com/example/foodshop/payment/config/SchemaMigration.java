package com.example.foodshop.payment.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SchemaMigration {
    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            List<Map<String, Object>> checks = jdbcTemplate.queryForList(
                "SELECT conname FROM pg_constraint " +
                "JOIN pg_class ON pg_constraint.conrelid = pg_class.oid " +
                "WHERE pg_class.relname = 'payments' " +
                "AND conname LIKE '%payment_method%check%'"
            );
            for (Map<String, Object> check : checks) {
                String name = (String) check.get("conname");
                jdbcTemplate.execute("ALTER TABLE payments DROP CONSTRAINT IF EXISTS \"" + name + "\"");
                log.info("Dropped check constraint: {}", name);
            }
        } catch (Exception e) {
            log.warn("Could not drop check constraints on payments.payment_method: " + e.getMessage());
        }
    }
}
