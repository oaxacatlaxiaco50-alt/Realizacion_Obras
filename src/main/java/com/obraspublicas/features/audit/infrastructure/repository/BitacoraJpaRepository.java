package com.obraspublicas.features.audit.infrastructure.repository;

import com.obraspublicas.features.audit.infrastructure.entity.BitacoraEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BitacoraJpaRepository extends JpaRepository<BitacoraEntity, Long> {
    Page<BitacoraEntity> findByObraId(Long obraId, Pageable pageable);
}
