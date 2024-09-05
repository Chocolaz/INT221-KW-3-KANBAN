package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
import com.example.integradeproject.project_management.pm_entities.Status;
import com.example.integradeproject.services.StatusService;
import com.example.integradeproject.services.StatusV3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v3/boards/{boardId}/statuses")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173"})
public class StatusV3Controller {
    @Autowired
    private StatusV3Service statusService;

    @GetMapping("")
    public ResponseEntity<List<Status>> getAllStatuses(@PathVariable String boardId) {
        List<Status> statuses = statusService.findAllStatusesForBoard(boardId);
        return ResponseEntity.ok(statuses);
    }

    @PostMapping("")
    public ResponseEntity<Status> createStatus(@PathVariable String boardId, @RequestBody Status status) {
        Status newStatus = statusService.createNewStatus(boardId, status);
        return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
    }

    @PutMapping("/{statusId}")
    public ResponseEntity<Status> updateStatus(@PathVariable String boardId, @PathVariable Integer statusId, @RequestBody Status status) {
        Status updatedStatus = statusService.updateStatus(boardId, statusId, status);
        return ResponseEntity.ok(updatedStatus);
    }

    @DeleteMapping("/{statusId}")
    public ResponseEntity<StatusDTO> removeStatus(@PathVariable String boardId, @PathVariable Integer statusId) {
        StatusDTO deletedStatusDTO = statusService.deleteStatus(boardId, statusId);
        return ResponseEntity.ok(deletedStatusDTO);
    }

    @DeleteMapping("/{statusId}/{newStatusId}")
    public ResponseEntity<Void> deleteStatusAndTransferTasks(@PathVariable String boardId, @PathVariable int statusId, @PathVariable int newStatusId) {
        statusService.deleteStatusAndTransferTasks(boardId, statusId, newStatusId);
        return ResponseEntity.ok().build();
    }
}