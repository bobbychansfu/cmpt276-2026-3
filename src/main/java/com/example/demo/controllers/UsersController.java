package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.models.User;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.models.UserRepository;

@Controller
public class UsersController {
    private List<User> users = new ArrayList<>();
    // private static int nextUid = 4; // Starting UID for new users
    private final UserRepository userRepository;

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // methods...

    @GetMapping("/users/show")
    public String showUsers(Model model) {
        users = userRepository.findAll();
        model.addAttribute("users", users);
        return "users/showall";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String name, @RequestParam String email, @RequestParam String password) {
        User newUser = new User();
        // newUser.setUid(nextUid++);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        users.add(newUser);
        userRepository.save(newUser);

        return "redirect:/users/show";
    }


}