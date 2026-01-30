package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.lms.dto.ContentManagerStatsResponseDTO;


import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentManagerService {

    private final InstitutionRepository institutionRepository;
    private final ContentManagerRepository contentManagerRepository;
    private final CourseRepository courseRepository;
    private final InstitutionCourseManagerRepository institutionCourseManagerRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentRepository enrollmentRepository;





    /* ================= CREATE ================= */
    @Transactional
    public void createContentManager(CreateContentManagerRequestDTO dto, Long institutionId) {

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Institution not found"));

        Role role = roleRepository.findByName("CONTENT_MANAGER")
                .orElseThrow(() -> new RuntimeException("CONTENT_MANAGER role not found"));

        User user = User.builder()
                .email(dto.email())
                .name(dto.name())
                .password(passwordEncoder.encode(dto.password()))
                .role(role)
                .isActive(true)
                .build();

        userRepository.save(user);

        ContentManager cm = ContentManager.builder()
                .name(dto.name())
                .email(dto.email())
                .department(dto.department())
                .user(user)
                .institution(institution)
                .build();

        contentManagerRepository.save(cm);
    }

    /* ================= GET ================= */
    public ContentManager getContentManager(Long cmId, Long institutionId) {

        return contentManagerRepository
                .findByIdAndInstitutionId(cmId, institutionId)
                .orElseThrow(() -> new IllegalArgumentException("Content Manager not found"));
    }

    public List<ContentManagerResponseDTO> getAllContentManagers(Long institutionId) {

        return contentManagerRepository.findByInstitutionId(institutionId)
                .stream()
                .map(cm -> new ContentManagerResponseDTO(
                        cm.getId(),
                        cm.getName(),
                        cm.getEmail(),
                        cm.getDepartment(),
                        cm.getUser().getId(),
                        cm.getUser().getIsActive()
                ))
                .toList();
    }

    /* ================= UPDATE ================= */
    @Transactional
    public void updateContentManager(Long cmId, UpdateContentManagerRequestDTO dto) {

        ContentManager cm = contentManagerRepository.findById(cmId)
                .orElseThrow(() -> new IllegalArgumentException("Content Manager not found"));

        cm.setName(dto.name());
        cm.setDepartment(dto.department());
        cm.getUser().setIsActive(dto.isActive());

        userRepository.save(cm.getUser());
        contentManagerRepository.save(cm);
    }

    /* ================= DELETE ================= */
    @Transactional
    public void deleteContentManager(Long cmId, Long institutionId) {

        ContentManager cm = getContentManager(cmId, institutionId);

        institutionCourseManagerRepository.deleteByContentManagerId(cmId);
        userRepository.delete(cm.getUser());
        contentManagerRepository.delete(cm);
    }

    /* ================= ASSIGN COURSE ================= */
    @Transactional
    public void assignCourseToContentManager(Long courseId, Long cmId) {

        ContentManager cm = contentManagerRepository.findById(cmId)
                .orElseThrow(() -> new IllegalArgumentException("Content Manager not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        if (institutionCourseManagerRepository
                .existsByCourseIdAndContentManagerId(courseId, cmId)) {
            throw new IllegalStateException("Course already assigned");
        }

        InstitutionCourseManager mapping =
                InstitutionCourseManager.builder()
                        .institution(cm.getInstitution()) // ✅ REQUIRED
                        .course(course)
                        .contentManager(cm)
                        .build();

        institutionCourseManagerRepository.save(mapping);
    }


    /* ================= BULK ASSIGN ================= */
    @Transactional
    public void bulkAssignCoursesToContentManager(
            List<Long> courseIds,
            Long cmId,
            Long institutionId) {

        ContentManager cm = getContentManager(cmId, institutionId);

        for (Long courseId : courseIds) {
            assignCourseToContentManager(courseId, cm.getId());
        }
    }

    /* ================= UNASSIGN ================= */
    @Transactional
    public void unassignCourseFromContentManager(Long courseId, Long cmId) {

        InstitutionCourseManager mapping =
                institutionCourseManagerRepository
                        .findByCourseIdAndContentManagerId(courseId, cmId)
                        .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));

        institutionCourseManagerRepository.delete(mapping);
    }

    /* ================= GET ASSIGNED COURSES ================= */
    public List<Course> getCoursesAssignedToContentManager(Long cmId) {

        return institutionCourseManagerRepository
                .findByContentManagerId(cmId)
                .stream()
                .map(InstitutionCourseManager::getCourse)
                .toList();
    }
    /* ================= ADMIN STATS ================= */
    public ContentManagerStatsResponseDTO getContentManagerStats() {

        long total = contentManagerRepository.count();
        long active = contentManagerRepository.countActiveContentManagers();

        return new ContentManagerStatsResponseDTO(
                total,
                active
        );
    }
    /* ================= CONTENT MANAGER STUDENT COUNT ================= */
    public ContentManagerStudentStatsResponseDTO getStudentCountUnderContentManager(Long cmId) {

        // Validate CM exists
        contentManagerRepository.findById(cmId)
                .orElseThrow(() -> new IllegalArgumentException("Content Manager not found"));

        long totalStudents =
                enrollmentRepository.countStudentsUnderContentManager(cmId);

        return new ContentManagerStudentStatsResponseDTO(
                cmId,
                totalStudents
        );
    }
    /* ================= STUDENTS UNDER CONTENT MANAGER ================= */
    public List<EnrolledStudentResponseDTO>
    getStudentsUnderContentManager(Long cmId) {

        contentManagerRepository.findById(cmId)
                .orElseThrow(() -> new IllegalArgumentException("Content Manager not found"));

        return enrollmentRepository.findStudentsUnderContentManager(cmId);
    }



    /* ================= REPLACE COURSES ================= */
    @Transactional
    public void updateAssignedCourses(List<Long> courseIds, Long cmId) {

        institutionCourseManagerRepository.deleteByContentManagerId(cmId);

        for (Long courseId : courseIds) {
            assignCourseToContentManager(courseId, cmId);
        }
    }
}
