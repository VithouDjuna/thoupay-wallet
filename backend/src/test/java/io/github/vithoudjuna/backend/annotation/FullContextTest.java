package io.github.vithoudjuna.backend.annotation;

import io.github.vithoudjuna.backend.config.PostgresTestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME) @Target(TYPE)
@SpringBootTest
@Import(PostgresTestConfiguration.class)
@AutoConfigureMockMvc
public @interface FullContextTest {

}
