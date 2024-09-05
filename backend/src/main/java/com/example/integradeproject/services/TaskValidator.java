package com.example.integradeproject.services;

import com.example.integradeproject.project_management.pm_dtos.NewTask2DTO;
import com.example.integradeproject.exceptions.InvalidTaskDataException;

public class TaskValidator {

        public static void validateNewTask2DTO(NewTask2DTO newTask2DTO) {

            StringBuilder errormessage = new StringBuilder();
            if (newTask2DTO.getTitle() == null || newTask2DTO.getTitle().trim().isEmpty())  {
                throw new InvalidTaskDataException("Title must not be null or empty");
            }


            if (newTask2DTO.getTitle().length() > 100) {
               errormessage.append("Title size must be between 0 and 100");
            }
            if (newTask2DTO.getDescription() != null && newTask2DTO.getDescription().length() > 500) {
                errormessage.append("Description size must be between 0 and 500");
            }
            if (newTask2DTO.getAssignees() != null && newTask2DTO.getAssignees().length() > 30) {
                errormessage.append("Assignees size must be between 0 and 30");
            }

        }
    }