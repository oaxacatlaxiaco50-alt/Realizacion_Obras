package com.obraspublicas.features.expedientes.infrastructure.repository;

import com.obraspublicas.features.expedientes.application.mapper.ExpedienteMapper;
import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import com.obraspublicas.features.expedientes.domain.repository.CatalogoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CatalogoDocumentoRepositoryImpl implements CatalogoDocumentoRepository {

    private final CatalogoDocumentoJpaRepository jpaRepository;
    private final ExpedienteMapper mapper;

    @Override
    public List<CatalogoDocumento> findAllActive() {
        return jpaRepository.findByActivoTrue().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
