package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.services.StatusV3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/boards/{boardId}/statuses")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173"})
public class StatusV3Controller {

    @Autowired
    private StatusV3Service statusService;

    @GetMapping("")
    public ResponseEntity<?> getAllStatuses(@PathVariable String boardId) {
        try {
            List<StatusDTO> statuses = statusService.findAllStatusesByBoardId(boardId);
            return ResponseEntity.ok(statuses);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> createStatus(@PathVariable String boardId, @RequestBody Status status) {
        try {
            StatusDTO createdStatus = statusService.createNewStatus(status, boardId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStatus);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<?> updateStatus(@PathVariable String boardId, @PathVariable Integer statusId, @RequestBody Status status) {
        try {
            StatusDTO updatedStatus = statusService.updateStatus(boardId, statusId, status);
            return ResponseEntity.ok(updatedStatus);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @DeleteMapping("/{statusId}")
    public ResponseEntity<?> deleteStatus(@PathVariable String boardId, @PathVariable Integer statusId) {
        try {
            statusService.deleteStatus(boardId, statusId);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @DeleteMapping("/{statusId}/{newStatusId}")
    public ResponseEntity<?> deleteStatusAndTransferTasks(@PathVariable String boardId,
                                                          @PathVariable Integer statusId,
                                                          @PathVariable Integer newStatusId) {
        try {
            statusService.deleteStatusAndTransferTasks(boardId, statusId, newStatusId);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }
}