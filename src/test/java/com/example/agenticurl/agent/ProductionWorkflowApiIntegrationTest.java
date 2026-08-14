package com.example.agenticurl.agent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:productionapitest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ProductionWorkflowApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void normalWorkflowApiDoesNotRequireFaultOptions() throws Exception {
        mockMvc.perform(
                        post("/api/workflows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "requirement": "Add expiration support to short URLs"
                                        }
                                        """)
                )
                .andExpect(status().isOk());
    }

    @Test
    void demoEndpointIsUnavailableWithoutDemoProfile() throws Exception {
        mockMvc.perform(
                        post("/api/demo/workflows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "requirement": "Add expiration support to short URLs"
                                        }
                                        """)
                )
                .andExpect(status().isNotFound());
    }
}
