package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.Screen;
import io.agx.bookmyseat.entity.Theater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreenRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Theater voxAlmaza;
    private Theater renaissanceDowntown;

    @BeforeEach
    void setUp() {
        screenRepository.deleteAll();

        // 1. Persist the parent Theaters first!
        voxAlmaza = entityManager.persistAndFlush(Theater.builder()
                .name("VOX Cinemas")
                .branch("Almaza City Center")
                .city("Cairo")
                .address("Sheraton Al Matar")
                .build());

        renaissanceDowntown = entityManager.persistAndFlush(Theater.builder()
                .name("Renaissance Cinema")
                .branch("Downtown")
                .city("Cairo")
                .address("8 Emad El Din St.")
                .build());

        // 2. Persist the Screens attached to the Theaters
        screenRepository.save(Screen.builder()
                .theater(voxAlmaza)
                .name("IMAX")
                .build());

        screenRepository.save(Screen.builder()
                .theater(voxAlmaza)
                .name("Standard 1")
                .build());

        screenRepository.save(Screen.builder()
                .theater(renaissanceDowntown)
                .name("VIP Lounge")
                .build());
    }

    @Test
    void findByTheaterId_shouldReturnScreensForSpecificTheater() {
        List<Screen> voxScreens = screenRepository.findByTheaterId(voxAlmaza.getId());

        assertThat(voxScreens).hasSize(2)
                .extracting(Screen::getName)
                .containsExactlyInAnyOrder("IMAX", "Standard 1");
    }

    @Test
    void findByTheaterId_shouldReturnEmptyList_whenTheaterHasNoScreens() {
        // Create a dummy theater with no screens
        Theater emptyTheater = entityManager.persistAndFlush(Theater.builder()
                .name("Empty Cinema")
                .branch("Nowhere")
                .city("Cairo")
                .address("123 Empty St")
                .build());

        List<Screen> screens = screenRepository.findByTheaterId(emptyTheater.getId());

        assertThat(screens).isEmpty();
    }

    @Test
    void existsByNameAndTheaterId_shouldReturnTrue_whenScreenExistsInTheater() {
        boolean exists = screenRepository.existsByNameAndTheaterId("IMAX", voxAlmaza.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndTheaterId_shouldReturnFalse_whenScreenDoesNotExistInTheater() {
        // "IMAX" exists, but not in the Renaissance theater
        boolean exists = screenRepository.existsByNameAndTheaterId("IMAX", renaissanceDowntown.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldPersistScreen_whenLinkedToValidTheater() {
        Screen screen = screenRepository.saveAndFlush(Screen.builder()
                .theater(voxAlmaza)
                .name("MAX Screen 3")
                .build());

        assertThat(screen.getId()).isNotNull();
        assertThat(screen.getCreatedAt()).isNotNull();
        assertThat(screen.getTheater().getName()).isEqualTo("VOX Cinemas");
    }

    @Test
    void save_shouldFail_whenTheaterIsNull() {
        assertThatThrownBy(() -> {
            screenRepository.saveAndFlush(Screen.builder()
                    .theater(null) // Violates nullable = false
                    .name("Orphan Screen")
                    .build());
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}