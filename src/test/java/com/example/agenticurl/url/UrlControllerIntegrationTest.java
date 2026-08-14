package com.example.agenticurl.url;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:urltestdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.url-shortener.base-url=https://sho.rt"
})
@AutoConfigureMockMvc
class UrlControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createResponseContainsConfiguredApplicationBaseUrlAndRedirectWorks() throws Exception {
        String body = mockMvc.perform(post("/api/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "url": "https://www.google.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl", startsWith("https://sho.rt/")))
                .andExpect(jsonPath("$.expiresAt").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode response = objectMapper.readTree(body);
        String shortCode = response.get("shortCode").asText();

        mockMvc.perform(get("/" + shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://www.google.com"));
    }
}
