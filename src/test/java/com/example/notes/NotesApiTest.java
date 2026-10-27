package com.example.notes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NotesApiTest {
    @Autowired private MockMvc mvc;

    @Test
    void fullLifecycle() throws Exception {
        String location = mvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Plan\",\"content\":\"Text\",\"tags\":[\" Work \"]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andReturn().getResponse().getHeader("Location");
        mvc.perform(get(location)).andExpect(status().isOk());
        mvc.perform(get("/notes").param("tag", "WORK"))
                .andExpect(jsonPath("$[0].title").value("Plan"));
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Updated\",\"content\":\"Done\",\"tags\":[]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated"));
        mvc.perform(get("/notes").param("tag", "work")).andExpect(jsonPath("$").isEmpty());
        mvc.perform(delete(location)).andExpect(status().isNoContent());
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\" \",\"content\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }
}
