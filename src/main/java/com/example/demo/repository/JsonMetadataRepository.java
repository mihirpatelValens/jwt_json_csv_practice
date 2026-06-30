package com.example.demo.repository;

import com.example.demo.entity.JsonMetaDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JsonMetadataRepository extends JpaRepository<JsonMetaDataEntity, Long> {
}
