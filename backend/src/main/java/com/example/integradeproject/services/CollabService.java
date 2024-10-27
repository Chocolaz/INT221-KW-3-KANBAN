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
import com.example.integradeproject.user_account.ua_entities.User;
import com.example.integradeproject.user_account.ua_repositories.UserRepository;
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
    private UserRepository userRepository;

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

    public CollabDTO addCollaborator(String boardId, String email, String accessRightStr, String token) {

        try {
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
            }

            String userOid = jwtTokenUtil.getUidFromToken(token);

            // First check if board exists to avoid unnecessary permission checks on non-existing board
            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "board not found "));

            // Check if user is owner or collaborator
            if (!isOwnerOrCollaborator(board, userOid)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            // Check if user is owner (only owners can add collaborators)
            if (!board.getOwnerOid().getOid().equals(userOid)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only board owner can add collaborators");
            }

            // After permission checks, validate the input data
            if (email == null || accessRightStr == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and access_right are required");
            }

            Collab.AccessRight accessRight;
            try {
                accessRight = Collab.AccessRight.valueOf(accessRightStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid access_right value");
            }

            // Verify user exists in user_account schema
            User accountUser = (User) userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in system"));

            // Check if user exists in PM_User, if not create them
            PMUser pmUser = pmUserRepository.findByEmail(email)
                    .orElseGet(() -> {
                        PMUser newPMUser = new PMUser();
                        newPMUser.setOid(accountUser.getOid());
                        newPMUser.setName(accountUser.getName());
                        newPMUser.setUsername(accountUser.getUsername());
                        newPMUser.setEmail(accountUser.getEmail());
                        return pmUserRepository.save(newPMUser);
                    });

            // Check if the user is trying to add themselves
            if (pmUser.getOid().equals(userOid)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot add yourself as a collaborator");
            }

            // Check if the user is already a collaborator
            if (collabRepository.findByBoardAndOid(board, pmUser).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a collaborator");
            }

            Collab collab = new Collab();
            BoardCollaboratorsId id = new BoardCollaboratorsId(boardId, pmUser.getOid());
            collab.setId(id);
            collab.setBoard(board);
            collab.setOid(pmUser);
            collab.setEmail(email);
            collab.setAccess_right(accessRight);
            collab.setName(pmUser.getName());

            Collab savedCollab = collabRepository.save(collab);
            return modelMapper.map(savedCollab, CollabDTO.class);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to add collaborator: " + e.getMessage());
        }
    }

    private boolean isOwnerOrCollaborator(Board board, String userOid) {
        // Check if user is owner
        if (board.getOwnerOid().getOid().equals(userOid)) {
            return true;
        }

        // Check if user is collaborator
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElse(null);
        if (user == null) {
            return false;
        }

        Optional<Collab> collab = collabRepository.findByBoardAndOid(board, user);
        return collab.isPresent();
    }
    public CollabDTO updateCollaborator(String boardId, String collabOid, String accessRightStr, String token) {
        // Check if board exists first - should return 404 if not found
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        String userOid = jwtTokenUtil.getUidFromToken(token);

        // Get requesting user's collaboration status
        PMUser requestingUser = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User not found"));

        Collab requestingUserCollab = collabRepository.findByBoardAndOid(board, requestingUser)
                .orElse(null);

        // Check if user is not a collaborator or has READ access
        if (requestingUserCollab != null && requestingUserCollab.getAccess_right() == Collab.AccessRight.READ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }

        if (requestingUserCollab == null && !board.getOwnerOid().getOid().equals(userOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }

        // Check for empty string access_right for write collaborator
        if (requestingUserCollab != null && requestingUserCollab.getAccess_right() == Collab.AccessRight.WRITE
                && "".equals(accessRightStr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions");
        }

        // Validate request body
        if (accessRightStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "access_right is required");
        }

        Collab.AccessRight accessRight;
        try {
            accessRight = Collab.AccessRight.valueOf(accessRightStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid access_right value");
        }

        // Get target user to update
        PMUser targetUser = pmUserRepository.findByOid(collabOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Collab collab = collabRepository.findByBoardAndOid(board, targetUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found"));

        // Only owner can update collaborators - WRITE collaborators cannot modify other collaborators
        if (requestingUserCollab != null && requestingUserCollab.getAccess_right() == Collab.AccessRight.WRITE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Write collaborators cannot modify other collaborators");
        }

        // Only owner can update collaborators
        if (!board.getOwnerOid().getOid().equals(userOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only board owner can modify collaborators");
        }

        collab.setAccess_right(accessRight);
        Collab updatedCollab = collabRepository.save(collab);
        return modelMapper.map(updatedCollab, CollabDTO.class);
    }


    public void removeCollaborator(String boardId, String collabOid, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        String userOid = jwtTokenUtil.getUidFromToken(token);

        // Check if the user is a collaborator
        Collab collab = collabRepository.findByBoardAndOid(board, pmUserRepository.findByOid(userOid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")))
                .orElse(null);

        // If the user is not the owner and not a collaborator, they can't perform this action
        if (!board.getOwnerOid().getOid().equals(userOid) && collab == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Non-collaborator cannot leave or remove collaborators from the board");
        }

        // If the user is a collaborator but trying to remove someone else
        if (collab != null && !userOid.equals(collabOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Collaborators can only remove themselves from the board");
        }

        PMUser userToRemove = pmUserRepository.findByOid(collabOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User to remove not found"));

        Collab collabToRemove = collabRepository.findByBoardAndOid(board, userToRemove)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collaborator not found"));

        collabRepository.delete(collabToRemove);
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
