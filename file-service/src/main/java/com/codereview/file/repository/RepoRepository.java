package com.codereview.file.repository;

import com.codereview.file.entity.CodeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoRepository extends JpaRepository<CodeRepository, Long> {

    List<CodeRepository> findByOwnerId(Long ownerId);
    boolean existsByNameAndOwnerId(String name, Long ownerId);
}
