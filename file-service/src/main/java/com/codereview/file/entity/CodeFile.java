package com.codereview.file.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="code_files", indexes = {
        @Index(name = "idx_codefile_repository_id", columnList = "repository_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeFile {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String fileName;

    @Column(nullable=false)
    private String filePath;

    private String contentType;

    private Long size;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable=false)
    private CodeRepository codeRepository;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable=false)
    private User uploadedBy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
