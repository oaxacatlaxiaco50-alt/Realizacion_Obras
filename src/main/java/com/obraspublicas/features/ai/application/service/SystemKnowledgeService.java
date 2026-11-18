package com.obraspublicas.features.ai.application.service;

import com.obraspublicas.features.archivos.infrastructure.repository.ObraArchivoJpaRepository;
import com.obraspublicas.features.audit.infrastructure.entity.AuditLogEntity;
import com.obraspublicas.features.audit.infrastructure.entity.BitacoraEntity;
import com.obraspublicas.features.audit.infrastructure.repository.AuditLogJpaRepository;
import com.obraspublicas.features.audit.infrastructure.repository.BitacoraJpaRepository;
import com.obraspublicas.features.avances.infrastructure.entity.ObraAvanceEntity;
import com.obraspublicas.features.avances.infrastructure.repository.ObraAvanceJpaRepository;
import com.obraspublicas.features.expedientes.domain.model.CatalogoDocumento;
import com.obraspublicas.features.expedientes.domain.model.EstadoDocumento;
import com.obraspublicas.features.expedientes.domain.model.SeccionExpediente;
import com.obraspublicas.features.expedientes.domain.repository.CatalogoDocumentoRepository;
import com.obraspublicas.features.expedientes.infrastructure.entity.ExpedienteObraEntity;
import com.obraspublicas.features.expedientes.infrastructure.repository.ExpedienteObraJpaRepository;
import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import com.obraspublicas.features.obras.infrastructure.entity.ObraEntity;
import com.obraspublicas.features.obras.infrastructure.entity.ObraMetaEntity;
import com.obraspublicas.features.obras.infrastructure.repository.ObraJpaRepository;
import com.obraspublicas.features.obras.infrastructure.repository.ObraMetaJpaRepository;
import com.obraspublicas.features.roles.infrastructure.entity.RoleEntity;
import com.obraspublicas.features.rutas.infrastructure.repository.RutaObraJpaRepository;
import com.obraspublicas.features.users.infrastructure.entity.UserEntity;
import com.obraspublicas.features.users.infrastructure.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemKnowledgeService {

    private final ObraJpaRepository obraJpaRepository;
    private final AuditLogJpaRepository auditLogJpaRepository;
    private final BitacoraJpaRepository bitacoraJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ObraArchivoJpaRepository obraArchivoJpaRepository;
    private final CatalogoDocumentoRepository catalogoDocumentoRepository;
    private final RutaObraJpaRepository rutaObraJpaRepository;
    private final ObraMetaJpaRepository obraMetaJpaRepository;
    private final ObraAvanceJpaRepository obraAvanceJpaRepository;
    private final ExpedienteObraJpaRepository expedienteObraJpaRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "MX"));

    private static final Set<String> STOP_WORDS = Set.copyOf(List.of(
            "la", "el", "los", "las", "un", "una", "unos", "unas", "de", "del", "al", "en", "con",
            "por", "para", "que", "cual", "cuales", "como", "donde", "esta", "estan", "es", "son",
            "hay", "tiene", "tienen", "obra", "obras", "proyecto", "proyectos", "dame", "quiero",
            "ver", "saber", "datos", "informacion", "info", "acerca", "sobre", "llama", "llamada",
            "ubicada", "ubicado", "encuentra", "cuesta", "vale", "monto", "nombre", "no", "me", "se",
            "pero", "ese", "esa", "esto", "favor", "puedes", "decir", "busca", "buscar"
    ));

    @Transactional(readOnly = true)
    public String getSummaryMetrics() {
        try {
            List<ObraEntity> obras = obraJpaRepository.findAll();
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

            long totalObras = obras.size();
            Map<String, Long> porEstatus = obras.stream()
                    .collect(Collectors.groupingBy(o -> o.getEstatus() != null ? o.getEstatus().name() : "DESCONOCIDO", Collectors.counting()));

            BigDecimal montoTotal = obras.stream()
                    .filter(o -> o.getEstatus() == null || !o.getEstatus().name().equals("ELIMINADA"))
                    .map(ObraEntity::getMonto)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long bloqueadas15 = obras.stream().filter(this::isObraBlocked15Days).count();
            long totalArchivos = obraArchivoJpaRepository.count();
            long totalRutas = rutaObraJpaRepository.count();
            String hoy = LocalDate.now().format(DATE_FORMATTER);

            StringBuilder sb = new StringBuilder();
            sb.append("📅 **Fecha de corte:** ").append(capitalize(hoy)).append("\n");
            sb.append("• **Total de Obras Registradas:** ").append(totalObras).append("\n");
            sb.append("• **Inversión Total Activa (sin Papelera):** ").append(currency.format(montoTotal)).append(" MXN\n");
            sb.append("• **Obras con Alerta de Bloqueo (>15 días sin editar):** ").append(bloqueadas15).append("\n");
            sb.append("• **Archivos en Expedientes:** ").append(totalArchivos).append(" documento(s)\n");
            sb.append("• **Rutas / Polígonos Trazados en Mapa:** ").append(totalRutas).append("\n");
            sb.append("• **Desglose por Estatus:**\n");
            porEstatus.forEach((estatus, cant) ->
                    sb.append("   - `").append(estatus).append("`: ").append(cant).append(" obra(s)\n")
            );

            return sb.toString();
        } catch (Exception e) {
            log.error("Error al obtener métricas del sistema", e);
            return "No fue posible consultar las métricas en vivo en este momento.";
        }
    }

    @Transactional(readOnly = true)
    public String getUsersSummaryForAdmin() {
        try {
            List<UserEntity> users = userJpaRepository.findAll();
            long total = users.size();
            long activos = users.stream().filter(UserEntity::isActive).count();
            long inactivos = total - activos;

            Map<String, Long> porRol = users.stream()
                    .collect(Collectors.groupingBy(u -> {
                        if (u.getRoles() == null || u.getRoles().isEmpty()) return "SIN_ROL";
                        return u.getRoles().stream().map(RoleEntity::getName).findFirst().orElse("SIN_ROL");
                    }, Collectors.counting()));

            StringBuilder sb = new StringBuilder();
            sb.append("👥 **Resumen de Usuarios del Sistema (Datos en Vivo):**\n");
            sb.append("• **Total de Cuentas:** ").append(total).append(" (")
              .append(activos).append(" activas, ").append(inactivos).append(" inactivas)\n");
            sb.append("• **Distribución por Rol:**\n");
            porRol.forEach((rol, count) ->
                    sb.append("   - `").append(rol).append("`: ").append(count).append(" usuario(s)\n")
            );

            sb.append("\n📋 **Listado de Usuarios Registrados:**\n");
            for (UserEntity u : users) {
                String rol = (u.getRoles() != null && !u.getRoles().isEmpty())
                        ? u.getRoles().stream().map(RoleEntity::getName).collect(Collectors.joining(", "))
                        : "SIN ROL";
                String estado = u.isActive() ? "Activo" : "Inactivo";
                sb.append(String.format("• **%s %s** (`@%s`) — Rol: `%s` | Estado: **%s** | Correo: %s\n",
                        u.getFirstName(), u.getLastName(), u.getUsername(), rol, estado, u.getEmail()));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error al obtener usuarios para IA", e);
            return "Puedes consultar y gestionar todos los usuarios desde el módulo **⚙️ Administración**.";
        }
    }

    @Transactional(readOnly = true)
    public String getDetailedObrasList() {
        try {
            List<ObraEntity> obras = obraJpaRepository.findAll();
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            LocalDate hoy = LocalDate.now();

            if (obras.isEmpty()) {
                return "No hay obras registradas en el sistema actualmente.";
            }

            StringBuilder sb = new StringBuilder();
            for (ObraEntity o : obras) {
                String inicioStr = o.getFechaInicio() != null ? o.getFechaInicio().format(DATE_FORMATTER) : "No definida";
                String finStr = o.getFechaFin() != null ? o.getFechaFin().format(DATE_FORMATTER) : "No definida";

                String plazoInfo = "";
                if (o.getFechaInicio() != null && o.getFechaFin() != null) {
                    long diasPlazo = ChronoUnit.DAYS.between(o.getFechaInicio(), o.getFechaFin());
                    long diasRestantes = ChronoUnit.DAYS.between(hoy, o.getFechaFin());
                    if (diasRestantes > 0) {
                        plazoInfo = String.format(" (%d días de plazo | Restan %d días)", diasPlazo, diasRestantes);
                    } else if (diasRestantes == 0) {
                        plazoInfo = String.format(" (%d días de plazo | Vence hoy)", diasPlazo);
                    } else {
                        plazoInfo = String.format(" (%d días de plazo | Concluyó hace %d días)", diasPlazo, Math.abs(diasRestantes));
                    }
                }

                sb.append(String.format("• **[%s]** %s\n  - *Estatus:* `%s` | *Monto:* %s | *Categoría:* %s | *Localidad:* %s\n  - *Plazo:* Del %s al %s%s\n",
                        o.getCodigo(),
                        o.getNombre(),
                        o.getEstatus(),
                        currency.format(o.getMonto() != null ? o.getMonto() : BigDecimal.ZERO),
                        o.getCategoria() != null ? o.getCategoria() : "Infraestructura General",
                        o.getDireccion() != null && !o.getDireccion().isBlank() ? o.getDireccion() : "Tlaxiaco",
                        capitalize(inicioStr),
                        capitalize(finStr),
                        plazoInfo));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("Error al listar obras", e);
            return "No se pudieron recuperar las obras en este momento.";
        }
    }

    @Transactional(readOnly = true)
    public String getHighestAndLowestBudgetObras() {
        try {
            List<ObraEntity> obras = obraJpaRepository.findAll().stream()
                    .filter(o -> o.getEstatus() == null || !o.getEstatus().name().equals("ELIMINADA"))
                    .toList();
            if (obras.isEmpty()) return "No hay obras activas registradas para analizar presupuestos.";

            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            ObraEntity maxObra = obras.stream()
                    .filter(o -> o.getMonto() != null)
                    .max(Comparator.comparing(ObraEntity::getMonto))
                    .orElse(null);

            ObraEntity minObra = obras.stream()
                    .filter(o -> o.getMonto() != null)
                    .min(Comparator.comparing(ObraEntity::getMonto))
                    .orElse(null);

            StringBuilder sb = new StringBuilder();
            if (maxObra != null) {
                String finStr = maxObra.getFechaFin() != null ? capitalize(maxObra.getFechaFin().format(DATE_FORMATTER)) : "Sin fecha";
                sb.append(String.format("🏆 **Obra con Mayor Presupuesto:**\n• **%s** (`%s`)\n• **Monto:** %s MXN\n• **Estatus:** `%s`\n• **Localidad:** %s\n• **Fecha Término:** %s\n\n",
                        maxObra.getNombre(), maxObra.getCodigo(), currency.format(maxObra.getMonto()), maxObra.getEstatus(),
                        maxObra.getDireccion() != null ? maxObra.getDireccion() : "Tlaxiaco", finStr));
            }
            if (minObra != null) {
                String finStr = minObra.getFechaFin() != null ? capitalize(minObra.getFechaFin().format(DATE_FORMATTER)) : "Sin fecha";
                sb.append(String.format("🔹 **Obra con Menor Presupuesto:**\n• **%s** (`%s`)\n• **Monto:** %s MXN\n• **Estatus:** `%s`\n• **Localidad:** %s\n• **Fecha Término:** %s",
                        minObra.getNombre(), minObra.getCodigo(), currency.format(minObra.getMonto()), minObra.getEstatus(),
                        minObra.getDireccion() != null ? minObra.getDireccion() : "Tlaxiaco", finStr));
            }
            return sb.toString();
        } catch (Exception e) {
            return "Información presupuestal disponible en el módulo de Expedientes.";
        }
    }

    @Transactional(readOnly = true)
    public String searchObrasByKeyword(String keyword) {
        return smartSearchObrasWithFullDetails(keyword, "ADMINISTRADOR");
    }

    /**
     * Búsqueda inteligente multicampo + tolerancia a faltas de ortografía + cruce con responsable, metas y monto aproximado.
     * Devuelve null si ninguna obra tuvo coincidencia suficiente.
     */
    @Transactional(readOnly = true)
    public String smartSearchObrasWithFullDetailsOrNull(String rawQuery, String role) {
        try {
            String normalizedQuery = normalizeText(rawQuery);
            List<String> tokens = extractSearchTokens(normalizedQuery);
            BigDecimal targetAmount = extractApproxAmount(normalizedQuery);

            if (tokens.isEmpty() && targetAmount == null) {
                return null;
            }

            boolean isAdmin = "ADMINISTRADOR".equalsIgnoreCase(role);
            List<ObraEntity> obras = obraJpaRepository.findAll().stream()
                    .filter(o -> isAdmin || o.getEstatus() != ObraEstatus.ELIMINADA)
                    .toList();

            if (obras.isEmpty()) return null;

            Map<Long, UserEntity> usersById = userJpaRepository.findAll().stream()
                    .collect(Collectors.toMap(UserEntity::getId, u -> u, (a, b) -> a));

            List<ScoredObra> scoredList = new ArrayList<>();

            for (ObraEntity o : obras) {
                int score = 0;

                String codigoN = normalizeText(o.getCodigo());
                String nombreN = normalizeText(o.getNombre());
                String dirN = normalizeText(o.getDireccion());
                String catN = normalizeText(o.getCategoria());
                String descN = normalizeText(o.getDescripcion());
                String contrN = normalizeText(o.getContratista());
                String fuenteN = normalizeText(o.getFuenteFinanciamiento());
                String benefN = normalizeText(o.getBeneficiarios());
                String impactoN = normalizeText(o.getImpactoSocial());
                String estN = o.getEstatus() != null ? normalizeText(o.getEstatus().name().replace("_", " ")) : "";

                UserEntity resp = o.getResponsableId() != null ? usersById.get(o.getResponsableId()) : null;
                String respN = resp != null
                        ? normalizeText(resp.getFirstName() + " " + resp.getLastName() + " " + resp.getUsername())
                        : "";

                List<ObraMetaEntity> metas = obraMetaJpaRepository.findByObraId(o.getId());
                String metasN = metas.stream()
                        .map(m -> normalizeText(m.getConcepto() + " " + m.getUnidadMedida()))
                        .collect(Collectors.joining(" "));

                for (String tok : tokens) {
                    if (codigoN.contains(tok)) score += 10;
                    if (containsTokenOrFuzzy(nombreN, tok)) score += 6;
                    if (containsTokenOrFuzzy(dirN, tok)) score += 6;
                    if (containsTokenOrFuzzy(catN, tok)) score += 4;
                    if (containsTokenOrFuzzy(descN, tok)) score += 4;
                    if (containsTokenOrFuzzy(metasN, tok)) score += 5;
                    if (containsTokenOrFuzzy(respN, tok)) score += 5;
                    if (containsTokenOrFuzzy(contrN, tok)) score += 5;
                    if (containsTokenOrFuzzy(fuenteN, tok)) score += 4;
                    if (containsTokenOrFuzzy(benefN, tok)) score += 3;
                    if (containsTokenOrFuzzy(impactoN, tok)) score += 3;
                    if (containsTokenOrFuzzy(estN, tok)) score += 3;
                }

                if (targetAmount != null && o.getMonto() != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                    double diffRatio = Math.abs(o.getMonto().doubleValue() - targetAmount.doubleValue()) / targetAmount.doubleValue();
                    if (diffRatio <= 0.05) score += 10;
                    else if (diffRatio <= 0.25) score += 6;
                }

                if (score >= 5) {
                    scoredList.add(new ScoredObra(o, score));
                }
            }

            if (scoredList.isEmpty()) {
                return null;
            }

            scoredList.sort((a, b) -> Integer.compare(b.score(), a.score()));
            int topScore = scoredList.get(0).score();

            List<ObraEntity> topMatches = scoredList.stream()
                    .filter(s -> s.score() >= Math.max(5, topScore - 4))
                    .limit(3)
                    .map(ScoredObra::obra)
                    .toList();

            return formatObrasFullFicha(topMatches, usersById);
        } catch (Exception e) {
            log.error("Error en búsqueda inteligente de obras", e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public String smartSearchObrasWithFullDetails(String keyword, String role) {
        String res = smartSearchObrasWithFullDetailsOrNull(keyword, role);
        if (res != null) return res;
        return "No encontré una obra que coincida exactamente con *" + keyword + "*. Aquí tienes la lista general registrada:\n\n" + getDetailedObrasList();
    }

    @Transactional(readOnly = true)
    public String getObrasByStatusFilter(String statusCondition, String role) {
        try {
            boolean isAdmin = "ADMINISTRADOR".equalsIgnoreCase(role);
            List<ObraEntity> todas = obraJpaRepository.findAll();
            Map<Long, UserEntity> usersById = userJpaRepository.findAll().stream()
                    .collect(Collectors.toMap(UserEntity::getId, u -> u, (a, b) -> a));

            List<ObraEntity> filtradas;
            String titulo;

            switch (statusCondition) {
                case "BLOQUEADAS" -> {
                    filtradas = todas.stream()
                            .filter(o -> (isAdmin || o.getEstatus() != ObraEstatus.ELIMINADA) && isObraBlocked15Days(o))
                            .toList();
                    titulo = "🔒 Obras con Alerta de Bloqueo (>15 días sin actualizar)";
                }
                case "INACTIVA" -> {
                    filtradas = todas.stream().filter(o -> o.getEstatus() == ObraEstatus.INACTIVA).toList();
                    titulo = "🟠 Obras en Estatus INACTIVA";
                }
                case "CANCELADA" -> {
                    filtradas = todas.stream().filter(o -> o.getEstatus() == ObraEstatus.CANCELADA).toList();
                    titulo = "🔴 Obras en Estatus CANCELADA";
                }
                case "EN_PROCESO" -> {
                    filtradas = todas.stream().filter(o -> o.getEstatus() == ObraEstatus.EN_PROCESO).toList();
                    titulo = "🟢 Obras EN PROCESO";
                }
                case "PLANIFICADA" -> {
                    filtradas = todas.stream().filter(o -> o.getEstatus() == ObraEstatus.PLANIFICADA).toList();
                    titulo = "📋 Obras PLANIFICADAS";
                }
                case "COMPLETADA" -> {
                    filtradas = todas.stream()
                            .filter(o -> o.getEstatus() == ObraEstatus.COMPLETADA || o.getEstatus() == ObraEstatus.FINALIZADA)
                            .toList();
                    titulo = "🔵 Obras COMPLETADAS";
                }
                case "ELIMINADA" -> {
                    filtradas = todas.stream().filter(o -> o.getEstatus() == ObraEstatus.ELIMINADA).toList();
                    titulo = "🗑️ Obras en Papelera (ELIMINADA)";
                }
                default -> {
                    filtradas = List.of();
                    titulo = "Obras";
                }
            }

            if (filtradas.isEmpty()) {
                return "### " + titulo + "\n\nNo hay obras registradas actualmente en este estado.";
            }

            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
            StringBuilder sb = new StringBuilder("### ").append(titulo).append(" (").append(filtradas.size()).append(")\n\n");
            for (ObraEntity o : filtradas) {
                UserEntity resp = o.getResponsableId() != null ? usersById.get(o.getResponsableId()) : null;
                String respStr = resp != null ? (resp.getFirstName() + " " + resp.getLastName()) : ("Resp. #" + o.getResponsableId());
                String extra = "";
                if ("ELIMINADA".equals(statusCondition)) {
                    LocalDateTime ref = o.getUpdatedAt() != null ? o.getUpdatedAt() : o.getCreatedAt();
                    long dias = ref != null ? Math.max(0, ChronoUnit.DAYS.between(ref, LocalDateTime.now())) : 0;
                    long faltan = Math.max(0, 30 - dias);
                    extra = faltan == 0 ? " | 🔥 Lista para borrado definitivo" : (" | 🔒 Faltan " + faltan + " días para borrado definitivo");
                } else if (isObraBlocked15Days(o)) {
                    extra = " | 🚫 Bloqueada (>15 días sin editar)";
                }
                sb.append(String.format("• **[%s] %s**\n  - Monto: %s | Localidad: %s | Responsable: %s%s\n",
                        o.getCodigo(),
                        o.getNombre(),
                        currency.format(o.getMonto() != null ? o.getMonto() : BigDecimal.ZERO),
                        o.getDireccion() != null && !o.getDireccion().isBlank() ? o.getDireccion() : "Tlaxiaco",
                        respStr,
                        extra));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error filtrando obras por estatus", e);
            return "No se pudo consultar el listado en este momento.";
        }
    }

    private String formatObrasFullFicha(List<ObraEntity> obras, Map<Long, UserEntity> usersById) {
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        StringBuilder sb = new StringBuilder();
        if (obras.size() == 1) {
            sb.append("### 🔍 Ficha Completa de la Obra Encontrada\n\n");
        } else {
            sb.append("### 🔍 Obras Coincidentes Encontradas (").append(obras.size()).append(")\n\n");
        }

        for (ObraEntity o : obras) {
            UserEntity resp = o.getResponsableId() != null ? usersById.get(o.getResponsableId()) : null;
            String respNombre = resp != null
                    ? (resp.getFirstName() + " " + resp.getLastName() + " (`@" + resp.getUsername() + "`)")
                    : ("Responsable #" + o.getResponsableId());

            Integer avancePct = (o.getEstatus() == ObraEstatus.COMPLETADA || o.getEstatus() == ObraEstatus.FINALIZADA)
                    ? 100
                    : obraAvanceJpaRepository.findFirstByObraIdOrderByPorcentajeDesc(o.getId())
                            .map(ObraAvanceEntity::getPorcentaje)
                            .orElse(0);

            List<ObraMetaEntity> metas = obraMetaJpaRepository.findByObraId(o.getId());
            List<ExpedienteObraEntity> checklist = expedienteObraJpaRepository.findByObraId(o.getId());
            long okCount = checklist.stream().filter(i -> i.getEstado() == EstadoDocumento.OK || i.getEstado() == EstadoDocumento.NO_APLICA).count();
            long conArchivo = checklist.stream().filter(i -> i.getArchivoUrl() != null && !i.getArchivoUrl().isBlank()).count();
            int archivosCarpetas = obraArchivoJpaRepository.findByObraId(o.getId()).size();
            int rutasCount = rutaObraJpaRepository.findByObraId(o.getId()).size();

            boolean blocked15 = isObraBlocked15Days(o);

            sb.append(String.format("🏗️ **[%s] %s**\n", o.getCodigo(), o.getNombre()));
            sb.append("• **Estatus:** `").append(o.getEstatus()).append("`")
              .append(blocked15 ? " (🚫 **Bloqueada >15 días sin editar**)" : "")
              .append(" | **Avance Global:** **").append(avancePct).append("%**\n");
            sb.append("• **Categoría:** ").append(o.getCategoria() != null ? o.getCategoria() : "Infraestructura General").append("\n");
            sb.append("• **Ubicación / Localidad:** ").append(o.getDireccion() != null && !o.getDireccion().isBlank() ? o.getDireccion() : "Tlaxiaco").append("\n");
            sb.append("• **Presupuesto:** ").append(currency.format(o.getMonto() != null ? o.getMonto() : BigDecimal.ZERO)).append(" MXN\n");
            sb.append("• **Periodo:** ").append(o.getFechaInicio()).append(" al ").append(o.getFechaFin()).append("\n");
            sb.append("• **Responsable:** ").append(respNombre).append("\n");

            if (o.getContratista() != null && !o.getContratista().isBlank()) {
                sb.append("• **Contratista:** ").append(o.getContratista()).append("\n");
            }
            if (o.getFuenteFinanciamiento() != null && !o.getFuenteFinanciamiento().isBlank()) {
                sb.append("• **Fuente de Financiamiento:** ").append(o.getFuenteFinanciamiento()).append("\n");
            }
            if (o.getBeneficiarios() != null && !o.getBeneficiarios().isBlank()) {
                sb.append("• **Beneficiarios:** ").append(o.getBeneficiarios()).append("\n");
            }
            if (o.getDescripcion() != null && !o.getDescripcion().isBlank()) {
                String desc = o.getDescripcion().length() > 180 ? o.getDescripcion().substring(0, 177) + "..." : o.getDescripcion();
                sb.append("• **Descripción:** ").append(desc).append("\n");
            }

            sb.append("• **Expediente Digital:** ").append(okCount).append("/").append(checklist.size())
              .append(" requisitos OK (").append(conArchivo).append(" con PDF adjunto) | ")
              .append(archivosCarpetas).append(" archivo(s) en carpetas | ").append(rutasCount).append(" ruta(s) en mapa\n");

            if (!metas.isEmpty()) {
                sb.append("• **Metas Físicas:**\n");
                for (ObraMetaEntity m : metas) {
                    sb.append(String.format("   - %s: %.2f / %.2f %s (**%d%%** · `%s`)\n",
                            m.getConcepto(),
                            m.getAvanceAcumulado() != null ? m.getAvanceAcumulado() : 0.0,
                            m.getCantidadMeta() != null ? m.getCantidadMeta() : 0.0,
                            m.getUnidadMedida(),
                            m.getPorcentaje() != null ? m.getPorcentaje() : 0,
                            m.getEstado()));
                }
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    @Transactional(readOnly = true)
    public String getChecklistDocumentsBySection(String normalizedPrompt) {
        try {
            List<CatalogoDocumento> docs = catalogoDocumentoRepository.findAllActive();
            if (docs.isEmpty()) {
                return "El catálogo oficial cuenta con **4 secciones**: I. Parte Social, II. Proyecto Ejecutivo, III. Procesos de Contratación y IV. Documentos Comprobatorios.";
            }

            SeccionExpediente filtro = null;
            String tituloSeccion = "Todas las Secciones del Checklist Oficial 2026";

            if (normalizedPrompt.contains("parte social") || normalizedPrompt.contains("social")) {
                filtro = SeccionExpediente.PARTE_SOCIAL;
                tituloSeccion = "I. Parte Social";
            } else if (normalizedPrompt.contains("proyecto ejecutivo") || normalizedPrompt.contains("parte tecnica") || normalizedPrompt.contains("tecnico")) {
                filtro = SeccionExpediente.PROYECTO_EJECUTIVO;
                tituloSeccion = "II. Parte Técnica o Proyecto Ejecutivo";
            } else if (normalizedPrompt.contains("contratacion") || normalizedPrompt.contains("contrato") || normalizedPrompt.contains("licitacion")) {
                filtro = SeccionExpediente.PROCESOS_CONTRATACION;
                tituloSeccion = "III. Procesos de Contratación";
            } else if (normalizedPrompt.contains("comprobatorio") || normalizedPrompt.contains("estimacion") || normalizedPrompt.contains("factura") || normalizedPrompt.contains("finiquito")) {
                filtro = SeccionExpediente.DOCUMENTOS_COMPROBATORIOS;
                tituloSeccion = "IV. Documentos Comprobatorios";
            }

            if (filtro != null) {
                SeccionExpediente target = filtro;
                List<CatalogoDocumento> items = docs.stream().filter(d -> d.getSeccion() == target).toList();
                StringBuilder sb = new StringBuilder("### 📋 Requisitos de ").append(tituloSeccion).append(" (").append(items.size()).append(")\n\n");
                for (int i = 0; i < items.size(); i++) {
                    sb.append(i + 1).append(". ").append(items.get(i).getNombre()).append("\n");
                }
                return sb.toString();
            }

            Map<SeccionExpediente, Long> conteo = docs.stream()
                    .collect(Collectors.groupingBy(CatalogoDocumento::getSeccion, Collectors.counting()));
            return String.format("### 📋 Checklist Oficial 2026 (%d Requisitos Activos)\n\n" +
                    "• **I. Parte Social (`PARTE_SOCIAL`):** %d requisitos\n" +
                    "• **II. Parte Técnica / Proyecto Ejecutivo (`PROYECTO_EJECUTIVO`):** %d requisitos\n" +
                    "• **III. Procesos de Contratación (`PROCESOS_CONTRATACION`):** %d requisitos\n" +
                    "• **IV. Documentos Comprobatorios (`DOCUMENTOS_COMPROBATORIOS`):** %d requisitos\n\n" +
                    "💡 *Si quieres ver la lista exacta de documentos de alguna sección, pregúntame por ejemplo: \"¿Qué documentos pide la Parte Social?\" o \"¿Qué documentos pide Contratación?\"*",
                    docs.size(),
                    conteo.getOrDefault(SeccionExpediente.PARTE_SOCIAL, 0L),
                    conteo.getOrDefault(SeccionExpediente.PROYECTO_EJECUTIVO, 0L),
                    conteo.getOrDefault(SeccionExpediente.PROCESOS_CONTRATACION, 0L),
                    conteo.getOrDefault(SeccionExpediente.DOCUMENTOS_COMPROBATORIOS, 0L));
        } catch (Exception e) {
            log.error("Error consultando catálogo", e);
            return "El Checklist oficial está organizado en 4 secciones: Parte Social, Proyecto Ejecutivo, Procesos de Contratación y Documentos Comprobatorios.";
        }
    }

    @Transactional(readOnly = true)
    public String getAuditSummary() {
        try {
            long totalLogs = auditLogJpaRepository.count();
            List<AuditLogEntity> recientes = auditLogJpaRepository.findTop10ByOrderByIdDesc();

            StringBuilder sb = new StringBuilder();
            sb.append("• **Total Eventos en Logs de Auditoría:** ").append(totalLogs).append("\n");
            sb.append("• **Últimos Eventos Registrados:**\n");
            for (AuditLogEntity logItem : recientes) {
                String desc = logItem.getDescription() != null ? logItem.getDescription() : "Sin descripción";
                String fechaHoraStr = "Reciente";
                if (logItem.getTimestamp() != null) {
                    fechaHoraStr = logItem.getTimestamp().toString().replace("T", " ").substring(0, Math.min(19, logItem.getTimestamp().toString().length()));
                }
                sb.append(String.format("   - [%s] **%s** ejecutó `%s` en %s (%s)\n",
                        fechaHoraStr,
                        logItem.getUsername() != null ? logItem.getUsername() : "Sistema",
                        logItem.getAction(),
                        logItem.getModule(),
                        desc));
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("Error al obtener auditoría", e);
            return "Bitácora inalterable activa con registro estricto de eventos de usuarios y auditoría de archivos.";
        }
    }

    @Transactional(readOnly = true)
    public String getBitacoraGeneralSummary() {
        try {
            long totalBitacora = bitacoraJpaRepository.count();
            List<BitacoraEntity> recientes = bitacoraJpaRepository
                    .findAll(PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "timestamp")))
                    .getContent();

            StringBuilder sb = new StringBuilder();
            sb.append("• **Total Registros en Bitácora General:** ").append(totalBitacora).append("\n");
            sb.append("• **Últimos Movimientos de Obras y Expedientes:**\n");
            for (BitacoraEntity b : recientes) {
                String fechaHora = b.getTimestamp() != null
                        ? b.getTimestamp().toString().replace("T", " ").substring(0, Math.min(19, b.getTimestamp().toString().length()))
                        : "Reciente";
                sb.append(String.format("   - [%s] Obra #%s — `%s`: %s\n",
                        fechaHora,
                        b.getObraId() != null ? b.getObraId() : "-",
                        b.getStatus() != null ? b.getStatus() : "REGISTRO",
                        b.getDescription() != null ? b.getDescription() : "Movimiento registrado"));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Error al obtener bitácora general", e);
            return "Puedes consultar el **📋 Historial General** en el menú lateral para ver todos los movimientos cronológicos.";
        }
    }

    @Transactional(readOnly = true)
    public int getCatalogoDocumentosCount() {
        try {
            return catalogoDocumentoRepository.findAllActive().size();
        } catch (Exception e) {
            return 44;
        }
    }

    public String getProjectArchitectureKnowledge(String role) {
        int totalChecklist = getCatalogoDocumentosCount();
        String base = "### 🏛️ ¿Cómo está conformado el Proyecto ResidenciaObra (Tlaxiaco)?\n\n" +
                "Es una plataforma web de gestión y fiscalización de **Obras Públicas del H. Ayuntamiento de Heroica Ciudad de Tlaxiaco**, construida bajo arquitectura cliente-servidor:\n\n" +
                "**1. Capa Frontend (`Obras_Tlaxiaco` — Angular 19):**\n" +
                "• Interfaz SPA moderna con Standalone Components, Signals y control de acceso por rol.\n" +
                "• **Mapas (`Leaflet`)**: Geolocalización GPS en Tlaxiaco y trazo interactivo de rutas/polígonos con cálculo de longitud y cierre magnético (**Imán**).\n" +
                "• **Reportes (`jsPDF`, `AutoTable`, `XLSX`)**: Generación de Fichas Técnicas, Checklist de Expediente Unitario 2026 y sábanas de Excel.\n\n" +
                "**2. Capa Backend (`Realizacion_Obras` — Spring Boot 3.4 / Java 21):**\n" +
                "• Arquitectura modular limpia dividida en `features`: `auth`, `users`, `roles`, `permissions`, `obras`, `expedientes`, `archivos`, `avances`, `rutas`, `audit`, `notificaciones` y `ai`.\n" +
                "• **Seguridad**: Autenticación JWT sin estado (*Stateless*), contraseñas cifradas con **BCrypt**, `@PreAuthorize` por rol en cada endpoint y triggers SQL que protegen la bitácora.\n\n" +
                "**3. Módulos Principales del Sistema:**\n" +
                "• **📊 Dashboard**: KPIs de inversión, avance físico y gráficas.\n" +
                "• **🗺️ Geolocalización**: Marcadores de obras y trazado de rutas sobre el mapa.\n" +
                "• **📁 Expedientes (`/obras`)**: Padrón de obras, filtros, Papelera con retención de **30 días**, Carpetas (**⚖️ Legal, 👥 Social, 📐 Técnicos, 📸 Anexo Fotográfico**), Checklist oficial de **" + totalChecklist + " requisitos en 4 secciones** y Metas/Avances con evidencias (*Antes, Durante, Después*).\n" +
                "• **🔔 Notificaciones**: Alertas automáticas cada 30s al asignar obras o registrar eventos.\n" +
                "• **📋 Historial General**: Bitácora cronológica de movimientos.\n" +
                "• **🛡️ Control Anti-Duplicados**: Validación por firma criptográfica **SHA-256** que bloquea archivos repetidos.\n";

        if ("ADMINISTRADOR".equals(role)) {
            base += "• **⚙️ Administración, 🔒 Bitácora de Cambios y 🔍 Logs de Auditoría**: Módulos exclusivos de **ADMINISTRADOR** para CRUD de usuarios, papelera de obras y trazabilidad de seguridad.\n\n" +
                    "**4. Roles del Sistema:**\n" +
                    "• `ADMINISTRADOR`: Control total (usuarios, obras, estatus, archivos, reportes, auditoría).\n" +
                    "• `SUPERVISOR`: Crea/edita obras, cambia estatus, sube archivos/avances y exporta reportes.\n" +
                    "• `CONTRATISTA`: Consulta obras/expedientes y sube archivos, checklist y avances físicos.\n" +
                    "• `AUDITOR`: Consulta obras/expedientes en **solo lectura** y exporta reportes PDF/Excel.";
        } else {
            base += "\n🔑 *Tu sesión actual tiene el rol **`" + role + "`**; el Asistente IA y los menús del sistema adaptan sus funciones y respuestas a los permisos de tu perfil.*";
        }
        return base;
    }

    @Transactional(readOnly = true)
    public String buildCompactSystemPromptForGemma(String role) {
        String effectiveRole = (role != null) ? role.toUpperCase() : "AUDITOR";
        boolean isAdmin = "ADMINISTRADOR".equals(effectiveRole);
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        StringBuilder sb = new StringBuilder();
        sb.append("Eres el Asistente IA oficial de ResidenciaObra (H. Ayuntamiento de Heroica Ciudad de Tlaxiaco).\n");
        sb.append("Responde siempre en español, de forma clara, amable y concisa (máximo 5 líneas o viñetas). No inventes funciones ni datos.\n\n");

        sb.append("ROL ACTUAL DEL USUARIO: ").append(effectiveRole).append("\n");
        switch (effectiveRole) {
            case "ADMINISTRADOR" -> sb.append("PERMISOS: Control total (usuarios en /admin, crear/editar/eliminar obras, papelera 30 días, cambiar estatus, subir/borrar archivos sin candado, bitácoras, logs de auditoría con IP, reportes PDF/Excel).\n");
            case "SUPERVISOR" -> sb.append("PERMISOS: Crea/edita obras, cambia estatus, asigna responsables, traza rutas en mapa, sube archivos/avances y exporta PDF/Excel. RESTRINGIDO: NO gestiona usuarios, NO ve Logs/Bitácora de Cambios, NO elimina obras ni archivos (candado).\n");
            case "CONTRATISTA" -> sb.append("PERMISOS: Consulta obras/expedientes/mapa/historial y sube archivos, checklist y avances físicos. RESTRINGIDO: NO gestiona usuarios, NO crea/edita obras, NO cambia estatus, NO borra archivos (candado) y NO exporta PDF/Excel.\n");
            default -> sb.append("PERMISOS: Solo lectura para consultar obras, mapa, historial, carpetas y checklist (puede ver/descargar) y exportar PDF/Excel. RESTRINGIDO: NO gestiona usuarios, NO crea/edita obras, NO cambia estatus, NO sube ni borra archivos ni registra avances.\n");
        }
        sb.append("Si el usuario pide realizar una acción restringida para su rol, indícale claramente que está restringida, el motivo y qué sí puede hacer.\n\n");

        sb.append("REGLAS Y MÓDULOS CLAVE DEL SISTEMA:\n");
        sb.append("- Stack: Frontend Angular 19 (Leaflet con zoom máx 18 e imán/snap, jsPDF, XLSX), Backend Spring Boot 3.4 + Java 21 + PostgreSQL, JWT, BCrypt (contraseñas cifradas unidireccionales: nadie puede verlas, solo Admin asigna nueva al editar).\n");
        sb.append("- Expedientes (/obras): Pestaña 'Todas' excluye CANCELADA, INACTIVA y ELIMINADA. Papelera ('ELIMINADA') exige 30 días antes de borrado definitivo.\n");
        sb.append("- Cierre Fiscal Anual: Restringido estrictamente a Diciembre (clave CONFIRMAR-[Año]). Obras concluidas/planificadas pasan a COMPLETADA y se trasladan al Archivo Fiscal (/bitacora). Padrón activo queda en 0. Reversión requiere dos pasos y clave REVERTIR-[Año] (solo Admin).\n");
        sb.append("- Identidad y Membrete: Hoja membretada oficial configurable en /admin (PNG/JPG/WebP máx 5MB) que se dibuja automáticamente como fondo en reportes PDF.\n");
        sb.append("- Bloqueo 15 días: Obras no completadas con >15 días sin editar muestran 'Bloqueada (15 días)' y bloquean el reporte al Supervisor hasta que Admin/Supervisor edite y guarde.\n");
        sb.append("- Documentación: 4 Carpetas (Legal, Social, Técnicos, Anexo Fotográfico) + Checklist Oficial 2026 de 44 requisitos en 4 secciones (OK, F/X, C, N/A). Máx 20 MB checklist / 30 MB general. Anti-duplicados SHA-256. Candado: solo Admin borra archivos.\n");
        sb.append("- Avances y Fotos: Compresión automática a WebP en navegador, visor Lightbox con zoom. Fecha entre Inicio y Fin, <=100%, 'Auditar con IA' calcula Score de Confianza. Notificaciones cada 30s. Bitácora protegida por triggers inalterables.\n\n");

        try {
            List<ObraEntity> obras = obraJpaRepository.findAll().stream()
                    .filter(o -> isAdmin || o.getEstatus() != ObraEstatus.ELIMINADA)
                    .toList();
            sb.append("OBRAS EN VIVO (").append(obras.size()).append("):\n");
            obras.stream().limit(15).forEach(o ->
                    sb.append("- ").append(o.getCodigo()).append(" | ").append(o.getNombre())
                      .append(" | ").append(o.getEstatus())
                      .append(" | ").append(currency.format(o.getMonto() != null ? o.getMonto() : BigDecimal.ZERO))
                      .append(" | Loc: ").append(o.getDireccion() != null ? o.getDireccion() : "Tlaxiaco")
                      .append("\n")
            );
            if (isAdmin) {
                List<UserEntity> users = userJpaRepository.findAll();
                sb.append("USUARIOS (").append(users.size()).append("): ");
                sb.append(users.stream()
                        .map(u -> "@" + u.getUsername() + "(" + (u.getRoles().isEmpty() ? "-" : u.getRoles().iterator().next().getName()) + ")")
                        .collect(Collectors.joining(", ")))
                  .append("\n");
            }
        } catch (Exception ignored) {}

        return sb.toString();
    }

    private boolean isObraBlocked15Days(ObraEntity o) {
        if (o == null || o.getEstatus() == ObraEstatus.COMPLETADA || o.getEstatus() == ObraEstatus.FINALIZADA) {
            return false;
        }
        LocalDateTime ref = o.getUpdatedAt() != null ? o.getUpdatedAt() : o.getCreatedAt();
        if (ref == null) return false;
        return ChronoUnit.DAYS.between(ref, LocalDateTime.now()) > 15;
    }

    private record ScoredObra(ObraEntity obra, int score) {}

    private List<String> extractSearchTokens(String normalizedQuery) {
        String cleaned = normalizedQuery.replaceAll("[^a-z0-9\\s-]", " ");
        String[] parts = cleaned.split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (part.length() >= 3 && !STOP_WORDS.contains(part)) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private BigDecimal extractApproxAmount(String normalizedQuery) {
        Matcher mMill = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*millon").matcher(normalizedQuery);
        if (mMill.find()) {
            return BigDecimal.valueOf(Double.parseDouble(mMill.group(1)) * 1_000_000.0);
        }
        Matcher mMil = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*mil\\b").matcher(normalizedQuery);
        if (mMil.find()) {
            return BigDecimal.valueOf(Double.parseDouble(mMil.group(1)) * 1_000.0);
        }
        Matcher mExact = Pattern.compile("\\$?\\s*(\\d{3,}(?:,\\d{3})*(?:\\.\\d+)?)").matcher(normalizedQuery);
        if (mExact.find()) {
            try {
                return new BigDecimal(mExact.group(1).replace(",", ""));
            } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean containsTokenOrFuzzy(String fieldText, String token) {
        if (fieldText == null || fieldText.isEmpty()) return false;
        if (fieldText.contains(token)) return true;
        if (token.length() < 5) return false;

        int maxDist = token.length() >= 8 ? 2 : 1;
        String[] words = fieldText.split("[^a-z0-9]+");
        for (String w : words) {
            if (Math.abs(w.length() - token.length()) <= maxDist && w.length() >= 4) {
                if (levenshtein(w, token) <= maxDist) {
                    return true;
                }
            }
        }
        return false;
    }

    private int levenshtein(String a, String b) {
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) dp[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[j] = prev;
                } else {
                    dp[j] = 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                }
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    private String normalizeText(String input) {
        if (input == null) return "";
        String nfd = Normalizer.normalize(input, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
