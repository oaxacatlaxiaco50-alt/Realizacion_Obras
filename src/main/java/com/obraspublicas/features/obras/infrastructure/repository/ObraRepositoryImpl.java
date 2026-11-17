package com.obraspublicas.features.obras.infrastructure.repository;

import com.obraspublicas.features.obras.application.mapper.ObraMapper;
import com.obraspublicas.features.obras.domain.model.Obra;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.features.obras.domain.repository.ObraRepository;
import com.obraspublicas.features.obras.infrastructure.entity.ObraEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ObraRepositoryImpl implements ObraRepository {

    private final ObraJpaRepository jpaRepository;
    private final ObraMapper mapper;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public Obra save(Obra obra) {
        ObraEntity entity = mapper.toEntity(obra);
        ObraEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Obra> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Obra> findByCodigo(String codigo) {
        return jpaRepository.findByCodigo(codigo).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCodigo(String codigo) {
        return jpaRepository.existsByCodigo(codigo);
    }

    @Override
    public Page<Obra> findAll(
            String codigo,
            String nombre,
            ObraEstatus estatus,
            String categoria,
            Long responsableId,
            LocalDate startFechaInicio,
            LocalDate endFechaInicio,
            Pageable pageable
    ) {
        Specification<ObraEntity> spec = Specification.where((root, query, cb) ->
                cb.or(cb.isNull(root.get("archivadaFiscal")), cb.isFalse(root.get("archivadaFiscal")))
        );

        if (codigo != null && !codigo.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("codigo")), "%" + codigo.toLowerCase() + "%"));
        }
        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }
        if (estatus != null) {
            if (estatus == ObraEstatus.COMPLETADA || estatus == ObraEstatus.FINALIZADA) {
                spec = spec.and((root, query, cb) -> root.get("estatus").in(ObraEstatus.COMPLETADA, ObraEstatus.FINALIZADA));
            } else {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("estatus"), estatus));
            }
        } else {
            spec = spec.and((root, query, cb) -> cb.not(root.get("estatus").in(ObraEstatus.INACTIVA, ObraEstatus.CANCELADA, ObraEstatus.ELIMINADA)));
        }
        if (categoria != null && !categoria.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("categoria")), categoria.toLowerCase().trim()));
        }
        if (responsableId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("responsableId"), responsableId));
        }
        if (startFechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaInicio"), startFechaInicio));
        }
        if (endFechaInicio != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaInicio"), endFechaInicio));
        }

        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Obra> searchGlobal(String keyword, Pageable pageable) {
        Specification<ObraEntity> spec = Specification.where((root, query, cb) ->
                cb.or(cb.isNull(root.get("archivadaFiscal")), cb.isFalse(root.get("archivadaFiscal")))
        );
        
        spec = spec.and((root, query, cb) -> cb.not(root.get("estatus").in(ObraEstatus.INACTIVA, ObraEstatus.CANCELADA, ObraEstatus.ELIMINADA)));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String likePattern = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("codigo")), likePattern),
                    cb.like(cb.lower(root.get("nombre")), likePattern),
                    cb.like(cb.lower(root.get("descripcion")), likePattern)
            ));
        }

        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public List<Obra> findAll() {
        return jpaRepository.findAll().stream()
                .filter(e -> !e.isArchivadaFiscal())
                .filter(e -> e.getEstatus() != ObraEstatus.ELIMINADA
                        && e.getEstatus() != ObraEstatus.INACTIVA
                        && e.getEstatus() != ObraEstatus.CANCELADA)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deletePermanentById(Long id) {
        jdbcTemplate.update("DELETE FROM avance_evidencias WHERE avance_id IN (SELECT id FROM obra_avances WHERE obra_id = ?)", id);
        jdbcTemplate.update("DELETE FROM obra_avances WHERE obra_id = ?", id);
        jdbcTemplate.update("DELETE FROM obra_metas WHERE obra_id = ?", id);
        jdbcTemplate.update("DELETE FROM obra_archivos WHERE obra_id = ?", id);
        jdbcTemplate.update("DELETE FROM expedientes_obras WHERE obra_id = ?", id);
        jdbcTemplate.update("DELETE FROM notificaciones WHERE obra_id = ?", id);
        jdbcTemplate.execute("ALTER TABLE bitacoras DISABLE TRIGGER trg_prevent_delete_bitacoras");
        try {
            jdbcTemplate.update("DELETE FROM bitacoras WHERE obra_id = ?", id);
        } finally {
            jdbcTemplate.execute("ALTER TABLE bitacoras ENABLE TRIGGER trg_prevent_delete_bitacoras");
        }
        jdbcTemplate.update("DELETE FROM geocercas WHERE obra_id = ?", id);
        jdbcTemplate.update("DELETE FROM rutas_obras WHERE obra_id = ?", id);
        jpaRepository.deleteById(id);
    }

    @Override
    public List<Obra> findArchivoFiscal(Integer ejercicio, String categoria, Integer mes, String estatus, String queryText) {
        Specification<ObraEntity> spec = (root, query, cb) -> cb.isTrue(root.get("archivadaFiscal"));

        if (ejercicio != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ejercicioFiscal"), ejercicio));
        }
        if (categoria != null && !categoria.trim().isEmpty() && !categoria.equalsIgnoreCase("TODAS")) {
            String catPattern = "%" + categoria.toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("categoria")), catPattern));
        }
        if (mes != null && mes >= 1 && mes <= 12) {
            spec = spec.and((root, query, cb) -> cb.equal(cb.function("date_part", Integer.class, cb.literal("month"), root.get("fechaInicio")), mes));
        }
        if (estatus != null && !estatus.trim().isEmpty() && !estatus.equalsIgnoreCase("TODOS")) {
            try {
                ObraEstatus st = ObraEstatus.valueOf(estatus.toUpperCase().trim());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("estatus"), st));
            } catch (Exception ignored) {}
        }
        if (queryText != null && !queryText.trim().isEmpty()) {
            String pattern = "%" + queryText.toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), pattern),
                    cb.like(cb.lower(root.get("codigo")), pattern),
                    cb.like(cb.lower(root.get("contratista")), pattern)
            ));
        }

        return jpaRepository.findAll(spec, Sort.by(Sort.Order.desc("ejercicioFiscal"), Sort.Order.desc("id")))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Integer> findEjerciciosFiscalesDisponibles() {
        List<Integer> list = jdbcTemplate.queryForList(
                "SELECT DISTINCT ejercicio_fiscal FROM obras WHERE archivada_fiscal = true ORDER BY ejercicio_fiscal DESC",
                Integer.class
        );
        int currentYear = LocalDate.now().getYear();
        List<Integer> result = new ArrayList<>(list);
        if (!result.contains(currentYear)) {
            result.add(0, currentYear);
        }
        return result;
    }

    @Override
    @Transactional
    public int ejecutarCierreFiscal(Integer ejercicio, String usernameAdmin) {
        List<ObraEntity> candidatos = jpaRepository.findAll().stream()
                .filter(o -> !o.isArchivadaFiscal())
                .filter(o -> o.getEjercicioFiscal() == null || o.getEjercicioFiscal().equals(ejercicio))
                .toList();

        int count = 0;
        for (ObraEntity obra : candidatos) {
            ObraEstatus st = obra.getEstatus();
            if (st == ObraEstatus.PLANIFICADA || st == ObraEstatus.EN_PROCESO) {
                obra.setEstatus(ObraEstatus.COMPLETADA);
                obra.setNotaCierreFiscal("Concluida administrativamente por Cierre Fiscal del ejercicio " + ejercicio);
            } else if (st == ObraEstatus.ELIMINADA || st == ObraEstatus.CANCELADA) {
                obra.setNotaCierreFiscal("Archivada en Cierre Fiscal " + ejercicio + " con estatus: " + st);
            } else {
                obra.setNotaCierreFiscal("Cierre Fiscal ordinario del ejercicio " + ejercicio);
            }

            obra.setArchivadaFiscal(true);
            obra.setEjercicioFiscal(ejercicio);
            jpaRepository.save(obra);
            count++;
        }

        return count;
    }

    @Override
    @Transactional
    public int revertirCierreFiscal(Integer ejercicio, String usernameAdmin) {
        List<ObraEntity> archivadas = jpaRepository.findAll().stream()
                .filter(ObraEntity::isArchivadaFiscal)
                .filter(o -> ejercicio == null || ejercicio.equals(o.getEjercicioFiscal()))
                .toList();

        int count = 0;
        for (ObraEntity obra : archivadas) {
            String nota = obra.getNotaCierreFiscal();
            if (nota != null && nota.startsWith("Concluida administrativamente por Cierre Fiscal")) {
                obra.setEstatus(ObraEstatus.EN_PROCESO);
            }
            obra.setArchivadaFiscal(false);
            obra.setNotaCierreFiscal(null);
            jpaRepository.save(obra);
            count++;
        }

        return count;
    }

    @Override
    public long countObrasActivasSinCierre(Integer ejercicio) {
        return jpaRepository.findAll().stream()
                .filter(o -> !o.isArchivadaFiscal())
                .filter(o -> o.getEjercicioFiscal() == null || o.getEjercicioFiscal().equals(ejercicio))
                .count();
    }
}
