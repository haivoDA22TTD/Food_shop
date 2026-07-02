package com.example.foodshop.product.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaMigration {
    private static final Logger log = LoggerFactory.getLogger(SchemaMigration.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void migrate() {
        try {
            jdbcTemplate.execute(
                "ALTER TABLE reviews DROP FOREIGN KEY IF EXISTS FKqwqg1lxgahsxdspnwqfac6sv6"
            );
            log.info("Dropped old FK constraint on reviews.order_id");
        } catch (Exception e) {
            log.warn("Could not drop FK constraint (may already be gone): " + e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                "ALTER TABLE reviews MODIFY COLUMN order_id BIGINT NOT NULL"
            );
            log.info("Ensured order_id column is BIGINT NOT NULL");
        } catch (Exception e) {
            log.warn("Could not modify order_id column: " + e.getMessage());
        }
    }
}
