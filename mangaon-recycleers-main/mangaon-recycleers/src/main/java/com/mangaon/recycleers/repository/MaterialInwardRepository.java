package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.MaterialInward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialInwardRepository extends JpaRepository<MaterialInward, Long> {

    @Query("SELECT t FROM MaterialInward t LEFT JOIN FETCH t.supplier")
    List<MaterialInward> findAllWithAssociations();

    @Query("SELECT t FROM MaterialInward t LEFT JOIN FETCH t.supplier WHERE t.id = :id")
    Optional<MaterialInward> findByIdWithAssociations(@Param("id") Long id);
}