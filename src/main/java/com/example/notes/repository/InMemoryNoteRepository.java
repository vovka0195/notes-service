package com.example.notes.repository;

import com.example.notes.model.Note;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** Потокобезопасное in-memory хранилище; порядок выдачи — по возрастанию id. */
@Repository
public class InMemoryNoteRepository implements NoteRepository {

    private final ConcurrentNavigableMap<Long, Note> storage = new ConcurrentSkipListMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public Note save(Note note) {
        Note toSave = note.id() == null ? note.withId(sequence.incrementAndGet()) : note;
        storage.put(toSave.id(), toSave);
        return toSave;
    }

    @Override
    public Optional<Note> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Note> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public List<Note> findByTag(String tag) {
        return storage.values().stream()
                .filter(note -> note.tags().contains(tag))
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        return storage.remove(id) != null;
    }
}
