package io.github.vithoudjuna.backend;

import org.hamcrest.core.StringStartsWith;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
@ImportAutoConfiguration({
        SpringDocConfiguration.class,
        SpringDocWebMvcConfiguration.class,
        SpringDocConfigProperties.class
})
public class ApiEndpointTest {
    @Autowired
    MockMvc mockMvc;


    @Test
    @DisplayName("GET /v3/api-docs → assert status 200, the content type is JSON, and the body field openapi starts with 3.")
    void testGetApiDocsThenReturnHttpStatus200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.openapi").value(StringStartsWith.startsWith("3.")));
    }

    @Test
    @DisplayName("GET /does-not-exist → assert status 404, Content-Type application/problem+json, and the body has status: 404 and a title")
    void testGetNotFoundApi() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/does-not-exist"))
                .andExpect(MockMvcResultMatchers.status().is(404))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").exists());
    }
}
