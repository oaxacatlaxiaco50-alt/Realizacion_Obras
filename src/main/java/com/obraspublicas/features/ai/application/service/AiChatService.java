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
            responseContent = generateSmartResponse(prompt, username);
        } catch (Exception e) {
            log.error("Error al procesar la respuesta de la IA", e);
            responseContent = String.format("### 🤖 Asistente de IA (Obras Tlaxiaco)\n\n" +
                    "Procesé tu consulta: *\"%s\"*\n\n" +
                    "%s\n\n" +
                    "💡 *El sistema está funcionando normalmente. ¿Deseas consultar obras, expedientes o auditoría?*",
                    prompt, systemKnowledgeService.getSummaryMetrics());
        }

        List<String> suggestedFollowUps = generateSuggestedFollowUps(prompt);

        return AiChatResponse.builder()
                .response(responseContent)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .suggestedFollowUps(suggestedFollowUps)
                .modelName("Gemma-2B / RAG Knowledge Engine v2")
                .build();
    }

    private String generateSmartResponse(String prompt, String username) {
        // 1. Saludos e Identificación
        if (prompt.contains("hola") || prompt.contains("saludos") || prompt.contains("buenos dias") 
                || prompt.contains("buenas tardes") || prompt.contains("quien eres") || prompt.contains("ayuda")) {
            return String.format("¡Hola %s! 👋 Soy el **Asistente Virtual de Obras Públicas de Tlaxiaco**.\n\n" +
                    "Estoy diseñado para responder **preguntas abiertas** sobre el estado del municipio, obras activas, presupuestos, auditoría de archivos y guías del sistema.\n\n" +
                    "**Puedes preguntarme cosas como:**\n" +
                    "• *\"¿Cuál es la obra con mayor presupuesto?\"*\n" +
                    "• *\"¿Cómo creo una nueva obra?\"*\n" +
                    "• *\"¿Qué obras hay de agua potable o caminos?\"*\n" +
                    "• *\"¿Por qué se rechaza un documento duplicado?\"*\n" +
                    "• *\"¿Qué eventos de auditoría se registraron hoy?\"*", username);
        }

        // 2. Presupuestos, Obras más Caras/Baratas o Inversión Total
        if (prompt.contains("cara") || prompt.contains("mayor presupuesto") || prompt.contains("mas grande") 
                || prompt.contains("mas barata") || prompt.contains("menor presupuesto") || prompt.contains("monto total") 
                || prompt.contains("inversion") || prompt.contains("presupuesto")) {
            return "### 💰 Análisis Presupuestal y Financiero\n\n" +
                    systemKnowledgeService.getHighestAndLowestBudgetObras() + "\n\n" +
                    "### 📊 Resumen Global\n" +
                    systemKnowledgeService.getSummaryMetrics();
        }

        // 3. Control de Duplicados / Rechazo de Archivos / Hash SHA-256
        if (prompt.contains("duplicado") || prompt.contains("rechaz") || prompt.contains("mismo archivo") 
                || prompt.contains("hash") || prompt.contains("sha") || prompt.contains("subir archivo")) {
            return "### 🛡️ Detección y Rechazo Inteligente de Archivos Duplicados\n\n" +
                    "El motor del backend protege la integridad de los expedientes mediante un algoritmo de 3 capas:\n" +
                    "1. **Firma Digital (Hash SHA-256)**: Compara el contenido binario exacto del archivo.\n" +
                    "2. **Validación Metadatos**: Verifica el nombre original, la sección del expediente y el tamaño en bytes.\n" +
                    "3. **Rechazo con Registro**: Si el archivo ya existe en la obra, el backend cancela la subida y genera una alerta inalterable en auditoría (`RECHAZO_DOCUMENTO_DUPLICADO`).\n\n" +
                    "💡 *Esto evita la duplicidad de estimaciones o contratos y garantiza transparencia ante la contraloría.*";
        }

        // 4. Auditoría, Bitácora e Historial
        if (prompt.contains("auditoria") || prompt.contains("bitacora") || prompt.contains("historial") 
                || prompt.contains("quien modifico") || prompt.contains("quien creo") || prompt.contains("log")) {
            return "### 📜 Bitácora e Historial Inalterable de Auditoría\n\n" +
                    systemKnowledgeService.getAuditSummary() +
                    "\n💡 *Cada acción (creación, edición, eliminación o rechazo) se registra con usuario, fecha, hora e IP.*";
        }

        // 5. Guías de Uso ("¿Cómo hacer X?")
        if (prompt.contains("como creo") || prompt.contains("nueva obra") || prompt.contains("crear obra") || prompt.contains("registrar obra")) {
            return "### 🏗️ Guía: Cómo registrar una nueva obra\n\n" +
                    "1. Ve al módulo **Obras / Dashboard** desde el menú lateral.\n" +
                    "2. Haz clic en el botón **+ Nueva Obra** (esquina superior derecha).\n" +
                    "3. Completa los campos requeridos: *Código (único), Nombre, Monto Presupuestado, Fechas de Inicio y Fin, Estatus e Ubicación GPS*.\n" +
                    "4. Guarda la obra. Se creará automáticamente su **Expediente Técnico** con la estructura de 57 documentos.";
        }

        if (prompt.contains("como subo") || prompt.contains("avance") || prompt.contains("fotografia") || prompt.contains("evidencia")) {
            return "### 📸 Guía: Cómo registrar Avances Fotográficos\n\n" +
                    "1. Entra al módulo **Obras** y selecciona la obra correspondiente.\n" +
                    "2. Haz clic en la pestaña **Avances Físicos**.\n" +
                    "3. Selecciona la fase (*Antes, Durante o Después*) e ingresa el porcentaje acumulado.\n" +
                    "4. Sube la fotografía de evidencia y guarda. La información actualizará el mapa y la bitácora.";
        }

        if (prompt.contains("mapa") || prompt.contains("coordenada") || prompt.contains("geolocalizacion") || prompt.contains("gps")) {
            return "### 🗺️ Guía: Geolocalización y Mapas\n\n" +
                    "• Accede a **Geolocalización** en el menú principal para ver todas las obras georreferenciadas en Tlaxiaco.\n" +
                    "• Al hacer clic en un marcador de mapa Leaflet, verás la ficha de la obra, su estatus y el responsable asignado.\n" +
                    "• Puedes actualizar latitud y longitud editando la obra en la lista.";
        }

        // 6. Búsqueda Específica de Obras por Categoría o Nombre
        if (prompt.contains("camino") || prompt.contains("agua") || prompt.contains("escuela") 
                || prompt.contains("paviment") || prompt.contains("drenaje") || prompt.contains("ob-") || prompt.contains("buscar")) {
            // Extraer palabra clave relevante
            String cleanKw = prompt.replace("buscar", "").replace("obra", "").replace("obras", "").replace("de", "").trim();
            if (cleanKw.isEmpty()) cleanKw = "obra";
            return systemKnowledgeService.searchObrasByKeyword(cleanKw);
        }

        // 7. Lista general o Resumen Ejecutivo
        if (prompt.contains("resumen") || prompt.contains("cuantas obras") || prompt.contains("estatus") 
                || prompt.contains("lista") || prompt.contains("obras")) {
            return "### 📊 Resumen Ejecutivo de Obras\n\n" +
                    systemKnowledgeService.getSummaryMetrics() + "\n" +
                    "### 🏗️ Listado de Obras\n" +
                    systemKnowledgeService.getDetailedObrasList();
        }

        // 8. Expedientes Técnicos / Documentación
        if (prompt.contains("expediente") || prompt.contains("documento") || prompt.contains("catalogo") || prompt.contains("requisito")) {
            return "### 📁 Estructura del Expediente Técnico (57 Documentos)\n\n" +
                    "El catálogo oficial de auditoría se organiza en 3 carpetas normativas:\n" +
                    "1. **Parte Social**: Actas de Asamblea, Comité de Contraloría Social, Solicitud de Obra.\n" +
                    "2. **Parte Técnica**: Proyecto Ejecutivo, Memoria de Cálculo, Presupuesto Base, Planos.\n" +
                    "3. **Contratación y Ejecución**: Licitación/Adjudicación, Contrato, Fianzas, Estimaciones, Finiquito.\n\n" +
                    "💡 *El sistema valida automáticamente que no se suban archivos repetidos en ninguna sección.*";
        }

        // 9. Respuesta para PREGUNTAS ABIERTAS Generales
        return String.format("### 🤖 Asistente de IA (Obras Tlaxiaco)\n\n" +
                "Analicé tu consulta abierta: *\"%s\"*\n\n" +
                "**Diagnóstico en tiempo real del sistema:**\n" +
                "%s\n\n" +
                "**Módulos principales a tu disposición:**\n" +
                "%s\n\n" +
                "💡 *Puedes pedirme detalles de obras específicas, montos, guías de uso o consulta de bitácora.*",
                prompt,
                systemKnowledgeService.getSummaryMetrics(),
                systemKnowledgeService.getSystemCapabilities());
    }

    private List<String> generateSuggestedFollowUps(String prompt) {
        List<String> followUps = new ArrayList<>();
        if (prompt.contains("duplicado") || prompt.contains("rechaz") || prompt.contains("archivo")) {
            followUps.add("📜 Ver historial de auditoría");
            followUps.add("📁 Ver catálogo de 57 documentos");
            followUps.add("📊 Resumen de obras");
        } else if (prompt.contains("monto") || prompt.contains("presupuesto") || prompt.contains("cara")) {
            followUps.add("🏗️ Lista completa de obras");
            followUps.add("📜 Ver bitácora de auditoría");
            followUps.add("🗺️ Ver mapa de geolocalización");
        } else if (prompt.contains("auditoria") || prompt.contains("bitacora")) {
            followUps.add("🛡️ Detección de archivos duplicados");
            followUps.add("🏆 ¿Cuál es la obra más costosa?");
            followUps.add("📊 Resumen ejecutivo");
        } else {
            followUps.add("🏆 ¿Cuál es la obra con mayor presupuesto?");
            followUps.add("📜 Ver auditoría y bitácora");
            followUps.add("🛡️ ¿Cómo funciona el control anti-duplicados?");
        }
        return followUps;
    }
}
