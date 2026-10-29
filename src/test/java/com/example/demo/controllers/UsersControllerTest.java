package com.example.demo.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.models.User;
import com.example.demo.models.UserRepository;

public class UsersControllerTest {
    private MockMvc mockMvc;

    private UsersController usersController;

    @Mock
    private UserRepository usersRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        usersController = new UsersController(usersRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(usersController).build();
    }

    @Test
    void testAddUser() throws Exception {
        mockMvc.perform(post("/users/add")
                .param("name", "Alice")
                .param("email", "alice@example.com")
                .param("password", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users/show"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("Alice", savedUser.getName());
        org.junit.jupiter.api.Assertions.assertEquals("alice@example.com", savedUser.getEmail());
        org.junit.jupiter.api.Assertions.assertNotEquals("secret", savedUser.getPassword()); // password is hashed, not stored as-is
        org.junit.jupiter.api.Assertions.assertTrue(new BCryptPasswordEncoder().matches("secret", savedUser.getPassword()));
    }

    @Test
    void testShowUsers() throws Exception {
        User user = new User(1, "Alice", "alice@example.com", "secret");
        List<User> users = List.of(user);
        when(usersRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/users/show"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/showall"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", users));

        verify(usersRepository).findAll();
    }

    @Test
    void testLoginSuccess() throws Exception {
        String hashedPassword = new BCryptPasswordEncoder().encode("secret");
        User user = new User(1, "Alice", "alice@example.com", hashedPassword);
        when(usersRepository.findByName("Alice")).thenReturn(List.of(user));

        mockMvc.perform(post("/login")
                .param("name", "Alice")
                .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/protected"))
                .andExpect(model().attribute("user", user))
                .andExpect(request().sessionAttribute("session_user", user));
    }

    @Test
    void testLoginWrongPassword() throws Exception {
        String hashedPassword = new BCryptPasswordEncoder().encode("secret");
        User user = new User(1, "Alice", "alice@example.com", hashedPassword);
        when(usersRepository.findByName("Alice")).thenReturn(List.of(user));

        mockMvc.perform(post("/login")
                .param("name", "Alice")
                .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"))
                .andExpect(request().sessionAttributeDoesNotExist("session_user"));
    }

    @Test
    void testLoginUnknownUser() throws Exception {
        when(usersRepository.findByName("Bob")).thenReturn(List.of());

        mockMvc.perform(post("/login")
                .param("name", "Bob")
                .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"))
                .andExpect(request().sessionAttributeDoesNotExist("session_user"));
    }

    @Test
    void testGetLoginWhenLoggedIn() throws Exception {
        User user = new User(1, "Alice", "alice@example.com", "hashed");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("session_user", user);

        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("users/protected"))
                .andExpect(model().attribute("user", user));
    }

    @Test
    void testGetLoginWhenLoggedOut() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"));
    }
}
