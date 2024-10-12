package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.CollabDTO;
import com.example.integradeproject.project_management.pm_entities.Board;
import com.example.integradeproject.project_management.pm_entities.BoardCollaboratorsId;
import com.example.integradeproject.project_management.pm_entities.Collab;
import com.example.integradeproject.project_management.pm_entities.PMUser;
import com.example.integradeproject.project_management.pm_repositories.BoardRepository;
import com.example.integradeproject.project_management.pm_repositories.CollabRepository;
import com.example.integradeproject.project_management.pm_repositories.PMUserRepository;
import com.example.integradeproject.security.JwtTokenUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollabService {

    @Autowired
    private CollabRepository collabRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private PMUserRepository pmUserRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public List<CollabDTO> getCollaboratorsForBoard(String boardId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getVisibility() == Board.BoardVisibility.PRIVATE) {
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required for private board");
            }
            String userOid = jwtTokenUtil.getUidFromToken(token);
            if (!hasAccessToBoard(board, userOid)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
            }
        }

        List<Collab> collabs = collabRepository.findByBoard(board);
        return collabs.stream()
                .map(collab -> modelMapper.map(collab, CollabDTO.class))
                .collect(Collectors.toList());
    }

    public CollabDTO getCollaboratorById(String boardId, String collabOid, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getVisibility() == Board.BoardVisibility.PRIVATE) {
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required for private board");
            }
            String userOid = jwtTokenUtil.getUidFromToken(token);
            if (!hasAccessToBoard(board, userOid)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
            }
        }

        PMUser user = pmUserRepository.findByOid(collabOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Collab collab = collabRepository.findByBoardAndOid(board, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found"));

        return modelMapper.map(collab, CollabDTO.class);
    }

    public CollabDTO addCollaborator(String boardId, String email, Collab.AccessRight accessRight, String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        String userOid = jwtTokenUtil.getUidFromToken(token);
        if (!board.getOwnerOid().getOid().equals(userOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only board owner can add collaborators");
        }

        PMUser user = pmUserRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if the user is trying to add themselves
        if (user.getOid().equals(userOid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot add yourself as a collaborator");
        }

        // Check if the user is already a collaborator
        if (collabRepository.findByBoardAndOid(board, user).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a collaborator");
        }


        Collab collab = new Collab();
        BoardCollaboratorsId id = new BoardCollaboratorsId(boardId, user.getOid());
        collab.setId(id);
        collab.setBoard(board);
        collab.setOid(user);
        collab.setEmail(email);
        collab.setAccess_right(accessRight);
        collab.setName(user.getName());

        Collab savedCollab = collabRepository.save(collab);
        return modelMapper.map(savedCollab, CollabDTO.class);
    }

    public CollabDTO updateCollaborator(String boardId, String collabOid, Collab.AccessRight accessRight, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        String userOid = jwtTokenUtil.getUidFromToken(token);
        if (!board.getOwnerOid().getOid().equals(userOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only board owner can update collaborators");
        }

        PMUser user = pmUserRepository.findByOid(collabOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Collab collab = collabRepository.findByBoardAndOid(board, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found"));

        collab.setAccess_right(accessRight);
        Collab updatedCollab = collabRepository.save(collab);
        return modelMapper.map(updatedCollab, CollabDTO.class);
    }

    public void removeCollaborator(String boardId, String collabOid, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        String userOid = jwtTokenUtil.getUidFromToken(token);
        if (!board.getOwnerOid().getOid().equals(userOid) && !userOid.equals(collabOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only board owner or the collaborator themselves can remove a collaborator");
        }

        PMUser user = pmUserRepository.findByOid(collabOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Collab collab = collabRepository.findByBoardAndOid(board, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found"));

        collabRepository.delete(collab);
    }
    private boolean hasAccessToBoard(Board board, String userOid) {
        if (board.getOwnerOid().getOid().equals(userOid)) {
            return true;
        }

        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
        return collaboration.isPresent() &&
                (collaboration.get().getAccess_right() == Collab.AccessRight.READ ||
                        collaboration.get().getAccess_right() == Collab.AccessRight.WRITE);
    }
}
