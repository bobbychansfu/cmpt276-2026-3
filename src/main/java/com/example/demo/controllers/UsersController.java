package com.example.demo.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.models.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.demo.models.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class UsersController {
    private List<User> users = new ArrayList<>();
    // private static int nextUid = 4; // Starting UID for new users
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        
        // hash the password before saving it to the database
        final String hashedPassword = passwordEncoder.encode(password);
        newUser.setPassword(hashedPassword);

        users.add(newUser);
        userRepository.save(newUser);

        return "redirect:/users/show";
    }

    // methods for login and logout 

    @GetMapping("/login")
    public String getLogin(Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response){
        User user = (User) session.getAttribute("session_user");
        if (user == null){
            return "users/login";
        }
        else {
            // Prevent caching
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            model.addAttribute("user",user);
            return "users/protected";
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam Map<String,String> formData, Model model, HttpServletRequest request, HttpSession session, HttpServletResponse response){
        // processing login
        String name = formData.get("name");
        String password = formData.get("password");
        List<User> userlist = userRepository.findByName(name);
        if (userlist.isEmpty()){
            return "users/login";
        }
        User user = userlist.get(0);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return "users/login";
        }
        else {
            // success
            request.getSession().setAttribute("session_user", user);
            
            // Prevent caching of protected pages
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
            
            model.addAttribute("user", user);
            return "users/protected";
        }
    }

    @GetMapping("/logout")
    public String destroySession(HttpServletRequest request, HttpServletResponse response){
        // Prevent caching of pages
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        
        // destroy the session
        request.getSession().invalidate();
        return "redirect:/login";
    }

    @GetMapping("/protected_page")
    public String getMethodName(HttpServletRequest request, HttpServletResponse response, Model model) {
        // check logged in status, if not logged in, redirect to login page
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("session_user") == null) {
            return "redirect:/login";
        }
        // get user info from session and pass to the protected page
        User user = (User) session.getAttribute("session_user");
        model.addAttribute("user", user);
        return "users/protected";
    }

}