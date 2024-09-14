//package com.example.integradeproject.services;
//
//import com.example.integradeproject.dtos.NewTask2DTO;
//import com.example.integradeproject.dtos.StatusDTO;
//import com.example.integradeproject.dtos.Task2DTO;
//import com.example.integradeproject.project_management.pmEntitities.Status;
//import com.example.integradeproject.project_management.pmEntitities.Task2;
//import com.example.integradeproject.project_management.pmRepositories.StatusRepository;
//import com.example.integradeproject.project_management.pmRepositories.Task2Repository;
//import jakarta.transaction.Transactional;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.rest.webmvc.ResourceNotFoundException;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class Task2Service {
//
//    @Autowired
//    private Task2Repository repository;
//
//    @Autowired
//    private ModelMapper mapper;
//    @Autowired
//    ListMapper listMapper;
//    @Autowired
//    private StatusRepository statusRepository;
//
//
//    public List<Task2DTO> getTask(String sortBy, List<String> filterStatuses) {
//        List<Task2> tasks;
//        if (filterStatuses == null || filterStatuses.isEmpty()) {
//            // if not use sortby -> send default data not sort
//            if (sortBy == null || sortBy.isEmpty()) {
//                tasks = repository.findAll();
//
//
//            } else {
//                // sortBy = status.name  จะเรียงข้อมูลมาให้
//                if (sortBy.equalsIgnoreCase("status.name")) {
//                    tasks = repository.findAll(Sort.by(Sort.Direction.ASC, "status.name"));
//                } else {
//                    throw new IllegalArgumentException("Invalid sorting option");
//                }
//            }
//        } else {
//            //filter find by statusName
//            tasks = repository.findByStatusNames(filterStatuses);
//            if (sortBy != null && !sortBy.isEmpty() && sortBy.equalsIgnoreCase("status.name")) {
//                // stream ช่วยในเรื่อง การ filter หรือ map ง่ายขึ้น โค้ดกระชับ กว่า การloop ปกติ
//                tasks = tasks.stream()
//                        // comparator ช่วย sort data
//                        .sorted(Comparator.comparing(t -> t.getStatusId().getStatusId()))
//                        .collect(Collectors.toList());
//            }
//        }
//        // สร้าง stream from tasks ซึ่งเป็น List ของ Task2
//        return tasks.stream()
//                .map(task -> {
//                    Task2DTO task2DTO = mapper.map(task, Task2DTO.class);
//                    // get statusDTO
//                    StatusDTO statusDTO = mapper.map(task.getStatusId(), StatusDTO.class);
//                    task2DTO.setStatusName(String.valueOf(statusDTO.getName()));
//                    return task2DTO;
//                })
//                // รวบรวม Task2DTO ที่ถูกแปลงทั้งหมดใน stream และเก็บเป็น List
//                //ส่งคืน List ของ Task2DTO ที่ได้จากการแปลง
//                .collect(Collectors.toList());
//    }
//    @Transactional
//    public Task2 findById(Integer id){
//        return repository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"task " + id + " does not exist !! "));
//    }
//    @Transactional
//    public NewTask2DTO deleteById(Integer id) {
//        Task2 task2 = repository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Task2 with ID " + id + " not found"));
//
//        repository.deleteById(task2.getTaskId());
//        NewTask2DTO deletedTask2DTO = mapper.map(task2, NewTask2DTO.class);
//
//        return deletedTask2DTO;
//    }
//    public NewTask2DTO createTask(NewTask2DTO newTask2DTO) {
//        // Validate the title
//        if (newTask2DTO.getTitle() == null || newTask2DTO.getTitle().trim().isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be null or empty");
//        }
//
//        Task2 task = new Task2();
//        task.setTitle(newTask2DTO.getTitle());
//        task.setDescription(newTask2DTO.getDescription());
//        task.setAssignees(newTask2DTO.getAssignees());
//
//        if (newTask2DTO.getStatusName() != null) {
//            // Find the Status by name
//            Status status = statusRepository.findByStatusName(newTask2DTO.getStatusName())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status '" + newTask2DTO.getStatusName() + "' DOES NOT EXIST !!!"));
//            task.setStatusId(status);
//        }
//
//        Task2 savedTask = repository.save(task);
//        NewTask2DTO createdTaskDTO = mapper.map(savedTask, NewTask2DTO.class);
//
//        return createdTaskDTO;
//    }
//
//    //    public NewTask2DTO createTask(NewTask2DTO newTask2DTO) {
////        // Validate the title
////        if (newTask2DTO.getTitle() == null || newTask2DTO.getTitle().trim().isEmpty()) {
////            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be null or empty");
////        }
////        //สร้าง instance
////        Task2 task = new Task2();
////        task.setTitle(newTask2DTO.getTitle());
////        task.setDescription(newTask2DTO.getDescription());
////        task.setAssignees(newTask2DTO.getAssignees());
////
////        if (newTask2DTO.getStatusName() != null) {
////            try {
////                int statusId = Integer.parseInt(newTask2DTO.getStatusName());
////                // ถ้าพบ Status จะตั้งค่าให้ task
////                Status status = statusRepository.findById(statusId)
////                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status Id " + statusId + " DOES NOT EXIST !!!"));
////                task.setStatusId(status);
////            } catch (NumberFormatException e) {
////                throw new IllegalArgumentException("Invalid status ID format");
////            }
////        }
////
////
////        Task2 savedTask = repository.save(task);
////        NewTask2DTO createdTaskDTO = mapper.map(savedTask, NewTask2DTO.class);
////        createdTaskDTO.setStatusName(savedTask.getStatusId().getStatusName());
////
////        return createdTaskDTO;
////    }
//    public NewTask2DTO updateTask(Integer taskId, NewTask2DTO newTask2DTO) {
//        // Validate title
//        if (newTask2DTO.getTitle() == null || newTask2DTO.getTitle().trim().isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title cannot be null or empty");
//        }
//
//        Task2 existingTask = repository.findById(taskId)
//                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));
//
//        if (newTask2DTO.getStatusName() != null) {
//            // Find the Status entity by name
//            Status status = statusRepository.findByStatusName(newTask2DTO.getStatusName())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status '" + newTask2DTO.getStatusName() + "' DOES NOT EXIST !!!"));
//            existingTask.setStatusId(status);
//        }
//
//        existingTask.setTitle(newTask2DTO.getTitle());
//        existingTask.setDescription(newTask2DTO.getDescription());
//        existingTask.setAssignees(newTask2DTO.getAssignees());
//
//        Task2 updatedTask = repository.save(existingTask);
//
//        NewTask2DTO updatedTaskDTO = mapper.map(updatedTask, NewTask2DTO.class);
//        return updatedTaskDTO;
//    }
//}