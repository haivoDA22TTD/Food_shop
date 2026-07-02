package com.example.foodshop.product.config;

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
            List<Map<String, Object>> fks = jdbcTemplate.queryForList(
                "SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'reviews' " +
                "AND CONSTRAINT_TYPE = 'FOREIGN KEY'"
            );
            for (Map<String, Object> fk : fks) {
                String name = (String) fk.get("CONSTRAINT_NAME");
                jdbcTemplate.execute("ALTER TABLE reviews DROP FOREIGN KEY `" + name + "`");
                log.info("Dropped FK constraint: {}", name);
            }
        } catch (Exception e) {
            log.warn("Could not drop FK constraints: " + e.getMessage());
        }
    }
}
