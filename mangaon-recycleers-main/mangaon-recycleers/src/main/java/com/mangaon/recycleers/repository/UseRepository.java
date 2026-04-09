package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.Use;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UseRepository extends JpaRepository<Use, Long> {

    @Query("SELECT u FROM Use u WHERE LOWER(u.useName) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(u.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Use> search(@Param("q") String q);
}