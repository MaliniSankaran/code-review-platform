package com.codereview.platform.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@Slf4j
public class FileStorageService {

    private final MinioClient minioClient;
    private final String bucketName;

    public FileStorageService(MinioClient minioClient, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        initBucket();
    }

    //Initialize Bucket
    private void initBucket() {

        try{
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if(!exists){
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("Bucket Created: {}", bucketName);
            }

        }catch (Exception e){
            log.error("Error initializing MinIO bucket: {}", e.getMessage());
            throw new RuntimeException("Could not initialize file storage: " + e);

        }
    }

    //Upload file
    public String uploadFile(String path, MultipartFile file) {

        log.info("Uploading file {} to path {}", file.getOriginalFilename(), path);

        try{

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("File uploaded successfully: {}", path);
            return path;

        } catch (Exception e) {
            log.error("Error uploading file {} to path {}",e.getMessage());
            throw new RuntimeException("Could not upload file",e);
        }
    }

    //Download file
    public InputStream downloadFile(String path) {

        log.info("Downloading file: {}", path);

        try{
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        }catch (Exception e){
                log.error("Error downloading file {}",e.getMessage());
                throw new RuntimeException("Could not download file",e);
        }
    }

    //Delete file
    public void deleteFile(String path) {

        log.info("Deleting file: {}", path);

        try{
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error deleting file {}",e.getMessage());
            throw new RuntimeException("Could not delete file",e);
        }
    }

}
