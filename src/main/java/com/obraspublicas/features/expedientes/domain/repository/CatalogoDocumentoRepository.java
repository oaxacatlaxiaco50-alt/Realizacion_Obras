package com.obraspublicas.features.expedientes.domain.repository;

import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import java.util.List;

public interface CatalogoDocumentoRepository {
    List<CatalogoDocumento> findAllActive();
}
