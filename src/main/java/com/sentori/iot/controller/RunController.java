package com.sentori.iot.controller;

import com.sentori.iot.model.run.RunEntity;
import com.sentori.iot.repository.RunRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final RunRepository runRepository;

    public RunController(RunRepository runRepository) {
        this.runRepository = runRepository;
    }

    /** GET /api/runs?sort=startedAt,desc&page=0&size=20 */
    @GetMapping
    public Page<RunEntity> list(Pageable pageable) {
        return runRepository.findAll(pageable);
    }

    /** GET /api/runs/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<RunEntity> get(@PathVariable("id") UUID id) {
        return runRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public List<RunEntity> all() {
        return runRepository.findAll(Sort.by(Sort.Direction.DESC, "startedAt"));
    }
}
