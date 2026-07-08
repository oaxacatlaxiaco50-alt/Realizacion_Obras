package com.obraspublicas.features.expedientes.application.service;

import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import com.obraspublicas.features.expedientes.domain.repository.CatalogoDocumentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogoDocumentoService {

    private final CatalogoDocumentoRepository repository;

    public List<CatalogoDocumento> obtenerCatalogoActivo() {
        return repository.findAllActive();
    }
}
