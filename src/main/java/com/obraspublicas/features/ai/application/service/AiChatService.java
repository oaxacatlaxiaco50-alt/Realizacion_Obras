package com.obraspublicas.features.ai.application.service;

import com.obraspublicas.features.ai.presentation.dto.AiChatRequest;
import com.obraspublicas.features.ai.presentation.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final SystemKnowledgeService systemKnowledgeService;
    private final OllamaClientService ollamaClientService;

    private static final DateTimeFormatter NOW_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy - HH:mm 'hs'", new Locale("es", "MX"));

    private final Map<String, String> lastTopicByConversation = new ConcurrentHashMap<>();
    private final Map<String, List<OllamaClientService.ChatTurn>> conversationHistories = new ConcurrentHashMap<>();

    private enum AccessRule {
        ALL,
        ADMIN_ONLY,
        ADMIN_OR_SUPERVISOR,
        CAN_UPLOAD,
        CAN_EXPORT
    }

    private record KnowledgeEntry(
            String id,
            AccessRule accessRule,
            String deniedActionLabel,
            String deniedReason,
            List<String> strongPhrases,
            List<String> keywords,
            Function<UserContext, String> answerBuilder
    ) {}

    private record UserContext(String username, String role, String rawPrompt, String normalizedPrompt) {
        boolean isAdmin() { return "ADMINISTRADOR".equals(role); }
        boolean isSupervisor() { return "SUPERVISOR".equals(role); }
        boolean isContratista() { return "CONTRATISTA".equals(role); }
        boolean isAuditor() { return "AUDITOR".equals(role); }
        boolean canCreateOrEditObra() { return isAdmin() || isSupervisor(); }
        boolean canUpload() { return isAdmin() || isSupervisor() || isContratista(); }
        boolean canExport() { return isAdmin() || isSupervisor() || isAuditor(); }
    }

    public AiChatResponse processMessage(AiChatRequest request, String username, String role) {
        String rawPrompt = request != null && request.getMessage() != null ? request.getMessage().trim() : "";
        String normalizedPrompt = normalize(rawPrompt);
        String effectiveRole = (role != null && !role.isBlank()) ? role.toUpperCase() : "AUDITOR";

        String conversationId = (request != null && request.getConversationId() != null && !request.getConversationId().isEmpty())
                ? request.getConversationId()
                : UUID.randomUUID().toString();

        UserContext ctx = new UserContext(username, effectiveRole, rawPrompt, normalizedPrompt);

        String responseContent;
        String matchedTopicId = "general";
        String engineLabel = "Java Rápido";
        try {
            QueryResolution resolution = resolveResponse(ctx, conversationId);
            responseContent = resolution.response();
            matchedTopicId = resolution.topicId();
            engineLabel = resolution.engineLabel();
            if (!"general".equals(matchedTopicId)) {
                lastTopicByConversation.put(conversationId, matchedTopicId);
            }
            recordTurn(conversationId, rawPrompt, responseContent);
        } catch (Exception e) {
            log.error("Error al procesar la respuesta de la IA", e);
            responseContent = "### 🤖 Asistente de IA\n\nOcurrió un error al consultar la base de conocimiento. Intenta nuevamente en unos segundos.";
        }

        List<String> suggestedFollowUps = generateSuggestedFollowUps(matchedTopicId, effectiveRole);

        return AiChatResponse.builder()
                .response(responseContent)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .suggestedFollowUps(suggestedFollowUps)
                .modelName("ResidenciaObra Híbrido (" + engineLabel + ") · Rol: " + effectiveRole)
                .build();
    }

    private void recordTurn(String conversationId, String userMsg, String aiMsg) {
        if (userMsg == null || userMsg.isBlank() || aiMsg == null || aiMsg.isBlank()) return;
        String compactAi = aiMsg.length() > 500 ? aiMsg.substring(0, 497) + "..." : aiMsg;
        conversationHistories.compute(conversationId, (k, list) -> {
            List<OllamaClientService.ChatTurn> next = list != null ? new ArrayList<>(list) : new ArrayList<>();
            next.add(new OllamaClientService.ChatTurn("user", userMsg));
            next.add(new OllamaClientService.ChatTurn("assistant", compactAi));
            if (next.size() > 6) {
                next = new ArrayList<>(next.subList(next.size() - 6, next.size()));
            }
            return next;
        });
    }

    private record QueryResolution(String topicId, String response, String engineLabel) {
        QueryResolution(String topicId, String response) {
            this(topicId, response, "Java Rápido");
        }
    }

    private QueryResolution resolveResponse(UserContext ctx, String conversationId) {
        String p = ctx.normalizedPrompt();
        String fechaActual = LocalDateTime.now().format(NOW_FORMATTER);
        fechaActual = fechaActual.substring(0, 1).toUpperCase() + fechaActual.substring(1);

        // 1. Saludos directos (conservando comportamiento original + contexto de rol)
        if (p.matches("^(hola|buenos dias|buenas tardes|buenas noches|saludos|hey|que tal|quien eres|como estas)[!?., ]*$")
                || (p.length() <= 25 && containsAny(p, "hola", "quien eres", "buenos dias", "buenas tardes"))) {
            return new QueryResolution("saludo", String.format(
                    "¡Hola **%s** (`%s`)! 👋 Soy el **Asistente Virtual de Obras Públicas de Tlaxiaco**.\n\n" +
                    "📅 *%s*\n\n" +
                    "Conozco todo el **Manual de Usuario**, los detalles técnicos del sistema y los datos en vivo de las obras, y respondo según los permisos de tu rol (**%s**).\n\n%s",
                    ctx.username(), ctx.role(), fechaActual, ctx.role(), getQuickCapabilitiesForRole(ctx.role())));
        }

        // 2. Consultas en vivo explícitas: Presupuesto mayor/menor
        if (containsAny(p, "mayor presupuesto", "menor presupuesto", "mas cara", "mas barata", "mas grande", "cual obra cuesta", "obra con mayor", "obra con menor")) {
            return new QueryResolution("presupuestos", systemKnowledgeService.getHighestAndLowestBudgetObras());
        }

        // 3. Consultas en vivo explícitas por código de obra (OBR-...)
        if (p.contains("obr-")) {
            return new QueryResolution("buscar_obra", systemKnowledgeService.smartSearchObrasWithFullDetails(p, ctx.role()));
        }

        // 4. Consultas en vivo de listado general de obras
        if (containsAny(p, "lista de obras", "lista completa de obras", "cuales obras hay", "que obras hay", "obras registradas", "fechas de las obras", "plazos de las obras")) {
            return new QueryResolution("lista_obras", "### 🏗️ Obras Registradas en el Sistema\n\n" + systemKnowledgeService.getDetailedObrasList());
        }

        // 5. Consultas en vivo por estatus específico ("cuales obras estan bloqueadas / inactivas / canceladas / en proceso / papelera")
        if (containsAny(p, "cuales obras", "que obras", "obras estan", "mostrar obras", "ver las obras")
                && containsAny(p, "bloqueada", "bloqueadas", "inactiva", "inactivas", "inativa", "cancelada", "canceladas", "en proceso", "planificada", "completada", "eliminada", "papelera")) {
            return resolveObrasByStatusQuery(ctx);
        }

        // 6. Evaluar catálogo granular de micro-intenciones y Manual del Usuario
        List<KnowledgeEntry> catalog = buildKnowledgeCatalog();
        KnowledgeEntry bestEntry = null;
        int bestScore = 0;

        for (KnowledgeEntry entry : catalog) {
            int score = scoreEntry(p, entry);
            if (score > bestScore) {
                bestScore = score;
                bestEntry = entry;
            }
        }

        // 7. Si la pregunta describe una obra ("la obra que está en San Bartolo y es de calle", "la de 10 millones", "dame los datos de la obra...")
        //    y NO ganó claramente una regla/guía del manual (bestScore < 6), buscar inteligentemente en la BD primero.
        boolean looksLikeSpecificObraLookup = containsAny(p,
                "esta en", "ubicada en", "ubicado en", "en el barrio", "en la calle", "en la colonia", "avenida",
                "san bartolo", "san miguel", "tlaxiaco", "cuesta", "millon", "millones", "datos de la obra",
                "datos de una obra", "informacion de la obra", "no se el nombre", "no me se el nombre",
                "se llama", "que obra es", "cual obra", "busca", "buscar");

        if (looksLikeSpecificObraLookup && bestScore < 7) {
            String obraResult = systemKnowledgeService.smartSearchObrasWithFullDetailsOrNull(p, ctx.role());
            if (obraResult != null) {
                return new QueryResolution("buscar_obra", obraResult);
            }
        }

        // 8. Respuesta rápida de alta confianza en Java (< 0.1s) o bloqueo estricto de permisos
        if (bestEntry != null && bestScore >= 3 && !hasAccess(ctx, bestEntry.accessRule())) {
            return new QueryResolution(
                    bestEntry.id(),
                    buildDenial(ctx.role(), bestEntry.deniedActionLabel(), bestEntry.deniedReason(), getPermittedAlternative(ctx.role()))
            );
        }

        if (bestEntry != null && bestScore >= 6) {
            return new QueryResolution(bestEntry.id(), bestEntry.answerBuilder().apply(ctx));
        }

        // 9. Búsqueda inteligente en BD sobre cualquier descripción libre de obra (lugar, meta, responsable, monto)
        String fuzzyObra = systemKnowledgeService.smartSearchObrasWithFullDetailsOrNull(p, ctx.role());
        if (fuzzyObra != null && bestScore < 4) {
            return new QueryResolution("buscar_obra", fuzzyObra);
        }

        // 10. NIVEL 2 HÍBRIDO: Preguntas abiertas, complejas o fuera del catálogo exacto -> Gemma (Ollama local)
        if (ollamaClientService.isEnabled()) {
            String systemPrompt = systemKnowledgeService.buildCompactSystemPromptForGemma(ctx.role());
            List<OllamaClientService.ChatTurn> history = conversationHistories.getOrDefault(conversationId, List.of());
            Optional<String> gemmaReply = ollamaClientService.askGemma(systemPrompt, history, ctx.rawPrompt());
            if (gemmaReply.isPresent()) {
                return new QueryResolution(
                        bestEntry != null ? bestEntry.id() : "gemma_libre",
                        gemmaReply.get(),
                        "Gemma · " + ollamaClientService.getModelName()
                );
            }
        }

        // 11. Respaldo automático en Java si Ollama no respondió o estuvo apagado
        if (bestScore < 3 && p.length() < 40 && lastTopicByConversation.containsKey(conversationId)) {
            if (containsAny(p, "como", "pasos", "explica", "por que", "donde", "detalle", "mas info", "cuales")) {
                String lastId = lastTopicByConversation.get(conversationId);
                for (KnowledgeEntry entry : catalog) {
                    if (entry.id().equals(lastId)) {
                        if (!hasAccess(ctx, entry.accessRule())) {
                            return new QueryResolution(entry.id(),
                                    buildDenial(ctx.role(), entry.deniedActionLabel(), entry.deniedReason(), getPermittedAlternative(ctx.role())));
                        }
                        return new QueryResolution(entry.id(), entry.answerBuilder().apply(ctx));
                    }
                }
            }
        }

        if (bestEntry != null && bestScore >= 3) {
            return new QueryResolution(bestEntry.id(), bestEntry.answerBuilder().apply(ctx));
        }

        if (fuzzyObra != null) {
            return new QueryResolution("buscar_obra", fuzzyObra);
        }

        // 12. Respuesta guía adaptada al rol
        return new QueryResolution("general", String.format(
                "### 🤖 Asistente Virtual · Manual y Consultas (`%s`)\n\n" +
                "📅 *%s*\n\n" +
                "No encontré una coincidencia exacta para *\"%s\"*, pero puedes preguntarme cualquier duda puntual o describir una obra aunque no recuerdes su nombre (por calle, barrio, monto o tipo de trabajo).\n\n" +
                "**Ejemplos de lo que puedes preguntarme:**\n%s",
                ctx.role(), fechaActual, ctx.rawPrompt(), getExampleQuestionsForRole(ctx.role())));
    }

    private QueryResolution resolveObrasByStatusQuery(UserContext ctx) {
        String p = ctx.normalizedPrompt();
        if (containsAny(p, "eliminada", "papelera")) {
            if (!ctx.isAdmin()) {
                return new QueryResolution("papelera_eliminar_obra", buildDenial(ctx.role(),
                        "Consultar Obras Eliminadas (Papelera)",
                        "La pestaña **🗑️ Eliminadas (Papelera)** y la gestión de obras borradas son exclusivas del **`ADMINISTRADOR`**.",
                        getPermittedAlternative(ctx.role())));
            }
            return new QueryResolution("obras_estatus", systemKnowledgeService.getObrasByStatusFilter("ELIMINADA", ctx.role()));
        }
        if (containsAny(p, "bloqueada")) {
            return new QueryResolution("obras_bloqueadas", systemKnowledgeService.getObrasByStatusFilter("BLOQUEADAS", ctx.role()));
        }
        if (containsAny(p, "inactiva", "inativa")) {
            return new QueryResolution("obras_bloqueadas", systemKnowledgeService.getObrasByStatusFilter("INACTIVA", ctx.role()));
        }
        if (containsAny(p, "cancelada")) {
            return new QueryResolution("obras_estatus", systemKnowledgeService.getObrasByStatusFilter("CANCELADA", ctx.role()));
        }
        if (containsAny(p, "en proceso")) {
            return new QueryResolution("obras_estatus", systemKnowledgeService.getObrasByStatusFilter("EN_PROCESO", ctx.role()));
        }
        if (containsAny(p, "planificada")) {
            return new QueryResolution("obras_estatus", systemKnowledgeService.getObrasByStatusFilter("PLANIFICADA", ctx.role()));
        }
        if (containsAny(p, "completada")) {
            return new QueryResolution("obras_estatus", systemKnowledgeService.getObrasByStatusFilter("COMPLETADA", ctx.role()));
        }
        return new QueryResolution("lista_obras", systemKnowledgeService.getDetailedObrasList());
    }

    private int scoreEntry(String p, KnowledgeEntry entry) {
        int score = 0;
        for (String phrase : entry.strongPhrases()) {
            if (p.contains(phrase)) {
                score += 7;
            }
        }
        for (String kw : entry.keywords()) {
            if (containsWordOrFuzzy(p, kw)) {
                score += 3;
            }
        }
        return score;
    }

    private boolean hasAccess(UserContext ctx, AccessRule rule) {
        return switch (rule) {
            case ALL -> true;
            case ADMIN_ONLY -> ctx.isAdmin();
            case ADMIN_OR_SUPERVISOR -> ctx.canCreateOrEditObra();
            case CAN_UPLOAD -> ctx.canUpload();
            case CAN_EXPORT -> ctx.canExport();
        };
    }

    private List<KnowledgeEntry> buildKnowledgeCatalog() {
        int totalDocs = systemKnowledgeService.getCatalogoDocumentosCount();
        List<KnowledgeEntry> list = new ArrayList<>();

        // === MICRO-TEMA 1: VER / RECUPERAR CONTRASEÑAS (Respuestas directas al grano) ===
        list.add(new KnowledgeEntry(
                "ver_contrasenas",
                AccessRule.ALL,
                "",
                "",
                List.of("ver contrasena", "ver contrasenas", "puedo ver contrasenas", "ver clave", "ver claves",
                        "cuales son las contrasenas", "saber la contrasena", "recuperar contrasena", "olvide mi contrasena",
                        "se me olvido la contrasena", "requisitos de contrasena", "como debe ser la contrasena"),
                List.of("contrasena", "contrasenas", "password", "clave", "bcrypt", "olvide"),
                ctx -> {
                    String p = ctx.normalizedPrompt();
                    if (containsAny(p, "requisito", "regla", "como debe ser", "por que no me deja guardar")) {
                        return "### 🔐 Requisitos de Contraseña\n\n" +
                               "Para crear o cambiar una contraseña el sistema exige:\n" +
                               "• **Mínimo 8 caracteres**\n" +
                               "• Al menos **1 letra mayúscula**\n" +
                               "• Al menos **1 letra minúscula**\n" +
                               "• Al menos **1 número**";
                    }
                    if (ctx.isAdmin()) {
                        return "### 🔐 ¿Se pueden ver las contraseñas de los usuarios?\n\n" +
                               "**No.** Por seguridad informática, todas las contraseñas se guardan cifradas con **BCrypt** (cifrado unidireccional en la base de datos), por lo que **ningún usuario —ni siquiera el Administrador— puede visualizar contraseñas existentes**.\n\n" +
                               "**¿Qué puedes hacer si alguien olvidó su contraseña?**\n" +
                               "1. Entra a **⚙️ Administración** (`/admin`).\n" +
                               "2. Haz clic en **✏️ Editar** sobre el usuario.\n" +
                               "3. Escribe una **nueva contraseña** (mín. 8 caracteres, mayúscula, minúscula y número) y guarda.";
                    }
                    return "### 🔐 Contraseñas y Seguridad\n\n" +
                           "**No es posible ver contraseñas** porque el sistema las cifra con **BCrypt** en la base de datos.\n\n" +
                           "• Si **olvidaste tu contraseña** o necesitas cambiarla, solicita al **`ADMINISTRADOR`** que te asigne una nueva desde **⚙️ Administración**.\n" +
                           "• *Tip en el Login:* Puedes usar el botón **`👁️`** al escribir tu contraseña para verificar que no haya errores de dedo.";
                }
        ));

        // === MICRO-TEMA 2: ASIGNAR OBRA A UN USUARIO / CAMBIAR RESPONSABLE ===
        list.add(new KnowledgeEntry(
                "asignar_obra_usuario",
                AccessRule.ADMIN_OR_SUPERVISOR,
                "Asignar o Cambiar Responsable de una Obra",
                "Solo el **`ADMINISTRADOR`** y el **`SUPERVISOR`** pueden crear obras o reasignar al responsable a cargo.",
                List.of("asigno una obra", "asignar una obra", "asignar obra a un usuario", "asignar responsable",
                        "cambiar responsable", "cambiar de responsable", "quien esta a cargo", "darle una obra"),
                List.of("asigno", "asignar", "responsable"),
                ctx -> "### 👤 ¿Cómo asigno una obra a un usuario (Responsable)?\n\n" +
                       "**Al crear una obra nueva:**\n" +
                       "1. Ve a **📁 Expedientes** → **`+ Nueva Obra`**.\n" +
                       "2. En el campo **Responsable a Cargo \\***, selecciona al usuario de la lista desplegable.\n\n" +
                       "**En una obra que ya existe:**\n" +
                       "1. Abre el expediente de la obra (`/obras/:id`) y haz clic en **`✏️ Editar Obra`**.\n" +
                       "2. Cambia el selector **Responsable a Cargo** al nuevo usuario y presiona **`💾 Guardar Cambios`**.\n\n" +
                       "🔔 *Efecto automático:* Al guardar, el nuevo responsable recibe de inmediato la notificación individual **`\"Nueva obra asignada\"`** en su campanita (sin duplicar alertas al anterior)."
        ));

        // === MICRO-TEMA 3: ELIMINAR USUARIO QUE TIENE OBRAS / DESACTIVAR VS ELIMINAR ===
        list.add(new KnowledgeEntry(
                "usuario_integridad",
                AccessRule.ADMIN_ONLY,
                "Eliminar o Desactivar Cuentas de Usuario",
                "La administración y eliminación de cuentas de usuario es exclusiva del **`ADMINISTRADOR`**.",
                List.of("usuario tiene obras", "eliminar usuario con obras", "no me deja eliminar usuario", "diferencia entre desactivar y eliminar",
                        "si borro un usuario", "usuario duplicado", "correo duplicado"),
                List.of("desactivar", "permanente", "duplicado"),
                ctx -> "### 👥 Reglas al Desactivar o Eliminar un Usuario (`ADMINISTRADOR`)\n\n" +
                       "• **Desactivar (Soft Delete):** Revoca el acceso al sistema (`active = false`) sin borrar el historial en bitácoras; puedes reactivarlo en cualquier momento.\n" +
                       "• **Eliminar Permanentemente:** Solo se permite si el usuario está inactivo y **NO tiene obras asignadas como responsable** (para no dejar obras huérfanas). Si tiene obras, primero reasigna esas obras a otro responsable en `✏️ Editar Obra`.\n" +
                       "• **Validación Anti-Duplicados:** El sistema impide registrar un `username` o `correo` que ya exista en otra cuenta."
        ));

        // === MICRO-TEMA 4: GESTIÓN DE USUARIOS (Solo Admin) ===
        list.add(new KnowledgeEntry(
                "usuarios",
                AccessRule.ADMIN_ONLY,
                "Gestionar o Consultar Usuarios",
                "La creación, edición, desactivación, reactivación, eliminación y consulta de cuentas en **⚙️ Administración** es una facultad **exclusiva del `ADMINISTRADOR`** por seguridad y control de accesos.",
                List.of("como creo un usuario", "crear usuario", "nuevo usuario", "agregar usuario", "registrar usuario",
                        "editar usuario", "eliminar usuario", "desactivar usuario", "reactivar usuario",
                        "cuantos usuarios", "lista de usuarios", "gestionar usuarios", "crear un nuevo usuario"),
                List.of("usuario", "usuarios", "administracion", "admin", "reactivar", "cuenta", "cuentas"),
                ctx -> {
                    String p = ctx.normalizedPrompt();
                    StringBuilder sb = new StringBuilder("### 👥 Módulo de Administración de Usuarios (`/admin`)\n\n");
                    if (containsAny(p, "cuantos", "lista", "registrados", "quienes", "ver usuarios", "resumen")) {
                        sb.append(systemKnowledgeService.getUsersSummaryForAdmin()).append("\n");
                    }
                    sb.append("**1. Crear un Nuevo Usuario:**\n")
                      .append("• Ve a **⚙️ Administración** y haz clic en **`+ Nuevo Usuario`**.\n")
                      .append("• Captura: **Nombre(s)**, **Apellidos**, **Correo electrónico**, **Username**, **Contraseña** (mín. 8 caracteres, mayúscula, minúscula y número) y elige su **Rol** (`ADMINISTRADOR`, `SUPERVISOR`, `CONTRATISTA` o `AUDITOR`).\n\n")
                      .append("**2. Editar / Desactivar / Reactivar / Eliminar:**\n")
                      .append("• Usa **`✏️ Editar`** para cambiar datos, rol o asignar una nueva contraseña.\n")
                      .append("• Usa **Desactivar** / **Reactivar** para controlar el acceso conservando historial, o **Eliminar Definitivo** si está inactivo y sin obras asignadas.");
                    if (!containsAny(p, "cuantos", "lista", "registrados", "quienes", "ver usuarios")) {
                        sb.append("\n\n").append(systemKnowledgeService.getUsersSummaryForAdmin());
                    }
                    return sb.toString();
                }
        ));

        // === MICRO-TEMA 5: AGREGAR CONTRATISTA EN CHECKLIST / FICHA TÉCNICA ===
        list.add(new KnowledgeEntry(
                "contratista_en_checklist_pdf",
                AccessRule.ADMIN_OR_SUPERVISOR,
                "Agregar o Modificar el Contratista de una Obra",
                "Solo el **`ADMINISTRADOR`** y el **`SUPERVISOR`** pueden editar la razón social del contratista o los datos técnicos de la obra.",
                List.of("contratista a una obra en el checklist", "contratista en el checklist", "contratista en el chk list",
                        "agregar contratista", "agregar el contratista", "no sale el contratista en el pdf", "razon social del contratista"),
                List.of("contratista", "razon social", "chk", "checklist"),
                ctx -> "### 🏢 ¿Cómo agregar el Contratista para que aparezca en el Checklist / PDF?\n\n" +
                       "El campo **CONTRATISTA** del formato oficial *Integración de Expediente Unitario* (y de la Ficha Técnica) no se captura dentro de las filas del checklist, sino en la ficha editable de la obra:\n\n" +
                       "1. Abre la obra deseada en **📁 Expedientes** (`/obras/:id`).\n" +
                       "2. Haz clic en el botón rojo **`✏️ Editar Obra`**.\n" +
                       "3. Baja hasta la sección **📄 Datos para Ficha Técnica y Expediente** y escribe la razón social en **Contratista (Razón Social)** (ej. *Constructora del Sur S.A. de C.V.*).\n" +
                       "4. Presiona **`💾 Guardar Cambios`** en la parte superior.\n\n" +
                       "✅ Al generar el **PDF 1. Integración de Expediente Unitario**, el nombre aparecerá automáticamente en el recuadro **CONTRATISTA:**."
        ));

        // === MICRO-TEMA 6: DOCUMENTOS EXACTOS POR SECCIÓN DEL CHECKLIST ===
        list.add(new KnowledgeEntry(
                "documentos_por_seccion_checklist",
                AccessRule.ALL,
                "",
                "",
                List.of("documentos de la parte social", "documentos pide parte social", "requisitos de parte social",
                        "documentos del proyecto ejecutivo", "requisitos de proyecto ejecutivo", "documentos de parte tecnica",
                        "documentos de contratacion", "requisitos de contratacion",
                        "documentos comprobatorios", "requisitos comprobatorios", "que documentos lleva el checklist", "cuales son los 44 requisitos"),
                List.of("parte social", "proyecto ejecutivo", "parte tecnica", "procesos de contratacion", "documentos comprobatorios", "secciones"),
                ctx -> systemKnowledgeService.getChecklistDocumentsBySection(ctx.normalizedPrompt())
        ));

        // === MICRO-TEMA 7: SIMBOLOGÍA DEL CHECKLIST (OK, F, C, N/A, X) ===
        list.add(new KnowledgeEntry(
                "simbologia_checklist",
                AccessRule.ALL,
                "",
                "",
                List.of("que significa ok", "simbologia", "significa f", "significa c", "significa n/a", "sale x en el pdf", "porque sale x"),
                List.of("simbologia", "faltante", "corregir", "aplica"),
                ctx -> "### 🏷️ Simbología del Checklist Oficial (Expediente Unitario)\n\n" +
                       "• **`🟢 OK` (Completo):** Documento entregado, revisado y validado.\n" +
                       "• **`🔴 F` (Faltante):** Documento aún no entregado o pendiente *(en el PDF impreso oficial aparece como **`X`**)*.\n" +
                       "• **`🟡 C` (A Corregir):** Documento con observaciones que requiere corrección o complemento.\n" +
                       "• **`⚪ N/A` (No Aplica):** Requisito que no aplica según la modalidad o naturaleza de la obra.\n\n" +
                       "💡 *Los porcentajes de avance de cada sección cuentan como completados los requisitos en `OK` y `N/A`.*"
        ));

        // === MICRO-TEMA 8: TAMAÑOS MÁXIMOS Y FORMATOS PERMITIDOS DE ARCHIVOS ===
        list.add(new KnowledgeEntry(
                "tamanos_formatos_archivo",
                AccessRule.ALL,
                "",
                "",
                List.of("cuanto puede pesar", "tamano maximo", "peso maximo", "limite de tamano", "que formatos acepta", "no me deja subir"),
                List.of("mb", "tamano", "peso", "formato", "formatos", "dwg", "pdf", "video"),
                ctx -> "### 📦 Límites de Peso y Formatos Aceptados\n\n" +
                       "• **Archivos Generales y Evidencias (`📁 Archivos y Evidencias`):** Máximo **30 MB** por archivo. Acepta imágenes, videos, `.pdf` y planos `.dwg`.\n" +
                       "• **Documentos del Checklist Oficial (`44 requisitos`):** Máximo **20 MB** por requisito. Acepta `.pdf` e imágenes.\n" +
                       "• **Foto en modal de edición:** Máximo **10 MB** (`JPG`, `PNG`, `WEBP`).\n\n" +
                       "⚠️ *Si un archivo está dentro del peso y aun así se rechaza, verifica que no sea un **archivo duplicado** en la misma obra.*"
        ));

        // === MICRO-TEMA 9: CANDADO EN ARCHIVOS (🔒 Candado) ===
        list.add(new KnowledgeEntry(
                "candado_archivos",
                AccessRule.ALL,
                "",
                "",
                List.of("por que tiene candado", "candado en archivo", "no me deja borrar archivo", "no puedo eliminar archivo", "quien puede eliminar archivos"),
                List.of("candado", "borrar archivo", "eliminar archivo", "eliminar foto"),
                ctx -> "### 🔒 ¿Por qué los archivos muestran `Candado`?\n\n" +
                       "Por seguridad e integridad del expediente municipal, **los archivos y fotografías subidos quedan protegidos con 🔒 Candado** para evitar borrados accidentales o alteración de evidencias.\n\n" +
                       (ctx.isAdmin()
                               ? "✅ Como **`ADMINISTRADOR`**, eres el **único rol** que no tiene candado y sí puede eliminar archivos o fotografías desde el expediente o el visor."
                               : "⛔ Tu rol **`" + ctx.role() + "`** no puede borrar archivos ya integrados; si subiste un documento por error, solicita al **`ADMINISTRADOR`** que lo elimine.")
        ));

        // === MICRO-TEMA 10: POR QUÉ NO APARECE EN "TODAS" (CANCELADAS / INACTIVAS / ELIMINADAS) ===
        list.add(new KnowledgeEntry(
                "por_que_no_aparece_en_todas",
                AccessRule.ALL,
                "",
                "",
                List.of("no aparece en todas", "por que no sale en todas", "donde estan las canceladas", "donde estan las inactivas", "donde estan las eliminadas"),
                List.of("todas", "canceladas", "inactivas", "eliminadas", "aparece"),
                ctx -> "### 🗂️ ¿Por qué las Canceladas, Inactivas y Eliminadas no aparecen en `Todas`?\n\n" +
                       "En **📁 Expedientes (`/obras`)**, la pestaña **`Todas`** muestra únicamente las **obras operativas activas** (*Planificadas, En Proceso y Completadas*) para mantener el padrón limpio.\n\n" +
                       "Para consultar las demás, usa las pestañas específicas:\n" +
                       "• **`🟠 Inactivas`** → Obras pausadas temporalmente.\n" +
                       "• **`🔴 Canceladas`** → Obras suspendidas definitivamente.\n" +
                       (ctx.isAdmin()
                               ? "• **`🗑️ Eliminadas (Papelera)`** → Obras enviadas a papelera (exclusivo Administrador)."
                               : "• *(La papelera `🗑️ Eliminadas` es visible únicamente para el `ADMINISTRADOR`).*")
        ));

        // === MICRO-TEMA 11: COLORES DE METAS Y PORCENTAJES ===
        list.add(new KnowledgeEntry(
                "colores_metas_progreso",
                AccessRule.ALL,
                "",
                "",
                List.of("colores de las metas", "meta azul", "meta verde", "meta amarilla", "por que esta azul", "barra de progreso"),
                List.of("colores", "color", "barra", "ambar", "completado"),
                ctx -> "### 🎨 Código de Colores y Estados en Metas Físicas\n\n" +
                       "• 🔵 **Azul:** Avance menor al **50%** (`< 50%`).\n" +
                       "• 🟡 **Ámbar / Amarillo:** Avance entre **50% y 99%** (`EN_PROCESO`).\n" +
                       "• 🟢 **Verde:** **100% Completado** (al llegar al 100%, la meta cambia automáticamente a estado `COMPLETADO` y ya no pide más capturas)."
        ));

        // === MICRO-TEMA 12: POR QUÉ NO ME DEJA REGISTRAR UN AVANCE / VALIDACIONES DE AVANCES ===
        list.add(new KnowledgeEntry(
                "error_registrar_avance",
                AccessRule.CAN_UPLOAD,
                "Registrar Avances Físicos",
                "El rol **`AUDITOR`** opera en **modo solo lectura**, por lo que no tiene permiso para registrar avances físicos.",
                List.of("no me deja registrar un avance", "no me deja subir avance", "error al registrar avance", "fecha del avance", "supera el 100"),
                List.of("rango", "fecha avance", "cantidad ejecutada"),
                ctx -> "### 🎯 Validaciones al Registrar un Avance Físico\n\n" +
                       "Si el sistema no te permite guardar un avance en **🎯 Actualizar Avances**, revisa estas 3 reglas:\n" +
                       "1. **Rango de Fechas:** La fecha del avance debe estar dentro de la **Fecha de Inicio** y **Fecha de Fin** de la obra.\n" +
                       "2. **Tope del 100%:** La cantidad ejecutada + acumulado no puede superar la **Cantidad Meta** total.\n" +
                       "3. **Cantidad positiva:** Debe ser mayor a `0` y la meta no debe estar ya en estado `COMPLETADO`."
        ));

        // === MICRO-TEMA 13: AUDITORÍA IA DE AVANCES (`🤖 Auditar con IA`) Y RECHAZAR AVANCE ===
        list.add(new KnowledgeEntry(
                "auditoria_ia_avances",
                AccessRule.ALL,
                "",
                "",
                List.of("auditar con ia", "auditoria con ia", "score de confianza", "rechazar avance", "como funciona la auditoria ia"),
                List.of("auditar", "score", "confianza", "rechazar avance"),
                ctx -> "### 🤖 ¿Cómo funciona `Auditar con IA` en los Avances?\n\n" +
                       "Dentro de la pestaña **🎯 Actualizar Avances → *Ver Historial de Avances*** de cada meta, cada registro tiene el botón **`🤖 Auditar con IA`**:\n" +
                       "• Analiza la coherencia del avance reportado, cantidades y evidencias, devolviendo un **Score de Confianza (0–100%)** y una lista de observaciones.\n" +
                       "• Si el Score es menor a `100%`, muestra el botón **Rechazar Avance** para revertir ese registro si corresponde."
        ));

        // === MICRO-TEMA 14: ROLES Y PERMISOS DEL SISTEMA ===
        list.add(new KnowledgeEntry(
                "roles_permisos",
                AccessRule.ALL,
                "",
                "",
                List.of("que puedo hacer", "que no puedo hacer", "mis permisos", "roles y permisos", "matriz de permisos",
                        "que hace cada rol", "diferencia entre roles", "que puede hacer el", "permisos del"),
                List.of("permiso", "permisos", "rol", "roles", "acceso", "restriccion", "restricciones", "rbac"),
                ctx -> {
                    if (ctx.isAdmin()) {
                        return "### 🔑 Roles y Permisos del Sistema (Matriz Completa)\n\n" +
                               "1. **👑 `ADMINISTRADOR` (Control Total):**\n" +
                               "   • ✅ Gestionar Usuarios, Crear/Editar/Eliminar Obras, Papelera (30 días), Cambiar Estatus, Subir y Eliminar Archivos (sin candado), Consultar Bitácoras/Logs de Auditoría y Exportar PDF/Excel.\n\n" +
                               "2. **👷 `SUPERVISOR` (Gestión Operativa):**\n" +
                               "   • ✅ Crear/Editar Obras, Metas, Rutas en Mapa, Cambiar Estatus, Subir Archivos/Avances, Consultar Bitácora General y Exportar PDF/Excel.\n" +
                               "   • ❌ No gestiona usuarios, no ve Logs de Auditoría/Bitácora de Cambios ni elimina obras/archivos.\n\n" +
                               "3. **🏗️ `CONTRATISTA` (Carga de Evidencias y Avances):**\n" +
                               "   • ✅ Consultar Obras/Expedientes/Mapa/Historial y Subir Archivos, Checklist y Avances.\n" +
                               "   • ❌ No gestiona usuarios, no crea/edita obras, no cambia estatus, no elimina archivos (🔒) ni exporta PDF/Excel.\n\n" +
                               "4. **🔍 `AUDITOR` (Fiscalización · Solo Lectura):**\n" +
                               "   • ✅ Consultar Dashboard, Mapa, Historial, Carpetas y Checklist (ver/descargar) y Exportar Reportes PDF/Excel.\n" +
                               "   • ❌ No gestiona usuarios, no modifica obras/estatus y no puede subir archivos ni avances.";
                    }
                    return "### 🔑 Tus Permisos Actuales (`" + ctx.role() + "`)\n\n" + getQuickCapabilitiesForRole(ctx.role());
                }
        ));

        // === MICRO-TEMA 15: OBRAS BLOQUEADAS (15 DÍAS) E INACTIVAS ===
        list.add(new KnowledgeEntry(
                "obras_bloqueadas",
                AccessRule.ALL,
                "",
                "",
                List.of("obra bloqueada", "esta bloqueada", "como desbloquear", "puedo desbloquear", "15 dias", "sin actualizaciones recientes",
                        "obra inactiva", "obras inactivas", "obra inativa", "se inactiva", "diferencia entre bloqueada e inactiva"),
                List.of("bloquead", "desbloquear", "inactiva", "inativa", "pausada", "suspendida", "inactividad"),
                ctx -> {
                    StringBuilder sb = new StringBuilder("### 🔒 Obras Bloqueadas (15 días) e Inactivas (`INACTIVA`)\n\n");
                    sb.append("**1. ¿Qué es `🚫 Bloqueada (15 días)`?**\n")
                      .append("• Si una obra **no está `COMPLETADA`** y lleva **más de 15 días** sin editarse (`updatedAt`), aparece en rojo como **Bloqueada (15 días)** y **`🔒 Sin actualizaciones recientes`**.\n")
                      .append("• **Efecto:** Al **`SUPERVISOR`** se le oculta el botón **📄 Generar Reporte** dentro de esa obra hasta que se actualice.\n")
                      .append("• *No cambia su estatus a `INACTIVA` automáticamente.*\n\n")
                      .append("**2. ¿Qué es `🟠 INACTIVA`?**\n")
                      .append("• Es un estatus manual para obras pausadas. No sale en `Todas`; se consulta en la pestaña **🟠 Inactivas**.\n\n");
                    if (ctx.canCreateOrEditObra()) {
                        sb.append("**3. ¿Cómo desbloquearla o reactivarla? (`" + ctx.role() + "`):**\n")
                          .append("• **Quitar bloqueo de 15 días:** Abre la obra → **`✏️ Editar Obra`** → guarda una actualización (**`💾 Guardar Cambios`**) y se desbloquea al instante.\n")
                          .append("• **Reactivar `INACTIVA`:** Ve a **📁 Expedientes → 🟠 Inactivas** → abre la obra → **`✏️ Editar Obra`** → cambia Estatus a `Planificada` o `En Proceso` → **`💾 Guardar Cambios`**.");
                    } else {
                        sb.append("**3. Permisos de tu rol (`" + ctx.role() + "`):**\n")
                          .append("⛔ Tu rol no puede editar obras ni cambiar su estatus; solicita al **`SUPERVISOR`** o **`ADMINISTRADOR`** actualizar la obra.");
                    }
                    return sb.toString();
                }
        ));

        // === MICRO-TEMA 16: PAPELERA (30 DÍAS) Y ELIMINAR OBRA ===
        list.add(new KnowledgeEntry(
                "papelera_eliminar_obra",
                AccessRule.ADMIN_ONLY,
                "Eliminar Obras, Acceder a la Papelera o Borrar Definitivamente",
                "Por seguridad documental, únicamente el **`ADMINISTRADOR`** puede enviar obras a la **🗑️ Papelera**, restaurarlas o borrarlas definitivamente tras **30 días**.",
                List.of("eliminar obra", "borrar obra", "papelera", "restaurar obra", "borre una obra", "obra por equivocacion",
                        "30 dias", "borrar definitivo", "recuperar una obra"),
                List.of("papelera", "restaurar", "definitivo", "eliminadas", "reciclaje", "equivocacion"),
                ctx -> "### 🗑️ Papelera de Obras (Retención de 30 Días)\n\n" +
                       "1. **Enviar a Papelera:** Dentro del expediente → **`✏️ Editar Obra`** → **`🗑️ Eliminar Obra`** (cambia a `ELIMINADA`).\n" +
                       "2. **Restaurar:** Ve a **📁 Expedientes → 🗑️ Eliminadas (Papelera)** → **`♻️ Restaurar`** (vuelve a `PLANIFICADA` intacta).\n" +
                       "3. **Borrado Definitivo (30 días):** Muestra **`🔒 Disponible en Xd`** hasta cumplir **30 días** en papelera; al cumplirse habilita **`🔥 Borrar Definitivo`**."
        ));

        // === MICRO-TEMA 17: CREAR NUEVA OBRA ===
        list.add(new KnowledgeEntry(
                "crear_obra",
                AccessRule.ADMIN_OR_SUPERVISOR,
                "Crear o Registrar Obras Públicas",
                "El registro de nuevas obras públicas está habilitado únicamente para **`ADMINISTRADOR`** y **`SUPERVISOR`**.",
                List.of("crear obra", "nueva obra", "registrar obra", "como creo una obra", "agregar obra", "dar de alta obra"),
                List.of("crear", "nueva", "registrar"),
                ctx -> "### 🏗️ Paso a paso: Cómo Registrar una Nueva Obra (`/obras`)\n\n" +
                       "1. En **📁 Expedientes**, haz clic en **`+ Nueva Obra`**.\n" +
                       "2. Captura: **Nombre** (5–150 caracteres), **Responsable a Cargo** (recibirá notificación 🔔), **Categoría**, **Dirección**, **Fechas Inicio/Fin** (Fin ≥ Inicio), **Presupuesto ($)** (> $0) y **Descripción** (10–500 caracteres).\n" +
                       "3. Marca el punto exacto en el **mini-mapa GPS** y selecciona las **Metas Físicas** con sus cantidades.\n" +
                       "4. Presiona **`💾 Guardar Proyecto`** para generar código `OBR-XXXXXX` y su Checklist de **" + totalDocs + " requisitos**."
        ));

        // === MICRO-TEMA 18: EDITAR OBRA, METAS Y CAMBIAR ESTATUS ===
        list.add(new KnowledgeEntry(
                "editar_obra_estatus",
                AccessRule.ADMIN_OR_SUPERVISOR,
                "Editar Obras, Cambiar Estatus o Gestionar Metas",
                "Solo **`ADMINISTRADOR`** y **`SUPERVISOR`** pueden editar obras, cambiar su estatus (`PLANIFICADA`, `EN_PROCESO`, `COMPLETADA`, `INACTIVA`, `CANCELADA`) o agregar/eliminar metas.",
                List.of("editar obra", "modificar obra", "cambiar estatus", "estatus de obra", "estado de la obra",
                        "gestor de metas", "agregar meta", "eliminar meta"),
                List.of("editar", "estatus", "financiamiento", "beneficiarios", "meta", "metas"),
                ctx -> "### ✏️ Cómo Editar una Obra, Cambiar Estatus o Metas\n\n" +
                       "1. Abre el expediente (`/obras/:id`) y haz clic en **`✏️ Editar Obra`**.\n" +
                       "2. Modifica Nombre, Responsable, **Estatus** (`Planificada`, `En Proceso`, `Completada`, `Inactiva`, `Cancelada`), Categoría, **Contratista (Razón Social)**, Fuente de Financiamiento, Beneficiarios e Impacto Social.\n" +
                       "3. Abajo en **🎯 Gestor de Metas** puedes agregar o eliminar metas físicas.\n" +
                       "4. Presiona **`💾 Guardar Cambios`**."
        ));

        // === MICRO-TEMA 19: ACTUALIZAR AVANCES FÍSICOS Y SUBIDA GENERAL ===
        list.add(new KnowledgeEntry(
                "avances_ia",
                AccessRule.CAN_UPLOAD,
                "Registrar Avances Físicos o Subir Evidencias",
                "El rol **`AUDITOR`** opera en **modo solo lectura**, por lo que no puede registrar avances ni subir archivos.",
                List.of("actualizar avance", "registrar avance", "reportar avance", "subir avance", "subir evidencia", "subir fotos"),
                List.of("avance", "avances", "ejecutada", "evidencia", "evidencias"),
                ctx -> "### 🎯 Registrar Avances Físicos y Subir Evidencias\n\n" +
                       "• **Avances por Meta:** Ve a `/obras/:id` → pestaña **`🎯 Actualizar Avances`**, elige la fecha (entre Inicio y Fin), captura la cantidad ejecutada y presiona **`Guardar`**.\n" +
                       "• **Evidencias Fotográficas / Archivos:** En la pestaña **`📁 Archivos y Evidencias`**, selecciona destino (*General, Antes, Durante, Después*) y sube archivos de hasta **30 MB**."
        ));

        // === MICRO-TEMA 20: EXPEDIENTE UNITARIO, CARPETAS Y CHECKLIST ===
        list.add(new KnowledgeEntry(
                "expediente_checklist_carpetas",
                AccessRule.ALL,
                "",
                "",
                List.of("integracion de expedientes", "checklist", "44 requisitos", "carpetas", "legal", "anexo fotografico", "subir documento"),
                List.of("expediente", "expedientes", "checklist", "carpeta", "carpetas", "requisitos", "requisito",
                        "documento", "documentos", "pdf", "subir", "archivo", "archivos"),
                ctx -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("### 📂 Expediente Unitario: Carpetas y Checklist Oficial (" + totalDocs + " Requisitos)\n\n")
                      .append("• **4 Carpetas en `📂 Integración de Expedientes`:** **⚖️ Legal**, **👥 Social**, **📐 Técnicos** y **📸 Anexo Fotográfico**.\n")
                      .append("• **Checklist Oficial 2026 (" + totalDocs + " Requisitos):** I. Parte Social, II. Proyecto Ejecutivo, III. Contratación, IV. Documentos Comprobatorios.\n")
                      .append("• **Modal Pantalla Dividida:** Haz clic en cualquier requisito para ver su PDF/imagen a la derecha y gestionar estado (`OK`, `F`, `C`, `N/A`) u observaciones a la izquierda (máx. **20 MB**).\n\n");
                    if (ctx.canUpload()) {
                        sb.append("✅ Con tu rol **`" + ctx.role() + "`** puedes subir archivos, calificar requisitos y guardar observaciones.");
                    } else {
                        String prefix = containsAny(ctx.normalizedPrompt(), "como subo", "subir", "puedo subir", "cargar", "modificar")
                                ? "⛔ **Acción Restringida para tu rol (`AUDITOR`)**\n\nNo puedes **subir archivos ni modificar el Checklist** porque tu rol es de **solo lectura**. Sí puedes abrir las carpetas y requisitos para **ver y descargar (`👁️ Ver Documento`)**.\n\n---\n\n"
                                : "👁️ Con tu rol **`AUDITOR`** tienes **modo solo lectura** para consultar y descargar todos los archivos.\n\n";
                        return prefix + sb;
                    }
                    return sb.toString();
                }
        ));

        // === MICRO-TEMA 21: REPORTES PDF Y EXCEL ===
        list.add(new KnowledgeEntry(
                "reportes",
                AccessRule.CAN_EXPORT,
                "Exportar Reportes PDF y Excel",
                "El rol **`CONTRATISTA`** no tiene habilitada la exportación de reportes PDF ni Excel por confidencialidad presupuestal del Ayuntamiento (reservada a `ADMINISTRADOR`, `SUPERVISOR` y `AUDITOR`).",
                List.of("exportar pdf", "exportar excel", "generar reporte", "ficha tecnica pdf", "hojas del excel", "que trae el excel", "sin iconos"),
                List.of("reporte", "reportes", "exportar", "pdf", "excel", "xlsx", "ficha"),
                ctx -> "### 📄 Reportes Oficiales (PDF y Excel)\n\n" +
                       "**1. En `📁 Expedientes` (`/obras`):**\n" +
                       "• **`📄 Exportar PDF`**: Padrón ejecutivo limpio (sin emojis/símbolos) con totales y espacio de firmas.\n" +
                       "• **`📊 Exportar Excel`**: Libro `.xlsx` con el listado filtrado y totales.\n\n" +
                       "**2. Dentro de cada Expediente (`/obras/:id` → `📄 Generar Reporte`):**\n" +
                       "• **1. Integración de Expediente Unitario (PDF):** Formato oficial 2026 con las 4 secciones.\n" +
                       "• **2. Ficha Técnica (PDF):** Carátula membretada con localidad, metas, financiamiento, beneficiarios e impacto social.\n" +
                       "• **3. Reporte Completo de Avances (Excel):** Libro de 4 hojas (*1. Información General, 2. Metas del Proyecto, 3. Historial de Avances, 4. Resumen por Meta*)."
        ));

        // === MICRO-TEMA 22: MAPA, GEOCERCAS, RUTAS E IMÁN ===
        list.add(new KnowledgeEntry(
                "mapa_rutas",
                AccessRule.ALL,
                "",
                "",
                List.of("como funciona el mapa", "geolocalizacion", "trazar ruta", "dibujar ruta", "geocerca", "geocercas", "iman", "cierre magnetico", "eliminar ruta"),
                List.of("mapa", "geolocalizacion", "ruta", "rutas", "coordenada", "coordenadas", "gps", "leaflet", "trazo", "poligono"),
                ctx -> {
                    String base = "### 🗺️ Módulo de Geolocalización, Geocercas y Rutas (`/mapa`)\n\n" +
                                  "• Mapa interactivo **Leaflet** de Tlaxiaco con marcadores GPS de las obras activas.\n" +
                                  "• Muestra rutas lineales y polígonos (geocercas) con cálculo de longitud y enlace al expediente.\n";
                    if (ctx.canCreateOrEditObra()) {
                        return base + "\n✅ **Edición (`" + ctx.role() + "`):** Puedes trazar rutas/polígonos clic a clic con cierre magnético (**Imán** al vértice inicial) y eliminar trazos con confirmación SweetAlert2.";
                    }
                    return base + "\n👁️ **Modo Consulta (`" + ctx.role() + "`):** Puedes consultar marcadores y trazos; crear o eliminar rutas está reservado a **`ADMINISTRADOR`** y **`SUPERVISOR`**.";
                }
        ));

        // === MICRO-TEMA 23: CONTROL ANTI-DUPLICADOS SHA-256 ===
        list.add(new KnowledgeEntry(
                "anti_duplicados",
                AccessRule.ALL,
                "",
                "",
                List.of("control de duplicados", "archivos duplicados", "rechazo de documentos", "huella digital", "sha-256"),
                List.of("duplicado", "duplicados", "hash", "sha", "rechaza", "rechazo", "repetido"),
                ctx -> "### 🛡️ Control Anti-Duplicados por Firma Hash (SHA-256)\n\n" +
                       "• Al subir un archivo, el servidor calcula su firma binaria **SHA-256** y compara nombre y tamaño.\n" +
                       "• Si el archivo ya fue cargado en la misma obra, **se rechaza automáticamente** y registra el evento `RECHAZO_DOCUMENTO_DUPLICADO` en auditoría."
        ));

        // === MICRO-TEMA 24: LOGS DE AUDITORÍA Y BITÁCORA DE CAMBIOS (Solo Admin) + DIFERENCIAS ===
        list.add(new KnowledgeEntry(
                "auditoria_admin",
                AccessRule.ADMIN_ONLY,
                "Consultar Logs de Auditoría y Bitácora de Cambios",
                "Los módulos **🔍 Logs de Auditoría** (`/audit-logs`) y **🔒 Bitácora de Cambios** (`/bitacora/cambios`) contienen direcciones IP y comparativas técnicas reservadas exclusivamente al **`ADMINISTRADOR`**.",
                List.of("logs de auditoria", "bitacora de cambios", "audit-logs", "estado anterior y nuevo", "direccion ip", "quien modifico", "quien creo", "diferencia entre historial y bitacora"),
                List.of("logs", "audit", "cambios", "ip"),
                ctx -> "### 🔍 Logs de Auditoría y 🔒 Bitácora de Cambios (`ADMINISTRADOR`)\n\n" +
                       "• **📋 Historial General (`/bitacora`):** Cronología visible para todos los roles.\n" +
                       "• **🔒 Bitácora de Cambios (`/bitacora/cambios`):** Comparativa **Estado Anterior vs. Nuevo** (solo Admin).\n" +
                       "• **🔍 Logs de Auditoría (`/audit-logs`):** Usuario, acción, módulo e **IP** (solo Admin).\n" +
                       "• *Integridad:* Protegida por triggers de BD que impiden modificar o borrar registros.\n\n" +
                       systemKnowledgeService.getAuditSummary()
        ));

        // === MICRO-TEMA 25: HISTORIAL GENERAL Y BITÁCORA INALTERABLE ===
        list.add(new KnowledgeEntry(
                "historial_general",
                AccessRule.ALL,
                "",
                "",
                List.of("historial general", "como funciona la bitacora", "ver bitacora", "ultimos movimientos", "borrar bitacora", "eliminar bitacora"),
                List.of("historial", "bitacora", "movimientos", "cronologia", "eventos", "inalterable", "triggers"),
                ctx -> {
                    if (containsAny(ctx.normalizedPrompt(), "borrar bitacora", "eliminar bitacora", "modificar bitacora")) {
                        return "### 🔒 ¿Se puede borrar o editar la Bitácora?\n\n" +
                               "**No.** La bitácora es **inalterable**: está protegida a nivel de base de datos mediante **triggers** que bloquean cualquier `DELETE` o `UPDATE` manual para garantizar transparencia y trazabilidad legal.";
                    }
                    return "### 📋 Historial General (`/bitacora`)\n\n" +
                           "Disponible para los **4 roles** desde **📋 Historial General** en el menú lateral.\n\n" +
                           systemKnowledgeService.getBitacoraGeneralSummary();
                }
        ));

        // === MICRO-TEMA 26: DASHBOARD Y MÉTRICAS EN VIVO ===
        list.add(new KnowledgeEntry(
                "dashboard_resumen",
                AccessRule.ALL,
                "",
                "",
                List.of("como funciona el dashboard", "resumen general", "resumen de obras", "inversion total", "cuantas obras", "indicadores kpi"),
                List.of("dashboard", "resumen", "metricas", "estadistica", "estadisticas", "grafica", "graficas", "inversion", "kpi"),
                ctx -> "### 📊 Dashboard de Control y Métricas en Vivo\n\n" +
                       systemKnowledgeService.getSummaryMetrics() + "\n" +
                       "• En **📊 Dashboard** (`/dashboard`) puedes consultar tarjetas KPI y gráficas estadísticas de distribución y avance."
        ));

        // === MICRO-TEMA 27: BÚSQUEDA, FILTROS Y PESTAÑAS EN EXPEDIENTES ===
        list.add(new KnowledgeEntry(
                "filtros_expedientes",
                AccessRule.ALL,
                "",
                "",
                List.of("filtros de expedientes", "categorias de obra", "como filtrar obras"),
                List.of("filtro", "filtros", "categoria", "categorias", "pestana", "pestanas", "paginacion"),
                ctx -> "### 🔍 Búsqueda, Categorías y Pestañas en `📁 Expedientes`\n\n" +
                       "• **Buscador:** Filtra por código (`OBR-...`), nombre o descripción.\n" +
                       "• **6 Categorías:** Pavimentación y Vialidades, Agua Potable y Drenaje, Electrificación y Alumbrado, Educación y Escuelas, Salud y Espacios Públicos, Infraestructura General.\n" +
                       "• **Pestañas:** `Todas` (solo activas), `Planificadas`, `En Proceso`, `Completadas`, `Inactivas`, `Canceladas`" +
                       (ctx.isAdmin() ? " y `🗑️ Eliminadas (Papelera)`." : ".")
        ));

        // === MICRO-TEMA 28: NOTIFICACIONES ===
        list.add(new KnowledgeEntry(
                "notificaciones",
                AccessRule.ALL,
                "",
                "",
                List.of("como funcionan las notificaciones", "campana de notificaciones", "cada cuanto se actualizan", "nueva obra asignada"),
                List.of("notificacion", "notificaciones", "campana", "campanita", "alerta", "alertas", "leida"),
                ctx -> "### 🔔 Sistema de Notificaciones (`🔔`)\n\n" +
                       "• Se actualiza automáticamente cada **30 segundos**.\n" +
                       "• Recibes alertas individuales cuando te asignan una obra (**Nueva obra asignada**).\n" +
                       "• Al hacer clic sobre una notificación se marca como leída y abre directo el expediente `/obras/:id`."
        ));

        // === MICRO-TEMA 29: INTERFAZ, SESIÓN Y TEMAS ===
        list.add(new KnowledgeEntry(
                "interfaz_sesion_temas",
                AccessRule.ALL,
                "",
                "",
                List.of("como inicio sesion", "iniciar sesion", "cerrar sesion", "cambiar tema", "personalizar colores", "colapsar menu", "barra superior", "menu lateral"),
                List.of("login", "sesion", "salir", "tema", "temas", "colores", "apariencia", "sidebar", "navbar", "interfaz", "colapsar"),
                ctx -> "### 🖥️ Interfaz, Sesión y Personalización de Temas\n\n" +
                       "• **Login / Logout:** Entra en `/login` (usa `👁️` para verificar tu clave) y cierra sesión con **`⏻ Salir`** arriba a la derecha.\n" +
                       "• **Menú Lateral (`◀` / `▶`):** Se puede contraer para ganar espacio.\n" +
                       "• **Temas (`🎨`):** Botón en la barra superior para cambiar tema y colores (se guarda en tu navegador)."
        ));

        // === MICRO-TEMA 30: ACERCA DE Y DESARROLLADORES ===
        list.add(new KnowledgeEntry(
                "acerca_de",
                AccessRule.ALL,
                "",
                "",
                List.of("quienes desarrollaron", "desarrolladores", "acerca de", "correos de contacto", "quien hizo el sistema"),
                List.of("desarrollador", "desarrolladores", "acerca", "equipo", "autores", "kevin", "nancy", "nayeli", "jorge"),
                ctx -> "### ℹ️ Acerca del Sistema (`/acerca-de`)\n\n" +
                       "Desarrollado para el **H. Ayuntamiento Constitucional de Heroica Ciudad de Tlaxiaco**.\n\n" +
                       "• **Kevin Andrew Osorio Aparicio** (`andrewosorio589@gmail.com`)\n" +
                       "• **Nancy Olivera Santiago** (`nancy.104.oliver@gmail.com`)\n" +
                       "• **Nayeli Morales López** (`nayelhylop3@gmail.com`)\n" +
                       "• **Jorge Luis Hernández Matra** (`yo.hemati2322@gmail.com`)"
        ));

        // === MICRO-TEMA 31: LÍNEA DE TIEMPO FOTOGRÁFICA ===
        list.add(new KnowledgeEntry(
                "linea_tiempo_fotos",
                AccessRule.ALL,
                "",
                "",
                List.of("linea de tiempo", "antes durante despues", "visor de fotos", "fotos de la obra"),
                List.of("timeline", "antes", "durante", "despues", "foto", "fotos", "galeria", "fotografia", "fotografias"),
                ctx -> "### 📸 Línea de Tiempo Fotográfica (*Antes, Durante, Después*)\n\n" +
                       "• Clasifica las evidencias en **🔵 Antes**, **🟡 Durante** y **🟢 Después**.\n" +
                       "• Muestra una vista previa de **3 fotos** cuando está minimizada (con botón para desglosar todas) y visor en pantalla completa con descarga" +
                       (ctx.isAdmin() ? " y eliminación (solo Administrador)." : ".")
        ));

        // === MICRO-TEMA 32: ARQUITECTURA, MANUAL COMPLETO Y FUNCIONAMIENTO GENERAL ===
        list.add(new KnowledgeEntry(
                "arquitectura_manual",
                AccessRule.ALL,
                "",
                "",
                List.of("como funciona el sistema", "como esta conformado", "estructura del proyecto", "manual del usuario",
                        "manual del sistema", "de que trata el sistema", "modulos del sistema", "como esta hecho",
                        "para que sirve", "acerca del sistema", "que hace el sistema"),
                List.of("sistema", "proyecto", "arquitectura", "conformado", "estructura", "manual", "modulo", "modulos", "tecnologia", "backend", "frontend"),
                ctx -> systemKnowledgeService.getProjectArchitectureKnowledge(ctx.role())
        ));

        // === MICRO-TEMA 33: CIERRE FISCAL ANUAL (EXCLUSIVO DICIEMBRE) ===
        list.add(new KnowledgeEntry(
                "cierre_fiscal_diciembre",
                AccessRule.ALL,
                "",
                "",
                List.of("cierre fiscal", "por que no puedo hacer cierre", "cierre bloqueado", "cierre en diciembre",
                        "como funciona el cierre fiscal", "ejecutar cierre fiscal", "cierre anticipado", "mes de diciembre",
                        "cerrar el ano fiscal", "cerrar el ejercicio", "cierre anual"),
                List.of("cierre", "fiscal", "diciembre", "anticipado", "ejercicio", "confirmar"),
                ctx -> {
                    StringBuilder sb = new StringBuilder("### 🏛️ Cierre de Ejercicio Fiscal Anual\n\n");
                    sb.append("• **Normativa Gubernamental Obligatoria:** Por lineamientos contables y de administración pública, el Cierre Fiscal **únicamente puede ejecutarse durante el mes de Diciembre**.\n")
                      .append("• **Bloqueo Informativo (Enero a Noviembre):** El sistema bloquea automáticamente la ejecución anticipada en cualquier otro mes para resguardar la legalidad del ejercicio en curso.\n\n")
                      .append("**¿Qué sucede al ejecutar el Cierre Fiscal en Diciembre?**\n")
                      .append("1. Las obras en proceso o planificadas pasan formalmente a **COMPLETADAS** por cierre administrativo.\n")
                      .append("2. Las obras canceladas o eliminadas conservan su estado histórico.\n")
                      .append("3. Todas las obras del ejercicio se trasladan al **🏛️ Archivo Fiscal Histórico** (`/bitacora`).\n")
                      .append("4. El padrón activo en **📁 Expedientes** inicia **completamente limpio en ceros (0 obras)** para recibir el nuevo presupuesto anual.\n\n");
                    if (ctx.isAdmin()) {
                        sb.append("🔐 **Ejecución (`ADMINISTRADOR`):** Ve a **⚙️ Administración** (`/admin`) → tarjeta *Cierre de Ejercicio Fiscal* al final de la página. El sistema te pedirá escribir exactamente la clave de confirmación: `CONFIRMAR-[Año]` (ej. `CONFIRMAR-2026`).");
                    } else {
                        sb.append("⛔ *La ejecución del Cierre Fiscal es una atribución exclusiva del **`ADMINISTRADOR`**.*");
                    }
                    return sb.toString();
                }
        ));

        // === MICRO-TEMA 34: REVERSIÓN DE CIERRE FISCAL (DOBLE ALERTA DE SEGURIDAD) ===
        list.add(new KnowledgeEntry(
                "reversion_cierre_fiscal",
                AccessRule.ADMIN_ONLY,
                "Revertir el Cierre Fiscal de un Ejercicio",
                "La reversión del Cierre Fiscal es una operación de alta responsabilidad reservada exclusivamente al **`ADMINISTRADOR`**.",
                List.of("revertir cierre", "revertir el cierre", "revertir cierre fiscal", "deshacer cierre", "clave revertir",
                        "se cerro por error", "reabrir ejercicio", "como revierto el cierre", "cancelar cierre fiscal"),
                List.of("revertir", "reversion", "reabrir", "deshacer"),
                ctx -> "### 🔄 Procedimiento de Reversión de Cierre Fiscal (`ADMINISTRADOR`)\n\n" +
                       "Si un ejercicio fiscal fue cerrado por error o se requiere registrar movimientos extemporáneos de auditoría, el Administrador puede revertirlo desde **⚙️ Administración** (`/admin`) o desde la pestaña **🏛️ Archivo Fiscal** (`/bitacora`):\n\n" +
                       "**Protocolo de Seguridad en Dos Pasos:**\n" +
                       "1. **Paso 1 — Advertencia Crítica de Consecuencias:** Muestra una alerta roja (*¿Estás completamente seguro?*) detallando que todas las obras archivadas volverán a aparecer en el **Padrón Activo** y en el **Dashboard**, que las que fueron concluidas por cierre regresarán a **`En Proceso`**, y que la acción quedará asentada permanentemente en la **Bitácora de Auditoría**.\n" +
                       "2. **Paso 2 — Confirmación por Clave Obligatoria:** Si se confirma el primer paso, el sistema exige escribir manualmente la clave en mayúsculas: **`REVERTIR-[Año]`** (ejemplo: `REVERTIR-2026`). Si la clave no coincide exactamente, la acción se cancela de inmediato.\n\n" +
                       "✅ Al completarse, las obras regresan íntegras al padrón activo."
        ));

        // === MICRO-TEMA 35: ARCHIVO FISCAL HISTÓRICO DE OBRAS ===
        list.add(new KnowledgeEntry(
                "archivo_fiscal_historico",
                AccessRule.ALL,
                "",
                "",
                List.of("archivo fiscal", "obras archivadas", "obras de anos anteriores", "donde estan las obras del ano pasado",
                        "consultar archivo fiscal", "obras cerradas", "ejercicios anteriores", "donde quedaron las obras"),
                List.of("archivo fiscal", "archivadas", "ano pasado", "ejercicios", "historico"),
                ctx -> "### 🏛️ Archivo Fiscal Histórico de Obras\n\n" +
                       "Todas las obras pertenecientes a ejercicios fiscales cerrados anteriormente se resguardan de forma inmutable en el **Archivo Fiscal**:\n\n" +
                       "• **¿Dónde se consulta?** Ve a **📋 Historial General** (`/bitacora`) en el menú lateral y haz clic en la pestaña **🏛️ Archivo Fiscal**.\n" +
                       "• **Filtros de Auditoría Disponibles:**\n" +
                       "  - **Por Ejercicio Fiscal:** Selector de año (solo muestra años con obras archivadas).\n" +
                       "  - **Por Categoría / Rubro de Obra:** Pavimentación, Agua Potable, Electrificación, Educación, etc.\n" +
                       "  - **Por Mes de Ejecución.**\n" +
                       "  - **Por Estatus Histórico:** Completadas, Canceladas o Eliminadas durante ese año.\n" +
                       "  - **Buscador de Texto:** Localización por nombre, localidad o contratista.\n\n" +
                       "👁️ *Cualquier obra archivada puede abrirse para consultar su ficha técnica, fotos de evidencia y documentos del expediente tal como quedaron al momento del cierre.*"
        ));

        // === MICRO-TEMA 36: MAPA GIS - ZOOM SATELITAL CALIBRADO A NIVEL 18 ===
        list.add(new KnowledgeEntry(
                "zoom_mapa_satelital",
                AccessRule.ALL,
                "",
                "",
                List.of("zoom satelital", "zoom maximo", "por que no da mas zoom", "map data not yet available",
                        "cuadros grises en mapa", "limite de zoom", "zoom del satelite", "zoom 18", "no se puede dar mas zoom"),
                List.of("zoom", "satelite", "satelital", "esri", "gris", "grises", "calibrado"),
                ctx -> "### 🛰️ Calibración del Zoom Satelital en Mapa (`/mapa`)\n\n" +
                       "• **Límite Óptimo en Nivel 18:** El visor satelital de Esri World Imagery está configurado deliberadamente con un tope máximo en el **nivel de zoom 18**.\n" +
                       "• **¿Por qué ya no permite más zoom?** En los niveles 19 en adelante, los servidores satelitales no cuentan con fotografías aéreas de mayor resolución para la zona de Tlaxiaco y generaban mosaicos grises con el mensaje de error *\"Map data not yet available\"*.\n" +
                       "• **Beneficio:** El nivel 18 ofrece la máxima cercanía con nitidez absoluta hasta nivel de predio, banqueta o tramo carretero, garantizando que el mapa **nunca se rompa ni muestre errores visuales**."
        ));

        // === MICRO-TEMA 37: COMPRESIÓN AUTOMÁTICA WEBP Y VISOR LIGHTBOX ===
        list.add(new KnowledgeEntry(
                "compresion_webp_lightbox",
                AccessRule.ALL,
                "",
                "",
                List.of("compresion webp", "formato webp", "fotos pierden calidad", "fotos pesadas", "visor lightbox",
                        "lightbox", "pantalla completa fotos", "hacer zoom a fotos", "optimizar fotos"),
                List.of("webp", "lightbox", "compresion", "optimizar", "galeria", "zoom fotos"),
                ctx -> "### 📸 Compresión Inteligente WebP y Visor Lightbox\n\n" +
                       "• **Compresión Automática a WebP:** Al subir fotografías de evidencia constructiva (*Antes*, *Durante* o *Después*), el navegador las procesa y convierte automáticamente a formato **WebP**.\n" +
                       "  - **Ahorro de Espacio:** Reduce el peso del archivo hasta en un **80%** sin pérdida visual perceptible.\n" +
                       "  - **Rendimiento:** Evita saturar el disco duro del servidor municipal y acelera la carga de reportes.\n" +
                       "• **Visor Lightbox Integrado:** Al hacer clic en cualquier imagen del expediente se abre el visor a pantalla completa:\n" +
                       "  - Controles de zoom interactivo (+ / -) y paneo para revisar detalles de obra.\n" +
                       "  - Navegación rápida entre fotografías y ficha técnica con fecha, hora y descripción de la evidencia."
        ));

        // === MICRO-TEMA 38: IDENTIDAD INSTITUCIONAL Y HOJA MEMBRETADA ===
        list.add(new KnowledgeEntry(
                "hoja_membretada_oficial",
                AccessRule.ALL,
                "",
                "",
                List.of("hoja membretada", "cambiar membrete", "logo en reportes", "membrete municipal",
                        "restablecer membrete", "membrete tlaxiaco", "personalizar membrete", "formato de membrete"),
                List.of("membrete", "hoja", "logotipo", "institucional", "identidad", "fondo"),
                ctx -> {
                    StringBuilder sb = new StringBuilder("### 🏛️ Identidad Institucional y Hoja Membretada Oficial\n\n");
                    sb.append("• **Fondo Dinámico en Reportes:** Todos los reportes PDF generados por el sistema (Fichas Técnicas y Cédulas de Expediente) integran como fondo oficial la Hoja Membretada del H. Ayuntamiento.\n")
                      .append("• **Especificaciones Recomendadas:** Formato **PNG, JPG o WebP** vertical (proporción carta/A4), con resolución de 1200 x 1700 píxeles y peso menor a **5 MB**.\n\n");
                    if (ctx.isAdmin()) {
                        sb.append("**Gestión desde Administración (`ADMINISTRADOR`):**\n")
                          .append("1. Ve a **⚙️ Administración** (`/admin`) → sección **Identidad Institucional**.\n")
                          .append("2. **`Cambiar Hoja Membretada`:** Selecciona la nueva imagen del municipio.\n")
                          .append("3. **`Ver Hoja Completa`:** Abre una vista previa en tamaño real para validar escudos y cintillos.\n")
                          .append("4. **`Restablecer Predeterminada`:** Regresa al diseño original del Ayuntamiento de Tlaxiaco con un solo clic.");
                    } else {
                        sb.append("⛔ *La actualización del membrete institucional es administrada exclusivamente por el **`ADMINISTRADOR`**.*");
                    }
                    return sb.toString();
                }
        ));

        // === MICRO-TEMA 39: DIAGNÓSTICO: POR QUÉ NO ME APARECE UNA OBRA ===
        list.add(new KnowledgeEntry(
                "obra_no_aparece_diagnostico",
                AccessRule.ALL,
                "",
                "",
                List.of("no me aparece una obra", "no veo mi obra", "por que no aparece la obra",
                        "crearon una obra y no la veo", "no encuentro una obra", "obra no sale en la lista", "no sale la obra"),
                List.of("no aparece", "no veo", "no sale", "encuentro", "oculta"),
                ctx -> "### 🔍 Diagnóstico: ¿Por qué no me aparece una obra en `/obras`?\n\n" +
                       "Si te informaron de una obra y no la visualizas en el padrón, verifica estas **4 causas del sistema**:\n\n" +
                       "1. **Asignación por Rol:** Si tu rol es **`SUPERVISOR`** o **`CONTRATISTA`**, el sistema únicamente te muestra las obras donde fuiste designado formalmente como responsable. Solicita al Administrador que verifique tu asignación en `✏️ Editar Obra`.\n" +
                       "2. **Pertenece a un Ejercicio Anterior (Cierre Fiscal):** Si la obra es de un año anterior cerrado, ya no está en `/obras`, sino en **📋 Historial General > pestaña 🏛️ Archivo Fiscal**.\n" +
                       "3. **Está en la Papelera de Reciclaje:** Si fue dada de baja hace menos de 30 días, se encuentra en la pestaña **🗑️ Eliminadas** (visible para Administradores para restaurarla con `♻️ Restaurar`).\n" +
                       "4. **Filtros Activos en Pantalla:** Asegúrate de estar en la pestaña **`Todas`**, limpia el buscador de texto y verifica que los selectores de **Categoría** y **Mes** estén en *\"Todos\"*."
        ));

        // === MICRO-TEMA 40: GUÍA PARA NUEVOS USUARIOS / POR DÓNDE EMPIEZO ===
        list.add(new KnowledgeEntry(
                "nuevo_usuario_orientacion",
                AccessRule.ALL,
                "",
                "",
                List.of("soy nuevo", "por donde empiezo", "que hago aqui", "como empiezo", "guia rapida",
                        "primeros pasos", "que tengo que hacer", "acabo de entrar"),
                List.of("nuevo", "empiezo", "pasos", "inicio", "guia", "orientacion"),
                ctx -> {
                    String base = "### 🚀 Guía de Inicio Rápido · ResidenciaObra\n\nBienvenido al sistema. Tu punto de partida depende de tu encargo en el Ayuntamiento:\n\n";
                    return switch (ctx.role()) {
                        case "SUPERVISOR" -> base +
                                "1. Ve a **📁 Expedientes** para consultar tus obras asignadas.\n" +
                                "2. Entra a una obra y define sus **Metas Físicas** (conceptos medibles con unidad y cantidad programada).\n" +
                                "3. Conforme avancen los trabajos, captura tus avances en **🎯 Actualizar Avances** anexando fotos en etapas *Antes, Durante o Después*.\n" +
                                "4. Abre **📂 Integración de Expedientes** para cargar los documentos normativos de los 4 apartados.\n" +
                                "5. Ve a **🗺️ Geolocalización** para trazar la ruta de la obra sobre el mapa satelital.";
                        case "ADMINISTRADOR" -> base +
                                "1. Revisa **⚙️ Administración**: valida la **Hoja Membretada Municipal** y da de alta las cuentas de usuario en **+ Nuevo Usuario**.\n" +
                                "2. Entra a **📁 Expedientes** y crea las obras del ejercicio en **`+ Nueva Obra`**, asignando a cada residente responsable.\n" +
                                "3. Supervisa el avance financiero y alertas de retraso desde el **📊 Dashboard**.\n" +
                                "4. Al término del año, prepara y ejecuta el Cierre Fiscal anual.";
                        case "CONTRATISTA" -> base +
                                "1. Ingresa a **📁 Expedientes** para consultar las obras contratadas a tu cargo.\n" +
                                "2. En **📂 Integración de Expedientes**, sube tus facturas fiscales CFDI con archivo XML y comprobantes SPEI.\n" +
                                "3. En **🎯 Actualizar Avances**, aporta las estimaciones de obra y evidencias de avance para revisión de la residencia.";
                        default -> base + // AUDITOR
                                "1. Consulta los indicadores globales de inversión y cumplimiento en **📊 Dashboard**.\n" +
                                "2. Inspecciona el catálogo activo en **📁 Expedientes** y revisa el checklist de 44 requisitos de cada obra.\n" +
                                "3. Fiscaliza ejercicios anteriores en **📋 Historial General > 🏛️ Archivo Fiscal**.\n" +
                                "4. Descarga las Cédulas de Integración en PDF y sábanas en Excel desde **📄 Generar Reporte**.";
                    };
                }
        ));

        return list;
    }

    private String getQuickCapabilitiesForRole(String role) {
        return switch (role) {
            case "ADMINISTRADOR" ->
                    "• **Usuarios (`/admin`):** Crear, editar, desactivar, reactivar y eliminar usuarios (y consultar listado en vivo).\n" +
                    "• **Obras y Papelera:** Crear, editar, cambiar estatus, desbloquear (15 días), restaurar o borrar tras 30 días.\n" +
                    "• **Búsqueda Libre:** Descríbeme cualquier obra por calle, barrio, monto, meta o responsable.\n" +
                    "• **Auditoría y Reportes:** Logs de Auditoría, Bitácora de Cambios, Historial y PDF/Excel.";
            case "SUPERVISOR" ->
                    "• **Obras y Estatus:** Crear/editar obras, cambiar estatus, desbloquear alertas de 15 días, metas y rutas.\n" +
                    "• **Expedientes y Avances:** Subir archivos en Carpetas, Checklist y avances físicos.\n" +
                    "• **Búsqueda y Reportes:** Buscar cualquier obra por ubicación/concepto y exportar PDF/Excel.\n" +
                    "• *Restricciones:* Sin acceso a usuarios, logs de seguridad ni eliminación de obras/archivos.";
            case "CONTRATISTA" ->
                    "• **Consulta:** Dashboard, Mapa, Expedientes, Historial General y búsqueda inteligente de obras.\n" +
                    "• **Carga:** Subir archivos en Carpetas (*Legal, Social, Técnicos, Anexo Fotográfico*), Checklist y avances físicos.\n" +
                    "• *Restricciones:* Sin usuarios, crear/editar obras, estatus, borrar archivos (🔒) ni exportar PDF/Excel.";
            default -> // AUDITOR
                    "• **Consulta (Solo Lectura):** Dashboard, Mapa, Historial, Carpetas, Checklist (ver/descargar) y búsqueda de obras.\n" +
                    "• **Reportes:** Exportar reportes oficiales en **PDF y Excel**.\n" +
                    "• *Restricciones:* Sin usuarios, edición de obras/estatus ni subida de archivos/avances.";
        };
    }

    private String getExampleQuestionsForRole(String role) {
        return switch (role) {
            case "ADMINISTRADOR" ->
                    "• *\"¿Puedo ver las contraseñas?\"* o *\"¿Cómo le asigno una obra a un usuario?\"*\n" +
                    "• *\"Dame los datos de la obra que está en San Miguel\"* o *\"¿Cuál obra cuesta 600 mil?\"*\n" +
                    "• *\"¿Cómo agrego al contratista en el checklist?\"*\n" +
                    "• *\"¿Qué documentos pide la Parte Social?\"*";
            case "SUPERVISOR" ->
                    "• *\"¿Cómo desbloqueo una obra de 15 días?\"*\n" +
                    "• *\"¿Cómo le asigno una obra a otro responsable?\"*\n" +
                    "• *\"Dame los datos de la obra de pavimentación en San Bartolo\"*\n" +
                    "• *\"¿Qué significa OK, F, C y N/A?\"*";
            case "CONTRATISTA" ->
                    "• *\"¿Por qué me sale candado en los archivos?\"*\n" +
                    "• *\"¿Por qué no me deja registrar un avance?\"*\n" +
                    "• *\"Dame los datos de la obra del muro de contención\"*\n" +
                    "• *\"¿Qué puedo hacer y qué no con mi rol?\"*";
            default -> // AUDITOR
                    "• *\"¿Qué reportes PDF y Excel puedo exportar?\"*\n" +
                    "• *\"¿Qué documentos pide Contratación en el checklist?\"*\n" +
                    "• *\"Dame los datos de la obra de 10 millones\"*\n" +
                    "• *\"¿Qué pasa si una obra está bloqueada?\"*";
        };
    }

    private String getPermittedAlternative(String role) {
        return switch (role) {
            case "SUPERVISOR" -> "Con tu rol **`SUPERVISOR`** sí puedes crear o editar obras, asignar responsables, cambiar estatus, desbloquear alertas de 15 días, subir archivos/avances, trazar rutas y exportar PDF/Excel.";
            case "CONTRATISTA" -> "Con tu rol **`CONTRATISTA`** sí puedes consultar expedientes, subir archivos y evidencias fotográficas, actualizar el checklist y registrar avances físicos.";
            case "AUDITOR" -> "Con tu rol **`AUDITOR`** sí puedes consultar todas las obras, carpetas y documentos en modo lectura, y exportar los reportes oficiales en PDF y Excel.";
            default -> "";
        };
    }

    private String buildDenial(String role, String actionName, String reason, String alternative) {
        StringBuilder sb = new StringBuilder();
        sb.append("⛔ **Acción Restringida para tu rol (`").append(role).append("`)**\n\n");
        sb.append("No puedes **").append(actionName).append("**.\n\n");
        sb.append("**Motivo:** ").append(reason).append("\n\n");
        if (alternative != null && !alternative.isBlank()) {
            sb.append("💡 ").append(alternative);
        }
        return sb.toString();
    }

    private List<String> generateSuggestedFollowUps(String topicId, String role) {
        if ("cierre_fiscal_diciembre".equals(topicId)) {
            return List.of("🏛️ ¿Dónde consulto las obras archivadas?", "🔄 ¿Cómo se revierte un Cierre Fiscal?", "📁 ¿Cómo inicia el padrón del nuevo año?");
        }
        if ("reversion_cierre_fiscal".equals(topicId)) {
            return List.of("📅 ¿Por qué el Cierre Fiscal solo es en Diciembre?", "🏛️ ¿Cómo funciona el Archivo Fiscal?", "🔍 ¿Dónde se registran los eventos de auditoría?");
        }
        if ("archivo_fiscal_historico".equals(topicId)) {
            return List.of("📅 ¿Cómo funciona el Cierre Fiscal anual?", "🔍 ¿Cómo filtro por categoría o mes?", "📄 ¿Puedo descargar reportes de obras archivadas?");
        }
        if ("zoom_mapa_satelital".equals(topicId)) {
            return List.of("✏️ ¿Cómo trazo una ruta o calle en el mapa?", "🧲 ¿Cómo funciona el enganche magnético (Snap)?", "🗺️ ¿Cómo veo los marcadores de las obras?");
        }
        if ("hoja_membretada_oficial".equals(topicId)) {
            return List.of("📄 ¿Qué reportes PDF oficiales puedo generar?", "🏛️ ¿Dónde se cambia la Hoja Membretada?", "📑 ¿Qué formato y tamaño debe tener la imagen?");
        }
        if ("compresion_webp_lightbox".equals(topicId)) {
            return List.of("📸 ¿Cómo funciona la Línea de Tiempo Fotográfica?", "🎯 ¿Cómo registro avances físicos en metas?", "📦 ¿Cuáles son los límites de peso de archivos?");
        }
        if ("obra_no_aparece_diagnostico".equals(topicId)) {
            return List.of("👤 ¿Cómo se asigna un responsable a una obra?", "🗑️ ¿Cómo restauro una obra de la papelera?", "🏛️ ¿Cómo consulto el Archivo Fiscal?");
        }
        if ("nuevo_usuario_orientacion".equals(topicId)) {
            return List.of("📁 ¿Cómo busco mis obras?", "📂 ¿Qué es el Checklist de 44 requisitos?", "🔑 ¿Cuáles son los permisos de mi rol?");
        }

        if ("ADMINISTRADOR".equals(role)) {
            if ("usuarios".equals(topicId) || "ver_contrasenas".equals(topicId)) {
                return List.of("👤 ¿Cómo le asigno una obra a un usuario?", "🔑 Roles y permisos", "🔒 ¿Qué pasa si una obra está bloqueada?");
            }
            if ("buscar_obra".equals(topicId)) {
                return List.of("📊 Resumen general de obras", "🏆 Mayor y menor presupuesto", "🏢 ¿Cómo agrego el contratista al checklist?");
            }
            return List.of("📅 ¿Cómo funciona el Cierre Fiscal?", "🏛️ ¿Dónde se cambia la Hoja Membretada?", "👤 ¿Cómo asigno una obra a un usuario?");
        }
        if ("SUPERVISOR".equals(role)) {
            return List.of("🔒 ¿Cómo desbloquear una obra?", "👤 ¿Cómo cambio el responsable de una obra?", "📄 ¿Qué trae el Excel de avances?");
        }
        if ("CONTRATISTA".equals(role)) {
            return List.of("🔒 ¿Por qué sale candado en archivos?", "🎯 ¿Por qué no me deja registrar un avance?", "🔑 ¿Qué puedo hacer con mi rol?");
        }
        return List.of("📄 ¿Qué reportes PDF y Excel puedo exportar?", "📋 ¿Qué documentos pide Contratación?", "🔑 ¿Qué puedo hacer con mi rol?");
    }

    private boolean containsWordOrFuzzy(String prompt, String kw) {
        if (prompt.contains(kw)) return true;
        if (kw.contains(" ") || kw.length() < 5) return false;
        int maxDist = kw.length() >= 8 ? 2 : 1;
        String[] words = prompt.split("[^a-z0-9]+");
        for (String w : words) {
            if (Math.abs(w.length() - kw.length()) <= maxDist && w.length() >= 4) {
                if (levenshtein(w, kw) <= maxDist) return true;
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
                dp[j] = (a.charAt(i - 1) == b.charAt(j - 1))
                        ? prev
                        : 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    private String normalize(String input) {
        if (input == null) return "";
        String nfd = Normalizer.normalize(input, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
