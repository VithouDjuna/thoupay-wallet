package io.github.vithoudjuna.backend;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class ApplicationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.1-alpine");

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
        List<Customer> customers = jdbcTemplate.query(
                "select * from customer", (rs, rowNum) -> {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    return new Customer(name, email);
                });

        Assertions.assertEquals(1, customers.size());

        Customer customer = customers.getFirst();

        Assertions.assertEquals("Jane", customer.name);
        Assertions.assertEquals("jane@gmail.com", customer.email);
    }

    public record Customer(String name, String email) {

    }
}
