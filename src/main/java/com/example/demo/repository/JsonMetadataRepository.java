package com.example.demo.repository;

import com.example.demo.entity.JsonMetaDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JsonMetadataRepository extends JpaRepository<JsonMetaDataEntity, Long> {

    Optional<JsonMetaDataEntity> findByIdAndUploadedBy(Long id, String uploadedBy);
}