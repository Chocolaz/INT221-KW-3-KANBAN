package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.*;
import com.example.integradeproject.project_management.pm_entities.*;
import com.example.integradeproject.project_management.pm_repositories.*;
import com.example.integradeproject.security.JwtTokenUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private PMUserRepository pmUserRepository;

    @Autowired
    private TaskV3Repository taskRepository;
    @Autowired
    private CollabRepository collabRepository;


    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TaskAttachmentService taskAttachmentService;



    @Transactional
    public BoardDTO createBoard(String boardName, String token) {
        if (boardName == null || boardName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name cannot be empty");
        }

        if (boardName.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Board name cannot exceed 120 characters");
        }

        String ownerOid = jwtTokenUtil.getUidFromToken(token);

        PMUser owner = pmUserRepository.findByOid(ownerOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = new Board();
        board.setName(boardName);
        board.setOwnerOid(owner);

        Board savedBoard = boardRepository.save(board);

        // Create default statuses
        List<Status> defaultStatuses = createDefaultStatuses(savedBoard);
        statusRepository.saveAll(defaultStatuses);

        return convertToDTO(savedBoard);
    }

    private List<Status> createDefaultStatuses(Board board) {
        List<String> defaultStatusNames = Arrays.asList("No Status", "To Do", "Doing", "Done");
        List<String> defaultStatusDescriptions = Arrays.asList(
                "The default status",
                "The task is included in the project",
                "The task is being worked on",
                "Finished"
        );

        List<Status> statuses = new ArrayList<>();
        for (int i = 0; i < defaultStatusNames.size(); i++) {
            Status status = new Status();
            status.setStatusName(defaultStatusNames.get(i));
            status.setStatusDescription(defaultStatusDescriptions.get(i));
            status.setBoardId(board);
            statuses.add(status);
        }
        return statuses;
    }
    public BoardDTO updateBoardVisibility(String boardId, Map<String, String> updateRequest, String token) {
        // Authorization check first (403)
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        // Check permissions first - non-collaborator check
        boolean isOwner = board.getOwnerOid().getOid().equals(userOid);
        if (!isOwner) {
            Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);

            if (collaboration.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
            }

            // READ collaborator check
            if (collaboration.get().getAccess_right() == Collab.AccessRight.READ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Read-only collaborators cannot change visibility");
            }

            // WRITE collaborator check
            if (collaboration.get().getAccess_right() == Collab.AccessRight.WRITE) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the board owner can change visibility");
            }
        }

        // Validation checks (400) only after all authorization checks pass
        if (updateRequest == null || !updateRequest.containsKey("visibility")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visibility is required");
        }

        String visibility = updateRequest.get("visibility");
        String normalizedVisibility = visibility.toLowerCase();
        if (!normalizedVisibility.equals("public") && !normalizedVisibility.equals("private")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid visibility value");
        }

        board.setVisibility(Board.BoardVisibility.valueOf(normalizedVisibility.toUpperCase()));
        Board updatedBoard = boardRepository.save(board);
        return convertToDTO(updatedBoard);
    }
    public List<BoardDTO> getBoardsForUser(String token) {
        if (token == null) {
            return boardRepository.findByVisibility(Board.BoardVisibility.PUBLIC)
                    .stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findById(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Set<Board> userBoards = new HashSet<>(boardRepository.findByOwnerOid(user));
        Set<Board> publicBoards = new HashSet<>(boardRepository.findByVisibility(Board.BoardVisibility.PUBLIC));
        Set<Board> collaborationBoards = collabRepository.findByOid(user)
                .stream()
                .filter(collab -> collab.getAccess_right() == Collab.AccessRight.WRITE
                        || collab.getAccess_right() == Collab.AccessRight.READ)
                .map(Collab::getBoard)
                .collect(Collectors.toSet());

        Set<Board> allBoards = new HashSet<>();
        allBoards.addAll(userBoards);
        allBoards.addAll(publicBoards);
        allBoards.addAll(collaborationBoards);

        return allBoards.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BoardDTO getBoardById(String id, String token) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getVisibility() == Board.BoardVisibility.PUBLIC) {
            return convertToDTO(board);
        }

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findById(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (board.getOwnerOid().getOid().equals(userOid)) {
            return convertToDTO(board);
        }

        Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
        if (collaboration.isPresent() &&
                (collaboration.get().getAccess_right() == Collab.AccessRight.READ ||
                        collaboration.get().getAccess_right() == Collab.AccessRight.WRITE)) {
            return convertToDTO(board);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
    }

    @Transactional
    public void deleteBoard(String boardId, String token) {
        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (!board.getOwnerOid().getOid().equals(userOid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the board owner can delete the board");
        }

        // Delete all tasks associated with the board
        List<TaskV3> tasks = taskRepository.findByBoardId(board);
        taskRepository.deleteAll(tasks);

        // Delete all collaborations associated with the board
        List<Collab> collabs = collabRepository.findByBoard(board);
        collabRepository.deleteAll(collabs);

        // Delete all statuses associated with the board
        List<Status> statuses = statusRepository.findByBoardId(board);
        statusRepository.deleteAll(statuses);

        // Finally, delete the board itself
        boardRepository.delete(board);
    }






    public List<Task2DTO> getTasksForBoard(String boardId, String token, String sortBy, List<String> filterStatuses) {
        // Find the board by its ID, throw an exception if not found
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        // If the board is public, return tasks without further authentication
        if (board.getVisibility() == Board.BoardVisibility.PUBLIC) {
            return getFilteredTasks(board, filterStatuses);
        }

        // For private boards, token is mandatory
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
        }

        // Extract user ID from the JWT token
        String userOid = jwtTokenUtil.getUidFromToken(token);
        // Find the user, throw an exception if not found
        PMUser user = pmUserRepository.findById(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        // If user is the board owner, allow access
        if (board.getOwnerOid().getOid().equals(userOid)) {
            return getFilteredTasks(board, filterStatuses);
        }

        // Check if user has collaboration access to the board
        Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
        // If collaboration exists with READ or WRITE access, allow access
        if (collaboration.isPresent() &&
                (collaboration.get().getAccess_right() == Collab.AccessRight.READ ||
                        collaboration.get().getAccess_right() == Collab.AccessRight.WRITE)) {
            return getFilteredTasks(board, filterStatuses);
        }

        // If no access conditions met, deny access
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
    }

    // Helper method to filter tasks based on board and optional status filters
    private List<Task2DTO> getFilteredTasks(Board board, List<String> filterStatuses) {
        List<TaskV3> tasks;
        // If filter statuses are provided, find tasks with matching statuses
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            tasks = taskRepository.findByBoardIdAndStatusId_StatusNameIn(board, filterStatuses);
        } else {
            // Otherwise, retrieve all tasks for the board
            tasks = taskRepository.findByBoardId(board);
        }
        // Convert tasks to DTOs and return the list
        return tasks.stream().map(this::convertToTaskDTO).collect(Collectors.toList());
    }
    private Task2DTO convertToTaskDTO(TaskV3 task) {
        Task2DTO taskDTO = modelMapper.map(task, Task2DTO.class);
        int attachmentCount = task.getAttachments().size();
        taskDTO.setAttachmentCount(attachmentCount > 0 ? String.valueOf(attachmentCount) : "-");
        return taskDTO;
    }

    public Task2IdDTO getTaskById(String boardId, Integer taskId, String token) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        if (board.getVisibility() == Board.BoardVisibility.PUBLIC) {
            return findAndConvertTask(board, taskId);
        }

        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findById(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (board.getOwnerOid().getOid().equals(userOid)) {
            return findAndConvertTask(board, taskId);
        }

        Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
        if (collaboration.isPresent()) {
            if (collaboration.get().getAccess_right() == Collab.AccessRight.WRITE) {
                return findAndConvertTask(board, taskId);
            } else if (collaboration.get().getAccess_right() == Collab.AccessRight.READ) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
            }
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to private board");
    }

    private Task2IdDTO findAndConvertTask(Board board, Integer taskId) {
        TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        return convertToTask2IdDTO(task);
    }

    private Task2IdDTO convertToTask2IdDTO(TaskV3 task) {
        Task2IdDTO dto = modelMapper.map(task, Task2IdDTO.class);

        List<AttachmentDTO> attachmentDTOs = task.getAttachments().stream()
                .map(attachment -> modelMapper.map(attachment, AttachmentDTO.class))
                .collect(Collectors.toList());
        dto.setAttachments(attachmentDTOs);

        return dto;
    }
    @Transactional
    public NewTask2DTO createTask(String boardId, NewTask2DTO newTaskDTO, String token, List<MultipartFile> attachments, List<Integer> deleteAttachments) {
        // Authorization check first (403)
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        // Check permissions first - non-collaborator check
        boolean isOwner = board.getOwnerOid().getOid().equals(userOid);
        if (!isOwner) {
            Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);

            if (collaboration.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
            }

            // READ collaborator check
            if (collaboration.get().getAccess_right() == Collab.AccessRight.READ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Read-only collaborators cannot create tasks");
            }
        }

        // Validation checks (400) only after all authorization checks pass
        if (newTaskDTO == null || newTaskDTO.getTitle() == null || newTaskDTO.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title is required");
        }

        // Status validation
        Status status;
        if (newTaskDTO.getStatusName() != null) {
            status = statusRepository.findByStatusNameAndBoardId(newTaskDTO.getStatusName(), board);
            if (status == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found in this board");
            }
        } else {
            status = statusRepository.findByStatusNameAndBoardId("No Status", board);
        }

        TaskV3 task = new TaskV3();
        task.setTitle(newTaskDTO.getTitle());
        task.setDescription(newTaskDTO.getDescription());
        task.setAssignees(newTaskDTO.getAssignees());
        task.setBoardId(board);
        task.setStatusId(status);
        TaskV3 savedTask = taskRepository.save(task);

        // Handle attachments if present
        if (attachments != null && !attachments.isEmpty()) {
            for (MultipartFile file : attachments) {
                taskAttachmentService.validateAndAddAttachment(savedTask, file);
            }
        }

        // Handle attachment deletions if present
        if (deleteAttachments != null && !deleteAttachments.isEmpty()) {
            for (Integer attachmentId : deleteAttachments) {
                taskAttachmentService.deleteAttachment(savedTask, attachmentId);
            }
        }

        return convertToNewTaskDTO(savedTask);
    }

    @Transactional
    public NewTask2DTO updateTask(String boardId, Integer taskId, NewTask2DTO updateTaskDTO, String token) {
        // Authorization check first (403)
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));

        // Check permissions first - non-collaborator check
        boolean isOwner = board.getOwnerOid().getOid().equals(userOid);
        if (!isOwner) {
            Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);

            if (collaboration.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this board");
            }

            // READ collaborator check
            if (collaboration.get().getAccess_right() == Collab.AccessRight.READ) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Read-only collaborators cannot update tasks");
            }
        }

        // Validation checks (400) only after all authorization checks pass
        if (updateTaskDTO == null || updateTaskDTO.getTitle() == null || updateTaskDTO.getTitle().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title is required");
        }

        TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        // Status validation
        if (updateTaskDTO.getStatusName() != null) {
            Status status = statusRepository.findByStatusNameAndBoardId(updateTaskDTO.getStatusName(), board);
            if (status == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found in this board");
            }
            task.setStatusId(status);
        }

        task.setTitle(updateTaskDTO.getTitle());
        task.setDescription(updateTaskDTO.getDescription());
        task.setAssignees(updateTaskDTO.getAssignees());

        TaskV3 updatedTask = taskRepository.save(task);
        return convertToNewTaskDTO(updatedTask);
    }

    public NewTask2DTO convertToNewTaskDTO(TaskV3 task) {
        NewTask2DTO dto = modelMapper.map(task, NewTask2DTO.class);
        dto.setStatusName(task.getStatusId().getStatusName());

        // Convert attachments
        if (task.getAttachments() != null) {
            List<AttachmentDTO> attachmentDTOs = task.getAttachments().stream()
                    .map(attachment -> new AttachmentDTO(
                            attachment.getAttachmentId(),
                            attachment.getFile(),
                            attachment.getUploadedOn()))
                    .collect(Collectors.toList());
            dto.setAttachments(attachmentDTOs);
        }

        return dto;
    }





    public void deleteTask(String boardId, Integer taskId, String token) {
        String userOid = jwtTokenUtil.getUidFromToken(token);
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found"));
        checkWriteAccess(board, token);


        TaskV3 task = taskRepository.findByTaskIdAndBoardId(taskId, board)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (board.getOwnerOid().getOid().equals(userOid)) {
            taskRepository.delete(task);
            return;
        }

        Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")));

        if (collaboration.isPresent() && collaboration.get().getAccess_right() == Collab.AccessRight.WRITE) {
            taskRepository.delete(task);
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have permission to delete tasks in this board");
    }

    private BoardDTO convertToDTO(Board board) {
        BoardDTO dto = new BoardDTO();
        dto.setId(board.getId());
        dto.setName(board.getName());
        dto.setVisibility(board.getVisibility().toString().toLowerCase());

        // Set owner information
        BoardDTO.PMUserDTO ownerDTO = new BoardDTO.PMUserDTO();
        ownerDTO.setOid(board.getOwnerOid().getOid());
        ownerDTO.setName(board.getOwnerOid().getName());
        dto.setOwner(ownerDTO);

        // Get and set collaborators
        List<Collab> collaborators = collabRepository.findByBoard(board);
        List<BoardDTO.CollaboratorDTO> collaboratorDTOs = collaborators.stream()
                .map(collab -> {
                    BoardDTO.CollaboratorDTO collabDTO = new BoardDTO.CollaboratorDTO();
                    collabDTO.setOid(collab.getOid().getOid());
                    collabDTO.setName(collab.getOid().getName());
                    collabDTO.setAccess_right(collab.getAccess_right().toString());
                    return collabDTO;
                })
                .collect(Collectors.toList());
        dto.setCollaborators(collaboratorDTOs);

        return dto;
    }






    private void checkWriteAccess(Board board, String token) {
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authentication required");
        }

        String userOid = jwtTokenUtil.getUidFromToken(token);
        PMUser user = pmUserRepository.findByOid(userOid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if user is board owner
        if (board.getOwnerOid().getOid().equals(userOid)) {
            return;
        }

        // Check collaboration access
        Optional<Collab> collaboration = collabRepository.findByBoardAndOid(board, user);
        if (collaboration.isEmpty() || collaboration.get().getAccess_right() == Collab.AccessRight.READ) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have write permission for this board");
        }
    }

}