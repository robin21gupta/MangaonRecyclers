package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.enums.Role;
import com.mangaon.recycleers.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findByRole(Role role);

    // ✅ ADD THIS (VERY IMPORTANT)
    boolean existsByRole(Role role);
}