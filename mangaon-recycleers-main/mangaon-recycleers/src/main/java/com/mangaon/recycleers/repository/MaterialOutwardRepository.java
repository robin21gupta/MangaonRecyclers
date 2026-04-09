package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.MaterialOutward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialOutwardRepository extends JpaRepository<MaterialOutward, Long> {

    @Query("SELECT m FROM MaterialOutward m LEFT JOIN FETCH m.client")
    List<MaterialOutward> findAllWithAssociations();

    @Query("SELECT m FROM MaterialOutward m LEFT JOIN FETCH m.client WHERE m.id = :id")
    Optional<MaterialOutward> findByIdWithAssociations(@Param("id") Long id);
}