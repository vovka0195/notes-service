package com.example.notes.repository;

import com.example.notes.model.Note;
import java.util.List;
import java.util.Optional;

/** Абстракция хранилища (DIP): сервис зависит от интерфейса, а не от реализации. */
public interface NoteRepository {

    Note save(Note note);

    Optional<Note> findById(Long id);

    List<Note> findAll();

    List<Note> findByTag(String tag);

    /** @return true, если заметка существовала и была удалена */
    boolean deleteById(Long id);
}
