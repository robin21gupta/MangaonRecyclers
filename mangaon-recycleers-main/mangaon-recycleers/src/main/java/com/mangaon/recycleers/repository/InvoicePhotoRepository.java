package com.mangaon.recycleers.repository;

import com.mangaon.recycleers.model.InvoicePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoicePhotoRepository extends JpaRepository<InvoicePhoto, Long> {

    List<InvoicePhoto> findByEntryTypeAndEntryId(String entryType, Long entryId);

    Optional<InvoicePhoto> findTopByEntryTypeAndEntryIdOrderByUploadedAtDesc(
            String entryType, Long entryId);

    void deleteByEntryTypeAndEntryId(String entryType, Long entryId);
}