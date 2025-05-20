package com.artemisia_corp.artemisia.controller;

import com.artemisia_corp.artemisia.entity.Worker;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerDeleteDto;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerRequestDto;
import com.artemisia_corp.artemisia.entity.dto.worker.WorkerUpdateDto;
import com.artemisia_corp.artemisia.service.WorkerService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/workers")
public class WorkerController {
    private final WorkerService workerService;

    @GetMapping()
    public ResponseEntity<List<Worker>> list() {
        try {
            return ResponseEntity.ok(workerService.listAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<List<Void>> save(@RequestBody WorkerRequestDto dto) {
        try {
            workerService.save(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<List<Void>> delete(@RequestBody WorkerDeleteDto dto) {
        try {
            workerService.delete(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<List<Void>> update(@RequestBody WorkerUpdateDto dto) {
        try {
            workerService.update(dto);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
