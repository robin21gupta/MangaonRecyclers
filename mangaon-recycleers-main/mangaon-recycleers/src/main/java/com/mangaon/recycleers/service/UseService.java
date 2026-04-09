package com.mangaon.recycleers.service;

import com.mangaon.recycleers.model.Use;
import com.mangaon.recycleers.repository.UseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UseService {

    private final UseRepository useRepository;

    public UseService(UseRepository useRepository) {
        this.useRepository = useRepository;
    }

    public List<Use> getAll() {
        return useRepository.findAll();
    }

    public Use getById(Long id) {
        return useRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Use not found: " + id));
    }

    public List<Use> search(String q) {
        if (q == null || q.isBlank()) return useRepository.findAll();
        return useRepository.search(q.trim());
    }

    @Transactional
    public Use create(Map<String, String> body) {
        String useName = body.get("useName");
        if (useName == null || useName.isBlank())
            throw new IllegalArgumentException("useName is required");

        Use use = new Use();
        use.setUseName(useName.trim());
        use.setDescription(body.get("description"));
        return useRepository.save(use);
    }

    @Transactional
    public Use update(Long id, Map<String, String> body) {
        Use use = getById(id);
        String useName = body.get("useName");
        if (useName != null && !useName.isBlank()) use.setUseName(useName.trim());
        if (body.containsKey("description")) use.setDescription(body.get("description"));
        return useRepository.save(use);
    }

    @Transactional
    public void delete(Long id) {
        useRepository.deleteById(id);
    }
}