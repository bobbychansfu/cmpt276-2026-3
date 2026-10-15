package com.example.demo.models;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {
    // List<User> findByName(String name);
    // List<User> findByNameAndPassword(String name, String password);
}
