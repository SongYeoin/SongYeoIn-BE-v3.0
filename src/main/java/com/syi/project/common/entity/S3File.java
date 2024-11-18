package com.syi.project.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class S3File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String s3Key; // S3에서 파일을 식별하는 키

    @Column(nullable = false)
    private String bucketName; // S3 버킷 이름

    @Column(nullable = false)
    private String fileName; // 원본 파일명

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String fileUrl; // S3 접근 URL

    @Builder
    public S3File(String s3Key, String bucketName, String fileName, Long fileSize, String fileUrl) {
        this.s3Key = s3Key;
        this.bucketName = bucketName;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
    }
}
