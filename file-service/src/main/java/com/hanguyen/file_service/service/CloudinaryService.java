package com.hanguyen.file_service.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.hanguyen.file_service.exception.AppException;
import com.hanguyen.file_service.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary ;
    static long MAX_FILE_SIZE = 10 * 1024 * 1024;
    static List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/bmp",
            "image/tiff",
            "image/svg+xml",
            "image/x-icon"
    );


    public String uploadFile(MultipartFile file)  {

        if(file.isEmpty()){
            throw new AppException(ErrorCode.INVALID_FILE);
        }

        if(file.getSize() > MAX_FILE_SIZE){
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }

        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType())){
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }

        try {
            Map<? , ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "bookteria/image",
                            "overwrite", true)
            );
            return uploadResult.get("secure_url").toString();
        }
        catch (IOException e){
            throw new AppException(ErrorCode.UPLOAD_FAILED);
        }

    }
}
