package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByClientNameContainingIgnoreCase(String name);
}

