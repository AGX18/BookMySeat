package io.agx.bookmyseat.repository;

import io.agx.bookmyseat.BaseRepositoryTest;
import io.agx.bookmyseat.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .name("John Doe")
                .email("john@example.com")
                .passwordHash("hashedpassword")
                .build());
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        Optional<User> result = userRepository.findByEmail("john@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        assertThat(result.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<User> result = userRepository.findByEmail("notfound@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("notfound@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldPersistUser() {
        User user = userRepository.save(User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .passwordHash("hashedpassword")
                .build());

        assertThat(user.getId()).isNotNull();
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    void save_shouldNotAllowDuplicateEmail() {
        assertThatThrownBy(() -> {
            userRepository.save(User.builder()
                    .name("Another User")
                    .email("john@example.com")
                    .passwordHash("hashedpassword")
                    .build());
            userRepository.flush(); // force the insert immediately
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}