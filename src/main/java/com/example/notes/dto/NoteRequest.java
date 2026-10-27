package com.example.notes.dto;

import java.util.Set;

/** Входной DTO для создания и обновления заметки. */
public record NoteRequest(String title, String content, Set<String> tags) {
}
