package com.obraspublicas.features.ai.application.service;

import com.obraspublicas.features.ai.presentation.dto.AiChatRequest;
import com.obraspublicas.features.ai.presentation.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiChatService {

    private final SystemKnowledgeService systemKnowledgeService;

    public AiChatResponse processMessage(AiChatRequest request, String username) {
        String prompt = request.getMessage().toLowerCase().trim();
        String conversationId = request.getConversationId() != null && !request.getConversationId().isEmpty()
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        String responseContent = generateConciseResponse(prompt, username);
        List<String> suggestedFollowUps = generateSuggestedFollowUps(prompt);

        return AiChatResponse.builder()
                .response(responseContent)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .suggestedFollowUps(suggestedFollowUps)
                .modelName("Gemma-2B (Spring AI RAG Engine)")
                .build();
    }

    private String generateConciseResponse(String prompt, String username) {
        // Saludos
        if (prompt.contains("hola") || prompt.contains("saludos") || prompt.contains("buenos dias") || prompt.contains("buenas tardes")) {
            return String.format("¡Hola %s! 👋 Soy el **Asistente de IA (Gemma)**.\n\n" +
                    "Puedo responder preguntas concisas sobre las obras, presupuestos, expedientes, bitácora y auditoría.\n\n" +
                    "¿Qué deseas consultar?", username);
        }

        // Detección y Rechazo de Documentos Duplicados
        if (prompt.contains("duplicado") || prompt.contains("mismo documento") || prompt.contains("dos documentos") || prompt.contains("rechaz") || prompt.contains("iguales")) {
            return "### 🛡️ Detección y Rechazo de Documentos Duplicados\n\n" +
                    "El sistema analiza automáticamente cada archivo subido:\n" +
                    "• **Análisis Inteligente**: Valida nombre original, sección y tamaño de archivo (bytes).\n" +
                    "• **Rechazo Automático**: Si intentas subir dos veces el mismo documento en la misma obra o expediente, el sistema **rechaza** la operación con un mensaje descriptivo.\n" +
                    "• **Registro de Auditoría**: Cada intento de duplicado genera una alerta inalterable en la bitácora (`RECHAZO_DOCUMENTO_DUPLICADO`).";
        }

        // Bitácora y Auditoría de Obras
        if (prompt.contains("auditoria") || prompt.contains("bitacora") || prompt.contains("historial") || prompt.contains("quien") || prompt.contains("modific")) {
            return "### 📜 Auditoría e Historial de Obras\n\n" +
                    systemKnowledgeService.getAuditSummary() +
                    "\n💡 *Todos los eventos de auditoría son de sólo lectura e inalterables.*";
        }

        // Resumen general de obras / estadisticas
        if (prompt.contains("resumen") || prompt.contains("cuantas obras") || prompt.contains("estatus") || prompt.contains("total") || prompt.contains("monto") || prompt.contains("inversion")) {
            return "### 📊 Resumen Ejecutivo de Obras\n\n" +
                    systemKnowledgeService.getSummaryMetrics() +
                    "\n💡 *Puedes pedirme la lista de obras o la auditoría de alguna en específico.*";
        }

        // Lista o detalle de obras
        if (prompt.contains("detalle") || prompt.contains("lista") || prompt.contains("obras")) {
            return "### 🏗️ Lista de Obras Registradas\n\n" +
                    systemKnowledgeService.getDetailedObrasList() +
                    "\n💡 *Selecciona cualquier obra en el módulo de Expedientes para ver sus documentos.*";
        }

        // Expedientes técnicos
        if (prompt.contains("expediente") || prompt.contains("documento") || prompt.contains("requisito") || prompt.contains("catalogo")) {
            return "### 📁 Expedientes Técnicos (57 Documentos)\n\n" +
                    "El catálogo oficial se divide en 3 secciones principales:\n" +
                    "• **Parte Social**: Actas de priorización y comités.\n" +
                    "• **Parte Técnica**: Proyectos ejecutivos y presupuestos.\n" +
                    "• **Contratación**: Licitaciones, contratos y estimaciones.\n\n" +
                    "💡 *El sistema rechaza automáticamente cualquier documento duplicado.*";
        }

        // Avances y fotografías
        if (prompt.contains("avance") || prompt.contains("foto") || prompt.contains("evidencia")) {
            return "### 📸 Registro de Avances\n\n" +
                    "Pasos rápidos para subir evidencias:\n" +
                    "1. Ve a la obra deseada en el **Dashboard**.\n" +
                    "2. Abre la pestaña **Avances**.\n" +
                    "3. Sube fotos (*Antes, Durante o Después*) e ingresa el % de avance físico.";
        }

        // Geolocalización
        if (prompt.contains("geolocalizacion") || prompt.contains("mapa") || prompt.contains("coordenada") || prompt.contains("ruta") || prompt.contains("geocerca")) {
            return "### 🗺️ Geolocalización y Mapas\n\n" +
                    "• Consulta las coordenadas Lat/Long de cada obra en el mapa Leaflet.\n" +
                    "• Configura geocercas y traza rutas de supervisión en campo.";
        }

        // Respuesta por defecto concisa
        return String.format("### 🤖 Asistente de IA (Gemma)\n\n" +
                "Resumen rápido de tu consulta: *\"%s\"*\n\n" +
                "%s\n\n" +
                "¿Deseas detalles sobre obras, expedientes, auditoría o control de duplicados?",
                prompt, systemKnowledgeService.getSummaryMetrics());
    }

    private List<String> generateSuggestedFollowUps(String prompt) {
        List<String> followUps = new ArrayList<>();
        if (prompt.contains("duplicado") || prompt.contains("rechaz")) {
            followUps.add("📜 Ver eventos de auditoría");
            followUps.add("📁 Ver catálogo de 57 documentos");
        } else if (prompt.contains("auditoria") || prompt.contains("bitacora")) {
            followUps.add("🛡️ ¿Cómo funciona la detección de duplicados?");
            followUps.add("📊 Resumen ejecutivo de obras");
        } else if (prompt.contains("obra") || prompt.contains("resumen")) {
            followUps.add("📜 Ver historial de auditoría de obras");
            followUps.add("🛡️ Detección de documentos duplicados");
        } else {
            followUps.add("📊 Resumen general de obras");
            followUps.add("📜 Ver auditoría de obras");
            followUps.add("🛡️ Control de archivos duplicados");
        }
        return followUps;
    }
}
