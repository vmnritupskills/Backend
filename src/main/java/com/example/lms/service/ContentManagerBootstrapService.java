package com.example.lms.service;

import com.example.lms.dto.CmBootstrapResponseDTO;
import com.example.lms.dto.CourseSummaryDTO;
import com.example.lms.entity.ContentManager;
import com.example.lms.entity.Course;
import com.example.lms.entity.InstitutionCourseManager;
import com.example.lms.repository.ContentManagerRepository;
import com.example.lms.repository.InstitutionCourseManagerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentManagerBootstrapService {

    private final ContentManagerRepository cmRepo;
    private final InstitutionCourseManagerRepository icmRepo;

    @Transactional(readOnly = true)
    public CmBootstrapResponseDTO bootstrap(Long userId) {

        ContentManager cm = cmRepo.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("CM not found"));

        List<CourseSummaryDTO> courses =
                icmRepo.findByContentManager_Id(cm.getId())
                        .stream()
                        .map(icm -> {
                            Course c = icm.getCourse();
                            return new CourseSummaryDTO(
                                    c.getId(),
                                    c.getName(),
                                    c.getCourseCode()
                            );
                        })
                        .toList();

        return new CmBootstrapResponseDTO(
                cm.getId(),
                cm.getName(),
                courses
        );
    }
}
