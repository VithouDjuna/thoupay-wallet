package io.github.vithoudjuna.backend;

import io.github.vithoudjuna.backend.annotation.IntegrationTest;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;


@IntegrationTest
public class ApplicationIntegrationTest {
    @Autowired
    Flyway flyway;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void contextStartsAndFlywayMigrationsAreApplied() {
        // 1. Flyway's own view: at least one migration applied, none pending or failed
        MigrationInfoService info = flyway.info();
        assertThat(info.applied()).isNotEmpty();
        assertThat(info.pending()).isEmpty();

        // 2. The database's view: Flyway's history table records successful runs
        Integer result = jdbcTemplate.queryForObject("select count(*) from flyway_schema_history where version = '1' and success is true", Integer.class);

        Assertions.assertEquals(1, result);
    }
}
