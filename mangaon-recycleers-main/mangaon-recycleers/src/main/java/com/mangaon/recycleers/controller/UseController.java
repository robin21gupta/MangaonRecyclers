package com.mangaon.recycleers.controller;

import com.mangaon.recycleers.model.Use;
import com.mangaon.recycleers.service.UseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/uses")
public class UseController {

    private final UseService useService;

    public UseController(UseService useService) {
        this.useService = useService;
    }

    // GET /api/uses
    @GetMapping
    public ResponseEntity<List<Use>> getAll() {
        return ResponseEntity.ok(useService.getAll());
    }

    // GET /api/uses/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(useService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/uses/search?q=...
    @GetMapping("/search")
    public ResponseEntity<List<Use>> search(@RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(useService.search(q));
    }

    // POST /api/uses
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(useService.create(body));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/uses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(useService.update(id, body));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/uses/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            useService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}