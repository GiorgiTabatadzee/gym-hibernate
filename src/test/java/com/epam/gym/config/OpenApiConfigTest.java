package com.epam.gym.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiConfigTest {

    @Test
    void gymOpenApi_setsMetadataAndBasicAuthScheme() {
        OpenAPI openApi = new OpenApiConfig().gymOpenApi();

        assertNotNull(openApi.getInfo());
        assertEquals("Gym API", openApi.getInfo().getTitle());
        assertTrue(openApi.getComponents().getSecuritySchemes().containsKey("basicAuth"));
        assertEquals(1, openApi.getSecurity().size());
    }
}
