package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.Theater;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheaterRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private TheaterRepository theaterRepository;

    @BeforeEach
    void setUp() {
        theaterRepository.deleteAll();

        // 1. VOX Cinemas in Cairo (Almaza)
        theaterRepository.save(Theater.builder()
                .name("VOX Cinemas")
                .branch("Almaza City Center")
                .city("Cairo")
                .address("Sheraton Al Matar, Qism El-Nozha")
                .build());

        // 2. VOX Cinemas ALSO in Cairo (Maadi) - Proving the new constraint works!
        theaterRepository.save(Theater.builder()
                .name("VOX Cinemas")
                .branch("City Centre Maadi")
                .city("Cairo")
                .address("Maadi Ring Road, Cairo")
                .build());

        // 3. Renaissance in Cairo
        theaterRepository.save(Theater.builder()
                .name("Renaissance Cinema")
                .branch("Downtown")
                .city("Cairo")
                .address("8 Emad El Din St., Down Town - Cairo")
                .build());

        // 4. Cineplex in Alexandria
        theaterRepository.save(Theater.builder()
                .name("Cineplex Green Plaza")
                .branch("Semouha")
                .city("Alexandria")
                .address("14th Of May Rd., Semouha, Alexandria")
                .build());
    }

    @Test
    void findByCity_shouldReturnAllTheatersInCity_whenCityMatchesExactly() {
        List<Theater> theaters = theaterRepository.findByCity("Cairo");

        // Should return all 3 Cairo theaters (2 VOX, 1 Renaissance)
        assertThat(theaters).hasSize(3)
                .extracting(Theater::getBranch)
                .containsExactlyInAnyOrder("Almaza City Center", "City Centre Maadi", "Downtown");
    }

    @Test
    void findByCity_shouldReturnEmptyList_whenCityDoesNotExist() {
        List<Theater> theaters = theaterRepository.findByCity("Luxor");

        assertThat(theaters).isEmpty();
    }

    @Test
    void findByCityIgnoreCase_shouldReturnTheaters_regardlessOfCase() {
        List<Theater> theaters = theaterRepository.findByCityIgnoreCase("cairo");

        assertThat(theaters).hasSize(3);
    }

    @Test
    void existsByNameAndBranch_shouldReturnTrue_whenTheaterExists() {
        boolean exists = theaterRepository.existsByNameAndBranch("VOX Cinemas", "Almaza City Center");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndBranch_shouldReturnFalse_whenTheaterDoesNotExist() {
        // Correct name, wrong branch
        boolean wrongBranch = theaterRepository.existsByNameAndBranch("VOX Cinemas", "Downtown");

        // Wrong name, correct branch
        boolean wrongName = theaterRepository.existsByNameAndBranch("IMAX", "Almaza City Center");

        assertThat(wrongBranch).isFalse();
        assertThat(wrongName).isFalse();
    }

    @Test
    void findByNameAndBranch_shouldReturnTheater_whenItExists() {
        Optional<Theater> theater = theaterRepository.findByNameAndBranch("Renaissance Cinema", "Downtown");

        assertThat(theater).isPresent();
        assertThat(theater.get().getCity()).isEqualTo("Cairo");
    }

    @Test
    void save_shouldPersistTheater() {
        // Use saveAndFlush instead of just save
        Theater theater = theaterRepository.saveAndFlush(Theater.builder()
                .name("VOX Cinemas")
                .branch("Mall of Egypt")
                .city("Giza")
                .address("Al Wahat Road, 6th of October City")
                .build());

        assertThat(theater.getId()).isNotNull();
        assertThat(theater.getCreatedAt()).isNotNull(); // Now this will pass!
        assertThat(theater.getUpdatedAt()).isNotNull(); // And this will pass!
    }

    @Test
    void save_shouldNotAllowDuplicateNameAndBranch() {
        assertThatThrownBy(() -> {
            theaterRepository.save(Theater.builder()
                    .name("VOX Cinemas")          // Duplicate Name
                    .branch("Almaza City Center") // Duplicate Branch
                    .city("Alexandria")           // Even if the city is different, name+branch blocks it
                    .address("Some other address")
                    .build());
            theaterRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}