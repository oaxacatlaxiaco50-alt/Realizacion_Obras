package com.obraspublicas.features.ai.application.service;

import com.obraspublicas.features.audit.infrastructure.entity.AuditLogEntity;
import com.obraspublicas.features.audit.infrastructure.repository.AuditLogJpaRepository;
import com.obraspublicas.features.obras.infrastructure.entity.ObraEntity;
import com.obraspublicas.features.obras.infrastructure.repository.ObraJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemKnowledgeService {

    private final ObraJpaRepository obraJpaRepository;
    private final AuditLogJpaRepository auditLogJpaRepository;

    @Transactional(readOnly = true)
    public String getSummaryMetrics() {
        List<ObraEntity> obras = obraJpaRepository.findAll();
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        long totalObras = obras.size();
        Map<String, Long> porEstatus = obras.stream()
                .collect(Collectors.groupingBy(o -> o.getEstatus() != null ? o.getEstatus().name() : "DESCONOCIDO", Collectors.counting()));

        BigDecimal montoTotal = obras.stream()
                .map(ObraEntity::getMonto)
                .filter(m -> m != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder();
        sb.append("• **Total de Obras:** ").append(totalObras).append("\n");
        sb.append("• **Inversión Total:** ").append(currency.format(montoTotal)).append("\n");
        sb.append("• **Desglose por Estatus:**\n");
        porEstatus.forEach((estatus, cant) -> 
            sb.append("   - ").append(estatus).append(": ").append(cant).append(" obra(s)\n")
        );

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String getDetailedObrasList() {
        List<ObraEntity> obras = obraJpaRepository.findAll();
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        if (obras.isEmpty()) {
            return "No hay obras registradas en el sistema actualmente.";
        }

        StringBuilder sb = new StringBuilder();
        for (ObraEntity o : obras) {
            sb.append(String.format("• **[%s]** %s\n  *Estatus:* %s | *Monto:* %s | *Categoría:* %s\n",
                    o.getCodigo(),
                    o.getNombre(),
                    o.getEstatus(),
                    currency.format(o.getMonto()),
                    o.getCategoria() != null ? o.getCategoria() : "General"));
        }

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String getAuditSummary() {
        long totalLogs = auditLogJpaRepository.count();
        List<AuditLogEntity> recientes = auditLogJpaRepository.findTop10ByOrderByIdDesc();

        StringBuilder sb = new StringBuilder();
        sb.append("• **Total Eventos Auditados:** ").append(totalLogs).append("\n");
        sb.append("• **Últimos Eventos de Auditoría:**\n");
        for (AuditLogEntity log : recientes) {
            sb.append(String.format("   - [%s] Usuario **%s** ejecutó '%s' en módulo %s (%s)\n",
                    log.getTimestamp() != null ? log.getTimestamp().toString().substring(0, 16) : "Ahora",
                    log.getUsername(),
                    log.getAction(),
                    log.getModule(),
                    log.getDescription() != null ? log.getDescription() : "Sin descripción"));
        }

        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String getSystemCapabilities() {
        return "1. **Expedientes Técnicos**: Catálogo oficial de 57 documentos.\n" +
               "2. **Análisis de Duplicados**: Detección y rechazo automático de archivos duplicados.\n" +
               "3. **Geolocalización**: Coordenadas GPS y mapa interactivo.\n" +
               "4. **Avances Fotográficos**: Fotos de evidencia por fase.\n" +
               "5. **Bitácora y Auditoría**: Trazabilidad completa e inmutable de cambios.\n" +
               "6. **Notificaciones**: Alertas de estado en tiempo real.";
    }
}
