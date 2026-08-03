package com.obraspublicas.features.archivos.infrastructure.repository;

import com.obraspublicas.features.archivos.domain.model.CarpetaTipo;
import com.obraspublicas.features.archivos.domain.model.ObraArchivo;
import com.obraspublicas.features.archivos.domain.repository.ObraArchivoRepository;
import com.obraspublicas.features.archivos.infrastructure.entity.ObraArchivoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ObraArchivoRepositoryImpl implements ObraArchivoRepository {

    private final ObraArchivoJpaRepository jpa;

    @Override
    public ObraArchivo save(ObraArchivo archivo) {
        ObraArchivoEntity entity = toEntity(archivo);
        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<ObraArchivo> findById(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<ObraArchivo> findByObraId(Long obraId) {
        return jpa.findByObraId(obraId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<ObraArchivo> findByObraIdAndCarpeta(Long obraId, CarpetaTipo carpeta) {
        return jpa.findByObraIdAndCarpeta(obraId, carpeta).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpa.existsById(id);
    }

    private ObraArchivo toDomain(ObraArchivoEntity e) {
        return ObraArchivo.builder()
                .id(e.getId())
                .obraId(e.getObraId())
                .carpeta(e.getCarpeta())
                .nombreOriginal(e.getNombreOriginal())
                .archivoUrl(e.getArchivoUrl())
                .tipoArchivo(e.getTipoArchivo())
                .tamanioBytes(e.getTamanioBytes())
                .subidoPor(e.getSubidoPor())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private ObraArchivoEntity toEntity(ObraArchivo d) {
        return ObraArchivoEntity.builder()
                .id(d.getId())
                .obraId(d.getObraId())
                .carpeta(d.getCarpeta())
                .nombreOriginal(d.getNombreOriginal())
                .archivoUrl(d.getArchivoUrl())
                .tipoArchivo(d.getTipoArchivo())
                .tamanioBytes(d.getTamanioBytes())
                .subidoPor(d.getSubidoPor())
                .build();
    }
}
