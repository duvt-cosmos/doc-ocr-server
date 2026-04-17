package com.example.dococrserver.service;

import com.example.dococrserver.entity.User;
import com.example.dococrserver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class UserServiceTest {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @Test
    void testCreateAndRetrieveUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .build();

        User savedUser = userService.createUser(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();

        User retrieved = userService.getUserById(savedUser.getId()).orElseThrow();
        assertThat(retrieved.getUsername()).isEqualTo("testuser");
    }

    @Test
    void testUserVersioning() {
        User user = User.builder().username("vuser").email("v@ex.com").build();
        User saved = userService.createUser(user);

        // Force flush to database to ensure version is initialized and persisted
        userRepository.saveAndFlush(saved);
        Long version1 = saved.getVersion();

        saved.setFullName("Updated Name");
        User updated = userService.updateUser(saved.getId(), saved);

        // Flush the update to trigger the version increment
        userRepository.saveAndFlush(updated);

        assertThat(updated.getVersion()).isGreaterThan(version1);
    }
}
