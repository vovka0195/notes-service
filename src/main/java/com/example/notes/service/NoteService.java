package com.example.notes.service;

import com.example.notes.dto.NoteRequest;
import com.example.notes.exception.InvalidNoteException;
import com.example.notes.exception.NoteNotFoundException;
import com.example.notes.model.Note;
import com.example.notes.repository.NoteRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Бизнес-логика заметок: валидация, нормализация тегов, управление createdAt.
 * Хранение делегировано {@link NoteRepository} (SRP + DIP).
 */
@Service
public class NoteService {

    static final int MAX_TITLE_LENGTH = 200;
    static final int MAX_CONTENT_LENGTH = 10_000;
    static final int MAX_TAG_LENGTH = 50;

    private final NoteRepository repository;
    private final Clock clock;

    public NoteService(NoteRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public Note create(NoteRequest request) {
        validate(request);
        Note note = new Note(null, request.title().trim(), request.content(),
                Instant.now(clock), normalizeTags(request.tags()));
        return repository.save(note);
    }

    public Note getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoteNotFoundException(id));
    }

    /** @param tag фильтр по тегу; null — без фильтрации */
    public List<Note> findAll(String tag) {
        if (tag == null) {
            return repository.findAll();
        }
        String normalized = normalizeTag(tag);
        if (normalized.isEmpty()) {
            throw new InvalidNoteException("Tag filter must not be blank");
        }
        return repository.findByTag(normalized);
    }

    public Note update(Long id, NoteRequest request) {
        validate(request);
        Note existing = getById(id);
        Note updated = new Note(existing.id(), request.title().trim(), request.content(),
                existing.createdAt(), normalizeTags(request.tags()));
        return repository.save(updated);
    }

    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw new NoteNotFoundException(id);
        }
    }

    private void validate(NoteRequest request) {
        if (request == null) {
            throw new InvalidNoteException("Request body is required");
        }
        if (request.title() == null || request.title().isBlank()) {
            throw new InvalidNoteException("Title must not be blank");
        }
        if (request.title().trim().length() > MAX_TITLE_LENGTH) {
            throw new InvalidNoteException("Title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
        if (request.content() == null) {
            throw new InvalidNoteException("Content must not be null");
        }
        if (request.content().length() > MAX_CONTENT_LENGTH) {
            throw new InvalidNoteException("Content must not exceed " + MAX_CONTENT_LENGTH + " characters");
        }
        if (request.tags() != null) {
            for (String tag : request.tags()) {
                if (tag == null || tag.isBlank()) {
                    throw new InvalidNoteException("Tags must not be blank");
                }
                if (tag.trim().length() > MAX_TAG_LENGTH) {
                    throw new InvalidNoteException("Tag must not exceed " + MAX_TAG_LENGTH + " characters");
                }
            }
        }
    }

    private Set<String> normalizeTags(Set<String> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream().map(this::normalizeTag).collect(Collectors.toUnmodifiableSet());
    }

    private String normalizeTag(String tag) {
        return tag.trim().toLowerCase(Locale.ROOT);
    }
}
