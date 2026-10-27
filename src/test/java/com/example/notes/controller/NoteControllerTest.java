package com.example.notes.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.notes.exception.InvalidNoteException;
import com.example.notes.exception.NoteNotFoundException;
import com.example.notes.model.Note;
import com.example.notes.service.NoteService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    private static final Note NOTE = new Note(1L, "Title", "Body", Instant.parse("2024-01-15T10:00:00Z"), Set.of("work"));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @Test
    void postReturns201WithLocation() throws Exception {
        when(noteService.create(any())).thenReturn(NOTE);

        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Title\",\"content\":\"Body\",\"tags\":[\"work\"]}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/notes/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void postInvalidReturns400() throws Exception {
        when(noteService.create(any())).thenThrow(new InvalidNoteException("Title must not be blank"));

        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Title must not be blank"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/notes").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(noteService.getById(99L)).thenThrow(new NoteNotFoundException(99L));

        mockMvc.perform(get("/notes/99")).andExpect(status().isNotFound());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/notes/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void filterByTagReturnsList() throws Exception {
        when(noteService.findAll("work")).thenReturn(List.of(NOTE));

        mockMvc.perform(get("/notes").param("tag", "work"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tags[0]").value("work"));
    }

    @Test
    void deleteReturns204AndUnknownReturns404() throws Exception {
        mockMvc.perform(delete("/notes/1")).andExpect(status().isNoContent());

        doThrow(new NoteNotFoundException(2L)).when(noteService).delete(2L);
        mockMvc.perform(delete("/notes/2")).andExpect(status().isNotFound());
    }
}
