package com.example.agenticurl.url.repository;

import com.example.agenticurl.url.entity.ShortUrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShortUrlRepository extends JpaRepository<ShortUrlEntity, Long> {
    Optional<ShortUrlEntity> findByShortCode(String code);
    boolean existsByShortCode(String code);
}
