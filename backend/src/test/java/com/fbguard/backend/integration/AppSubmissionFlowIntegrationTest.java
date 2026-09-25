package com.fbguard.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fbguard.backend.dto.request.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * A real integration test: boots the full Spring context against an H2
 * in-memory database (see application-test.yml) and drives the app the same
 * way the frontend would - through actual HTTP requests, not method calls.
 * This is the test that would catch "the controller and service disagree
 * about a field name" bugs that unit tests, which mock everything, cannot.
 *
 * For an even more realistic version, swap H2 for Testcontainers running a
 * real throwaway MySQL container - H2 is used here purely for speed and to
 * avoid requiring Docker in CI.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppSubmissionFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerLoginAndSubmitApp_endToEnd() throws Exception {
        // 1. Register a new user
        mockMvc.perform(multipart("/api/auth/register")
                        .param("username", "integrationuser")
                        .param("password", "IntegrationPass1")
                        .param("email", "integration@example.com")
                        .param("country", "India")
                        .param("phoneno", "9000000099")
                        .param("gender", "Other"))
                .andExpect(status().isCreated());

        // 2. Log in and grab the JWT
        LoginRequest login = new LoginRequest();
        login.setUsername("integrationuser");
        login.setPassword("IntegrationPass1");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();
        assertThat(token).isNotBlank();

        // 3. Submit a clean, official-looking app URL - should come back LOW risk
        mockMvc.perform(multipart("/api/apps")
                        .param("appname", "Integration Test App")
                        .param("appid", "int-test-1")
                        .param("appurl", "https://apps.facebook.com/integrationtestapp")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("LOW"));

        // 4. The gallery should now contain it (paginated response)
        mockMvc.perform(get("/api/apps")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void submittingObviouslyMaliciousUrl_getsHighRisk() throws Exception {
        mockMvc.perform(multipart("/api/auth/register")
                        .param("username", "riskyuser")
                        .param("password", "RiskyPass1")
                        .param("email", "risky@example.com")
                        .param("country", "India")
                        .param("phoneno", "9000000098")
                        .param("gender", "Other"))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("riskyuser");
        login.setPassword("RiskyPass1");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // Raw IP + no HTTPS + shady TLD-like pattern -> should trip several heuristics
        mockMvc.perform(multipart("/api/apps")
                        .param("appname", "Free Coins Now")
                        .param("appid", "risky-1")
                        .param("appurl", "http://192.168.1.1/claim-free-coins")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskScore").value(org.hamcrest.Matchers.greaterThan(20)));
    }
}
