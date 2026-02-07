package com.example.lms.dto;

public record S3FileResponse(
        byte[] data,
        String contentType,
        String fileName
) {}

