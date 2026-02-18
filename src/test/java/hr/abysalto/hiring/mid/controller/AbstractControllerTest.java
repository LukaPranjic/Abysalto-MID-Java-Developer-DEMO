package hr.abysalto.hiring.mid.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.abysalto.hiring.mid.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Abstract base class for controller integration tests.
 * Provides common setup, dependencies, and utilities for testing REST endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoSpyBean
    protected UserRepository userRepository;
}

