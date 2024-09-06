//package com.example.integradeproject.controllers;
//
//import com.example.integradeproject.project_management.pm_dtos.StatusDTO;
//import com.example.integradeproject.project_management.pm_entities.Status;
//import com.example.integradeproject.services.ListMapper;
//import com.example.integradeproject.services.StatusService;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.HttpClientErrorException;
//
//import java.util.List;
//
//@RestController
//@CrossOrigin(origins = {"http://ip23kw3.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th" ,"http://localhost:5173"})
//
//@RequestMapping("/v2/statuses")
//public class StatusController {
//    @Autowired
//    private StatusService service;
//    @Autowired
//    private ModelMapper modelMapper;
//    @Autowired
//    private ListMapper listMapper;
//
//    @GetMapping("")
//    public List<Status> getALLStatus(){
//        return service.findAllStatus();
//    }
//    @PostMapping("")
//    public ResponseEntity<?> createStatus(@RequestBody Status status ,board board) {
//        try {
//            Status newStatus = service.createNewStatus(status);
//            return new ResponseEntity<>(newStatus, HttpStatus.CREATED);
//        } catch (HttpClientErrorException ex) {
//            return new ResponseEntity<>(ex.getMessage(), ex.getStatusCode());
//        }
//    }
//
//    @PutMapping("/{id}")
//    public  ResponseEntity<?> UpdateStatus (@RequestBody Status status , @PathVariable Integer id ){
//        try{
//            Status updateStatus  =service.updateByStatusId(id, status);
//            return  ResponseEntity.ok(updateStatus);
//
//        }catch (HttpClientErrorException e) {
//            return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
//        }
//    }
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Object> removeStatus(@PathVariable Status id) {
//        try {
//            StatusDTO deletedStatusDTO = service.deleteById(id);
//            return ResponseEntity.ok(deletedStatusDTO);
//        } catch (HttpClientErrorException ex) {
//            return new ResponseEntity<>(ex.getMessage(), ex.getStatusCode());
//        }
//    }
//
//    @DeleteMapping("/{id}/{newId}")
//    public ResponseEntity<Object> deleteStatusAndTransferTasks(@PathVariable int id, @PathVariable int newId) {
//        try {
//            service.deleteStatusAndTransferTasks(id, newId);
//            return ResponseEntity.ok("{}");
//        }catch (HttpClientErrorException e ){
//            return ResponseEntity.status(e.getStatusCode()).body(e.getStatusText());
//        }
//
//    }
//
//
//
//}
//
//
//
