package com.obraspublicas.features.obras.domain.repository;

import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObraRepository {
    Obra save(Obra obra);
    Optional<Obra> findById(Long id);
    Optional<Obra> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    Page<Obra> findAll(
            String codigo,
            String nombre,
            ObraEstatus estatus,
            String categoria,
            Long responsableId,
            LocalDate startFechaInicio,
            LocalDate endFechaInicio,
            Pageable pageable
    );
    
    List<Obra> findAll();

    Page<Obra> searchGlobal(String keyword, Pageable pageable);

    void deletePermanentById(Long id);

    List<Obra> findArchivoFiscal(Integer ejercicio, String categoria, Integer mes, String estatus, String queryText);

    List<Integer> findEjerciciosFiscalesDisponibles();

    int ejecutarCierreFiscal(Integer ejercicio, String usernameAdmin);

    int revertirCierreFiscal(Integer ejercicio, String usernameAdmin);

    long countObrasActivasSinCierre(Integer ejercicio);
}
