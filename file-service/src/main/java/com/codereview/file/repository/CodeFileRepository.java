package com.codereview.file.repository;

import com.codereview.file.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {

    List<CodeFile> findByCodeRepositoryId(Long repositoryId);
}
