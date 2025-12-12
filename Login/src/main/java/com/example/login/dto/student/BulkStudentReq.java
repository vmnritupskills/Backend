package com.example.login.dto.student;

import lombok.Data;
import java.util.List;

@Data
public class BulkStudentReq {
    private List<StudentReq> students;
}
