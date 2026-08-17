package com.epam.gym;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full Spring context boot (real profile-driven DataSource + Hibernate SessionFactory, actuator,
 * custom health indicators, custom Prometheus metrics) against the {@code test} profile's isolated
 * in-memory H2 — end-to-end proof that the "convert to Spring Boot" + "enable actuator" + "custom
 * health indicators" + "custom Prometheus metrics" + "Spring profiles" pieces all wire up together,
 * not just in isolation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureObservability
class ActuatorIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void health_reportsUp_withCustomIndicators() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/actuator/health"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"status\":\"UP\"");
        assertThat(response.getBody()).contains("\"database\"");
        assertThat(response.getBody()).contains("\"trainingTypes\"");
    }

    @Test
    void prometheus_exposesCustomMetrics_afterARegistration() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"firstName\":\"Metrics\",\"lastName\":\"Probe\"}";

        ResponseEntity<String> registration = restTemplate.postForEntity(
                url("/api/trainees"), new HttpEntity<>(body, headers), String.class);
        assertThat(registration.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> prometheus = restTemplate.getForEntity(url("/actuator/prometheus"), String.class);

        assertThat(prometheus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(prometheus.getBody()).contains("gym_trainee_registrations_total");
        assertThat(prometheus.getBody()).contains("gym_training_types");
    }
}
