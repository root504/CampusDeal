package com.campusdeal.controller;

import com.campusdeal.common.Result;
import com.campusdeal.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {
    private final FileService fileService;
    public UploadController(FileService fileService) {
        this.fileService = fileService;
    }


    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(fileService.uploadImage(file));
    }

    @PostMapping("/images")
    public Result<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        return Result.success(fileService.uploadImages(files));
    }
}
