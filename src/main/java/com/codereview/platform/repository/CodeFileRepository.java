package com.codereview.platform.repository;

import com.codereview.platform.entity.CodeFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeFileRepository extends JpaRepository<CodeFile, Long> {

    List<CodeFile> findByCodeRepositoryId(Long repositoryId);
}
