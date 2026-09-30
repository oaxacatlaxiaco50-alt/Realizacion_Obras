package com.obraspublicas.features.ai.application.service;

import com.obraspublicas.features.ai.presentation.dto.AiChatRequest;
import com.obraspublicas.features.ai.presentation.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final SystemKnowledgeService systemKnowledgeService;

    public AiChatResponse processMessage(AiChatRequest request, String username) {
        String prompt = request != null && request.getMessage() != null ? request.getMessage().toLowerCase().trim() : "";
        String conversationId = (request != null && request.getConversationId() != null && !request.getConversationId().isEmpty())
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        String responseContent;
        try {
            responseContent = generateConciseResponse(prompt, username);
        } catch (Exception e) {
            log.error("Error al procesar la respuesta de la IA", e);
            responseContent = "### 🤖 Asistente de IA\n\nNo se pudo procesar la solicitud en este momento. Puedes consultar obras, expedientes o auditoría.";
        }

        List<String> suggestedFollowUps = generateSuggestedFollowUps(prompt);

        return AiChatResponse.builder()
                .response(responseContent)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .suggestedFollowUps(suggestedFollowUps)
                .modelName("Gemma-2B / RAG Knowledge Engine v3")
                .build();
    }

    private String generateConciseResponse(String prompt, String username) {
        // 1. Saludos e Identificación
        if (prompt.contains("hola") || prompt.contains("saludos") || prompt.contains("buenos dias") 
                || prompt.contains("buenas tardes") || prompt.contains("quien eres")) {
            return String.format("¡Hola %s! 👋 Soy el **Asistente Virtual de Obras Públicas**.\n\n" +
                    "¿En qué te puedo ayudar hoy? Puedes preguntarme sobre obras, presupuestos, expedientes o auditoría.", username);
        }

        // 2. ¿Cómo funciona el sistema? / ¿Qué hace el sistema?
        if (prompt.contains("como funciona") || prompt.contains("que hace el sistema") || prompt.contains("para que sirve") 
                || prompt.contains("acerca del sistema") || prompt.contains("funciona el sistema")) {
            return "### ⚙️ ¿Cómo funciona el Sistema de Obras Públicas?\n\n" +
                    "El sistema gestiona de forma integral las obras públicas del municipio:\n\n" +
                    "1. **Registro y Control de Obras**: Registro con código único, presupuesto, fechas, categoría y coordenadas GPS.\n" +
                    "2. **Expedientes Técnicos (57 Documentos)**: Control normativo clasificado en carpetas Social, Técnica y Contratación.\n" +
                    "3. **Control Anti-Duplicados**: Algoritmo por firma Hash (SHA-256) que rechaza archivos repetidos automáticamente.\n" +
                    "4. **Avances Fotográficos**: Registro de evidencias por etapas (*Antes, Durante, Después*).\n" +
                    "5. **Geolocalización en Mapa**: Visualización en mapa interactivo Leaflet de todas las obras del municipio.\n" +
                    "6. **Bitácora e Historial Inalterable**: Registro de auditoría que guarda cada acción ejecutada por los usuarios.";
        }

        // 3. Control de Duplicados / Rechazo de Archivos
        if (prompt.contains("duplicado") || prompt.contains("rechaz") || prompt.contains("mismo archivo") 
                || prompt.contains("hash") || prompt.contains("sha") || prompt.contains("subir archivo")) {
            return "### 🛡️ Detección y Rechazo de Archivos Duplicados\n\n" +
                    "• **Firma Hash SHA-256**: Analiza el contenido binario real del archivo.\n" +
                    "• **Validación de Metadatos**: Verifica el nombre original, la sección del expediente y el tamaño en bytes.\n" +
                    "• **Rechazo Automático**: Si el archivo ya existe en esa obra o expediente, el backend **cancela la subida** y muestra un mensaje explicativo.\n" +
                    "• **Registro en Auditoría**: Guarda una alerta inalterable (`RECHAZO_DOCUMENTO_DUPLICADO`) en la bitácora.";
        }

        // 4. Presupuestos y Comparativos (Mayor/Menor presupuesto)
        if (prompt.contains("cara") || prompt.contains("mayor presupuesto") || prompt.contains("mas grande") 
                || prompt.contains("mas barata") || prompt.contains("menor presupuesto")) {
            return systemKnowledgeService.getHighestAndLowestBudgetObras();
        }

        // 5. Total de obras, métricas o inversión acumulada (SOLO cuando lo solicitan explícitamente)
        if (prompt.contains("resumen") || prompt.contains("cuantas obras") || prompt.contains("total de obras") 
                || prompt.contains("inversion total") || prompt.contains("monto total") || prompt.contains("estatus")) {
            return "### 📊 Resumen Ejecutivo de Obras\n\n" + systemKnowledgeService.getSummaryMetrics();
        }

        // 6. Auditoría, Bitácora e Historial
        if (prompt.contains("auditoria") || prompt.contains("bitacora") || prompt.contains("historial") 
                || prompt.contains("quien modifico") || prompt.contains("quien creo") || prompt.contains("log")) {
            return "### 📜 Bitácora e Historial de Auditoría\n\n" + systemKnowledgeService.getAuditSummary();
        }

        // 7. Guías de uso ("¿Cómo hago X?")
        if (prompt.contains("como creo") || prompt.contains("nueva obra") || prompt.contains("crear obra") || prompt.contains("registrar obra")) {
            return "### 🏗️ Paso a paso: Crear una nueva obra\n\n" +
                    "1. Ve al módulo **Obras** en el menú lateral.\n" +
                    "2. Haz clic en **+ Nueva Obra**.\n" +
                    "3. Ingresa Código, Nombre, Monto, Fechas y Coordenadas GPS.\n" +
                    "4. Guarda la obra para generar automáticamente su expediente técnico.";
        }

        if (prompt.contains("como subo") || prompt.contains("avance") || prompt.contains("fotografia") || prompt.contains("evidencia")) {
            return "### 📸 Paso a paso: Subir Avances Fotográficos\n\n" +
                    "1. Abre la obra deseada en el módulo **Obras**.\n" +
                    "2. Selecciona la pestaña **Avances**.\n" +
                    "3. Elige la etapa (*Antes, Durante, Después*), el % de avance y adjunta la imagen.";
        }

        if (prompt.contains("mapa") || prompt.contains("coordenada") || prompt.contains("geolocalizacion") || prompt.contains("gps")) {
            return "### 🗺️ Geolocalización y Mapas\n\n" +
                    "• Accede a **Geolocalización** para ver los marcadores GPS de cada obra en el mapa Leaflet de Tlaxiaco.\n" +
                    "• Al hacer clic en una ubicación, verás su ficha técnica y porcentaje de avance.";
        }

        // 8. Búsqueda específica de obras por nombre o categoría
        if (prompt.contains("camino") || prompt.contains("agua") || prompt.contains("escuela") 
                || prompt.contains("paviment") || prompt.contains("drenaje") || prompt.contains("ob-") || prompt.contains("buscar")) {
            String cleanKw = prompt.replace("buscar", "").replace("obra", "").replace("obras", "").replace("de", "").trim();
            if (cleanKw.isEmpty()) cleanKw = "obra";
            return systemKnowledgeService.searchObrasByKeyword(cleanKw);
        }

        // 9. Lista de obras completa
        if (prompt.contains("lista") || prompt.contains("obras")) {
            return "### 🏗️ Lista de Obras Registradas\n\n" + systemKnowledgeService.getDetailedObrasList();
        }

        // 10. Expedientes Técnicos / Catálogo de 57 documentos
        if (prompt.contains("expediente") || prompt.contains("documento") || prompt.contains("catalogo") || prompt.contains("requisito")) {
            return "### 📁 Expediente Técnico (57 Documentos Oficiales)\n\n" +
                    "Organizados en 3 secciones normativas:\n" +
                    "• **Parte Social**: Actas de priorización, asambleas y comités.\n" +
                    "• **Parte Técnica**: Proyecto ejecutivo, presupuestos y memoria de cálculo.\n" +
                    "• **Contratación**: Licitación, contrato, estimaciones y finiquito.\n\n" +
                    "💡 *El sistema rechaza automáticamente cualquier archivo duplicado.*";
        }

        // 11. Respuesta directa y concisa para preguntas abiertas varias
        return String.format("### 🤖 Asistente de IA\n\n" +
                "Respecto a tu consulta: *\"%s\"*\n\n" +
                "Puedes pedirme información sobre:\n" +
                "• **Obras**: Lista completa, búsqueda por nombre o presupuesto.\n" +
                "• **Expedientes**: Estructura de documentos y control de duplicados.\n" +
                "• **Auditoría**: Bitácora inalterable e historial de cambios.\n" +
                "• **Guías**: Cómo registrar obras, avances fotográficos o ver el mapa GPS.", prompt);
    }

    private List<String> generateSuggestedFollowUps(String prompt) {
        List<String> followUps = new ArrayList<>();
        if (prompt.contains("funciona") || prompt.contains("sistema")) {
            followUps.add("📊 Resumen ejecutivo de obras");
            followUps.add("🛡️ Control de archivos duplicados");
            followUps.add("📁 Ver catálogo de expedientes");
        } else if (prompt.contains("duplicado") || prompt.contains("rechaz") || prompt.contains("archivo")) {
            followUps.add("📜 Ver bitácora de auditoría");
            followUps.add("📁 Catálogo de 57 documentos");
        } else if (prompt.contains("monto") || prompt.contains("presupuesto") || prompt.contains("cara")) {
            followUps.add("🏗️ Lista completa de obras");
            followUps.add("📊 Resumen de inversión total");
        } else {
            followUps.add("⚙️ ¿Cómo funciona el sistema?");
            followUps.add("📊 Resumen general de obras");
            followUps.add("🏆 ¿Cuál es la obra con mayor presupuesto?");
        }
        return followUps;
    }
}
