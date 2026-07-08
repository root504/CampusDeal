package com.campusdeal.service.impl;

import com.campusdeal.common.BusinessException;
import com.campusdeal.service.FileService;
import com.campusdeal.service.R2Service;
import com.campusdeal.util.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private final R2Service r2Service;

    public FileServiceImpl(R2Service r2Service) {
        this.r2Service = r2Service;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件为空");
        }

        FileUtil.validateImage(file);
        return r2Service.uploadFile(file, "images");
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("文件列表为空");
        }

        if (files.size() > 9) {
            throw new BusinessException("最多上传9张图片");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadImage(file));
        }
        return urls;
    }
}
