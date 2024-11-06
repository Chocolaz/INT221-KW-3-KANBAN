package com.example.integradeproject.project_management.pm_repositories;

import com.example.integradeproject.project_management.pm_entities.Attachment;
import com.example.integradeproject.project_management.pm_entities.Status;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository  extends JpaRepository<Attachment, Integer> {

}
