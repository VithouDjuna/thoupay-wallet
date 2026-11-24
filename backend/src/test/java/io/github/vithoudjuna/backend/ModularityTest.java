package io.github.vithoudjuna.backend;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTest {
    @Test
    void modulesRespectBoundaries() {
        ApplicationModules.of(BackendApplication.class).verify();
    }
}
