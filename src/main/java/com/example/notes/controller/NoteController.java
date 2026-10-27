package com.example.notes.controller;

import com.example.notes.dto.NoteRequest;
import com.example.notes.dto.NoteResponse;
import com.example.notes.model.Note;
import com.example.notes.service.NoteService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<NoteResponse> create(@RequestBody NoteRequest request) {
        Note created = noteService.create(request);
        URI location = URI.create("/notes/" + created.id());
        return ResponseEntity.created(location).body(NoteResponse.from(created));
    }

    @GetMapping
    public List<NoteResponse> findAll(@RequestParam(required = false) String tag) {
        return noteService.findAll(tag).stream().map(NoteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public NoteResponse getById(@PathVariable Long id) {
        return NoteResponse.from(noteService.getById(id));
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @RequestBody NoteRequest request) {
        return NoteResponse.from(noteService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
