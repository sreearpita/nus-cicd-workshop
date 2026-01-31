package sg.edu.nus.iss.d13revision.controllers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class DataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Setup before each test if needed
    }

    @Test
    public void testHealthCheckEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("HEALTH CHECK OK!"));
    }

    @Test
    public void testHealthCheckContentType() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    public void testVersionEndpoint() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(content().string("The actual version is 1.0.0"));
    }

    @Test
    public void testVersionContentType() throws Exception {
        mockMvc.perform(get("/version"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    public void testGetRandomNationsEndpoint() throws Exception {
        mockMvc.perform(get("/nations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[*].nationality", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].capitalCity", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].flag", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].language", everyItem(notNullValue())));
    }

    @Test
    public void testGetRandomNationsResponseStructure() throws Exception {
        String response = mockMvc.perform(get("/nations"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        
        // Verify it's an array
        assert jsonNode.isArray();
        
        // Verify array size
        assert jsonNode.size() == 10;
        
        // Verify each element has required fields
        for (JsonNode nation : jsonNode) {
            assert nation.has("nationality");
            assert nation.has("capitalCity");
            assert nation.has("flag");
            assert nation.has("language");
            assert !nation.get("nationality").asText().isEmpty();
            assert !nation.get("capitalCity").asText().isEmpty();
        }
    }

    @Test
    public void testGetRandomNationsFieldTypes() throws Exception {
        mockMvc.perform(get("/nations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nationality", isA(String.class)))
                .andExpect(jsonPath("$[0].capitalCity", isA(String.class)))
                .andExpect(jsonPath("$[0].language", isA(String.class)));
    }

    @Test
    public void testGetRandomNationsMultipleCalls() throws Exception {
        // Call the endpoint twice and verify both return data
        mockMvc.perform(get("/nations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));

        mockMvc.perform(get("/nations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }

    @Test
    public void testGetRandomCurrenciesEndpoint() throws Exception {
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[*].name", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].code", everyItem(notNullValue())));
    }

    @Test
    public void testGetRandomCurrenciesResponseStructure() throws Exception {
        String response = mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        
        // Verify it's an array
        assert jsonNode.isArray();
        
        // Verify array size
        assert jsonNode.size() == 20;
        
        // Verify each element has required fields
        for (JsonNode currency : jsonNode) {
            assert currency.has("name");
            assert currency.has("code");
            assert !currency.get("name").asText().isEmpty();
            assert !currency.get("code").asText().isEmpty();
        }
    }

    @Test
    public void testGetRandomCurrenciesFieldTypes() throws Exception {
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", isA(String.class)))
                .andExpect(jsonPath("$[0].code", isA(String.class)));
    }

    @Test
    public void testGetRandomCurrenciesMultipleCalls() throws Exception {
        // Call the endpoint twice and verify both return data
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)));

        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)));
    }

    @Test
    public void testAllEndpointsReturnSuccessStatus() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/version")).andExpect(status().isOk());
        mockMvc.perform(get("/nations")).andExpect(status().isOk());
        mockMvc.perform(get("/currencies")).andExpect(status().isOk());
    }

    @Test
    public void testInvalidEndpointReturns404() throws Exception {
        mockMvc.perform(get("/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testNationsArrayIsNotEmpty() throws Exception {
        mockMvc.perform(get("/nations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

    @Test
    public void testCurrenciesArrayIsNotEmpty() throws Exception {
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }

}
