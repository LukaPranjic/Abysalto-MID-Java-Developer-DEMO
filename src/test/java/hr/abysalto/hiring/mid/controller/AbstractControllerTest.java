package hr.abysalto.hiring.mid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.mid.client.ProductClient;
import hr.abysalto.hiring.mid.configuration.TestCacheConfig;
import hr.abysalto.hiring.mid.repository.FavouriteRepository;
import hr.abysalto.hiring.mid.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Abstract base class for controller integration tests.
 * Provides common setup, dependencies, and utilities for testing REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestCacheConfig.class)
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoSpyBean
    protected UserRepository userRepository;

    @MockitoBean
    protected ProductClient productClient;

    @Autowired
    protected FavouriteRepository favouriteRepository;

    static RequestPostProcessor authenticatedUser() {
        return authenticatedUser("testuser");
    }

    static RequestPostProcessor authenticatedUser(String username) {
        return user(username).roles("USER");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        favouriteRepository.deleteAll();
    }
}

