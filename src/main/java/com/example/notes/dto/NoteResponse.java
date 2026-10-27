package com.example.notes.dto;

import com.example.notes.model.Note;
import java.time.Instant;
import java.util.Set;

/** Выходной DTO: домен не «протекает» в публичный контракт API. */
public record NoteResponse(Long id, String title, String content, Instant createdAt, Set<String> tags) {

    public static NoteResponse from(Note note) {
        return new NoteResponse(note.id(), note.title(), note.content(), note.createdAt(), note.tags());
    }
}
