package com.example.lms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.lms.entity.Course;
import com.example.lms.entity.Institution;
import com.example.lms.repository.CourseRepository;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

	@Mock
	private CourseRepository courseRepository;

	@InjectMocks
	private CourseService courseService;

	@Test
	void getAllCourses_shouldReturnCourses() {
		Long institutionId = 1L;

		Course course1 = Course.builder().name("Computer Science").courseCode("CS101").duration("4 Years").semester(1)
				.institution(Institution.builder().id(institutionId).build()).build();

		Course course2 = Course.builder().name("Mechanical Engineering").courseCode("ME101").duration("4 Years")
				.semester(1).institution(Institution.builder().id(institutionId).build()).build();

		when(courseRepository.findByInstitution_IdAndDeletedAtIsNull(anyLong())).thenReturn(List.of(course1, course2));

		List<Course> result = courseService.getAllCourses(institutionId);

		assertNotNull(result);
		assertEquals(2, result.size());

		verify(courseRepository).findByInstitution_IdAndDeletedAtIsNull(institutionId);
	}

}
