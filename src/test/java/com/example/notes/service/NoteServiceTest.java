package com.example.notes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.notes.dto.NoteRequest;
import com.example.notes.exception.InvalidNoteException;
import com.example.notes.exception.NoteNotFoundException;
import com.example.notes.model.Note;
import com.example.notes.repository.NoteRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    private static final Instant NOW = Instant.parse("2024-01-15T10:00:00Z");

    @Mock
    private NoteRepository repository;

    private NoteService service;

    @BeforeEach
    void setUp() {
        service = new NoteService(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("create: проставляет createdAt, триммит title и нормализует теги")
    void createSetsCreatedAtAndNormalizesTags() {
        when(repository.save(any(Note.class))).thenAnswer(inv -> ((Note) inv.getArgument(0)).withId(1L));

        Note result = service.create(new NoteRequest("  Plan  ", "text", Set.of(" Work ", "URGENT")));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(repository).save(captor.capture());
        Note saved = captor.getValue();
        assertThat(saved.id()).isNull();
        assertThat(saved.title()).isEqualTo("Plan");
        assertThat(saved.createdAt()).isEqualTo(NOW);
        assertThat(saved.tags()).containsExactlyInAnyOrder("work", "urgent");
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("create: пустой title -> InvalidNoteException, в репозиторий не обращаемся")
    void createWithBlankTitleThrows() {
        assertThatThrownBy(() -> service.create(new NoteRequest("  ", "text", Set.of())))
                .isInstanceOf(InvalidNoteException.class)
                .hasMessageContaining("Title");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create: tags=null превращается в пустое множество")
    void createWithNullTagsUsesEmptySet() {
        when(repository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = service.create(new NoteRequest("Title", "text", null));

        assertThat(result.tags()).isEmpty();
    }

    @Test
    @DisplayName("getById: несуществующий id -> NoteNotFoundException")
    void getByIdNotFoundThrows() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(42L))
                .isInstanceOf(NoteNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("findAll: без тега возвращает все заметки")
    void findAllWithoutTagReturnsEverything() {
        List<Note> notes = List.of(new Note(1L, "a", "b", NOW, Set.of()));
        when(repository.findAll()).thenReturn(notes);

        assertThat(service.findAll(null)).isEqualTo(notes);
        verify(repository, never()).findByTag(any());
    }

    @Test
    @DisplayName("findAll: тег нормализуется (регистр, пробелы) перед запросом в репозиторий")
    void findAllWithTagNormalizesTag() {
        Note note = new Note(1L, "a", "b", NOW, Set.of("work"));
        when(repository.findByTag("work")).thenReturn(List.of(note));

        assertThat(service.findAll(" WORK ")).containsExactly(note);
    }

    @Test
    @DisplayName("findAll: пустой тег -> InvalidNoteException")
    void findAllWithBlankTagThrows() {
        assertThatThrownBy(() -> service.findAll("  "))
                .isInstanceOf(InvalidNoteException.class);
    }

    @Test
    void tagNormalizationIsIndependentOfDefaultLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            when(repository.findByTag("important")).thenReturn(List.of());

            assertThat(service.findAll("IMPORTANT")).isEmpty();
            verify(repository).findByTag("important");
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    @DisplayName("update: сохраняет id и createdAt исходной заметки")
    void updateKeepsIdAndCreatedAt() {
        Instant original = Instant.parse("2023-05-01T08:00:00Z");
        when(repository.findById(5L)).thenReturn(Optional.of(new Note(5L, "old", "old", original, Set.of("a"))));
        when(repository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        Note result = service.update(5L, new NoteRequest("new", "new content", Set.of("B")));

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.createdAt()).isEqualTo(original);
        assertThat(result.title()).isEqualTo("new");
        assertThat(result.tags()).containsExactly("b");
    }

    @Test
    @DisplayName("update: несуществующая заметка -> NoteNotFoundException без сохранения")
    void updateNotFoundThrows() {
        when(repository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(7L, new NoteRequest("t", "c", Set.of())))
                .isInstanceOf(NoteNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("delete: если репозиторий ничего не удалил -> NoteNotFoundException")
    void deleteNotFoundThrows() {
        when(repository.deleteById(9L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(9L)).isInstanceOf(NoteNotFoundException.class);
    }
}
