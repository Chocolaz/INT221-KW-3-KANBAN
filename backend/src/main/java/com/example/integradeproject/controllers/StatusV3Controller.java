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
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173","https://ip23kw3.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th", "https://localhost:5174"})
public class StatusV3Controller {

    @Autowired
    private StatusV3Service statusService;

    @GetMapping("")
    public ResponseEntity<?> getAllStatuses(@PathVariable String boardId, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            List<StatusDTO> statuses = statusService.findAllStatusesByBoardId(boardId, jwtToken);
            return ResponseEntity.ok(statuses);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @GetMapping("/{statusId}")
    public ResponseEntity<?> getStatusById(
            @PathVariable String boardId,
            @PathVariable Integer statusId,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            StatusDTO statusDTO = statusService.getStatusById(boardId, statusId, jwtToken);
            return ResponseEntity.ok(statusDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createNewStatus(
            @PathVariable String boardId,
            @RequestBody(required = false) Status status,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            StatusDTO createdStatus = statusService.createNewStatus(status, boardId, jwtToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStatus);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<?> updateStatus(
            @PathVariable String boardId,
            @PathVariable Integer statusId,
            @RequestBody(required = false) Status updatedStatus,
            @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            StatusDTO updatedStatusDTO = statusService.updateStatus(boardId, statusId, updatedStatus, jwtToken);
            return ResponseEntity.ok(updatedStatusDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @DeleteMapping("/{statusId}")
    public ResponseEntity<?> deleteStatus(@PathVariable String boardId, @PathVariable Integer statusId, @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            statusService.deleteStatus(boardId, statusId, jwtToken);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }

    @DeleteMapping("/{statusId}/{newStatusId}")
    public ResponseEntity<?> deleteStatusAndTransferTasks(@PathVariable String boardId,
                                                          @PathVariable Integer statusId,
                                                          @PathVariable Integer newStatusId,
                                                          @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            statusService.deleteStatusAndTransferTasks(boardId, statusId, newStatusId, jwtToken);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", e.getReason()));
        }
    }
}

