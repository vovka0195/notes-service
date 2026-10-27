package com.example.notes.model;

import java.time.Instant;
import java.util.Set;

/** Доменная модель заметки. Иммутабельна: изменение = создание нового экземпляра. */
public record Note(Long id, String title, String content, Instant createdAt, Set<String> tags) {

    public Note withId(Long newId) {
        return new Note(newId, title, content, createdAt, tags);
    }
}
