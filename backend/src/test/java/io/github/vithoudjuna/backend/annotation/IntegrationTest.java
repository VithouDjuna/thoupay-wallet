package io.github.vithoudjuna.backend.annotation;

import io.github.vithoudjuna.backend.config.PostgresTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PostgresTestConfiguration.class)
@SpringBootTest
public @interface IntegrationTest {
}
