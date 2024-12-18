package com.example.integradeproject.controllers;

import com.example.integradeproject.project_management.pm_dtos.CollabDTO;
import com.example.integradeproject.project_management.pm_dtos.InviterDTO;
import com.example.integradeproject.project_management.pm_entities.Collab;
import com.example.integradeproject.services.CollabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/boards/{boardId}/collabs")
@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th", "http://localhost:5173","https://ip23kw3.sit.kmutt.ac.th", "https://intproj23.sit.kmutt.ac.th", "https://localhost:5174"})
public class CollabController {

    @Autowired
    private CollabService collabService;

    @GetMapping("")
    public ResponseEntity<?> getCollaborators(@PathVariable String boardId,
                                              @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            List<CollabDTO> collabs = collabService.getCollaboratorsForBoard(boardId, jwtToken);
            return ResponseEntity.ok(collabs);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @GetMapping("/{collabOid}")
    public ResponseEntity<?> getCollaborator(@PathVariable String boardId,
                                             @PathVariable String collabOid,
                                             @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            String jwtToken = token != null ? token.substring(7) : null;
            CollabDTO collab = collabService.getCollaboratorById(boardId, collabOid, jwtToken);
            return ResponseEntity.ok(collab);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @PostMapping("")
    public ResponseEntity<?> addCollaborator(@PathVariable String boardId,
                                             @RequestBody Map<String, String> request,
                                             @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            CollabDTO newCollab = collabService.addCollaborator(boardId,
                    request.get("email"),
                    request.get("access_right"),
                    jwtToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(newCollab);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }

    @PatchMapping("/{collabOid}")
    public ResponseEntity<?> updateCollaborator(@PathVariable String boardId,
                                                @PathVariable String collabOid,
                                                @RequestBody Map<String, String> request,
                                                @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            CollabDTO updatedCollab = collabService.updateCollaborator(boardId, collabOid,
                    request.get("access_right"),
                    jwtToken);
            return ResponseEntity.ok(updatedCollab);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{collabOid}")
    public ResponseEntity<?> removeCollaborator(@PathVariable String boardId,
                                                @PathVariable String collabOid,
                                                @RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            collabService.removeCollaborator(boardId, collabOid, jwtToken);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/invitations")
    public ResponseEntity<?> getPendingInvitations(@RequestHeader("Authorization") String token) {
        try {
            String jwtToken = token.substring(7);
            List<InviterDTO> pendingInvitations = collabService.getPendingInvitations(jwtToken);
            return ResponseEntity.ok(pendingInvitations);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getReason()));
        }
    }
}