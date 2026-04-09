package com.mangaon.recycleers.config;

import com.mangaon.recycleers.enums.Role;
import com.mangaon.recycleers.model.User;           // ✅ Fixed: was UserAccount + wrong Apache import
import com.mangaon.recycleers.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedUsers(UserRepository userRepository,  // ✅ Fixed: consistent name
                                       PasswordEncoder passwordEncoder) {
        return args -> {

            if (!userRepository.existsByUsername("master")) {
                userRepository.save(new User(               // ✅ Fixed: was new User() but saving to wrong repo var
                        "master",
                        passwordEncoder.encode("Master@123"),
                        Role.ACCOUNT
                ));
                System.out.println("✅ Seeded: master / Master@123");
            }

            if (!userRepository.existsByUsername("admin")) {
                userRepository.save(new User(
                        "admin",
                        passwordEncoder.encode("Admin@123"),
                        Role.ADMIN
                ));
                System.out.println("✅ Seeded: admin / Admin@123");
            }

            if (!userRepository.existsByUsername("operator")) {
                userRepository.save(new User(
                        "operator",
                        passwordEncoder.encode("Operator@123"),
                        Role.OPERATOR
                ));
                System.out.println("✅ Seeded: operator / Operator@123");
            }

            if (!userRepository.existsByUsername("user")) {
                userRepository.save(new User(
                        "user",
                        passwordEncoder.encode("User@123"),
                        Role.USER
                ));
                System.out.println("✅ Seeded: user / User@123");
            }
        };
    }
}