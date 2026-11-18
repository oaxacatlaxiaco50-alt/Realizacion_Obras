package com.obraspublicas.shared.licensing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

/**
 * Servicio de Seguridad de Licenciamiento y Auto-Sellado por Hardware (Machine Binding).
 * Protege la propiedad intelectual del Sistema ResidenciaObra desarrollado para el
 * H. Ayuntamiento de Heroica Ciudad de Tlaxiaco.
 */
@Slf4j
@Service
public class HardwareLicensingService {

    @Value("${licensing.enabled:false}")
    private boolean licensingEnabled;

    @Value("${licensing.activation-key:}")
    private String activationKey;

    @Value("${licensing.license-dir:/app/license}")
    private String licenseDir;

    // Clave secreta interna del equipo desarrollador para firma HMAC
    private static final String DEV_SECRET = "Tlaxiaco_2025_HeroicaCiudad_DevTeamSecretSignatureKey_Jorge_Kevin_Nancy_Nayeli";
    private static final String LICENSE_FILE_NAME = "residencia_tlaxiaco.lic";

    @EventListener(ApplicationReadyEvent.class)
    public void verifyOrSealHardwareLicense() {
        if (!licensingEnabled) {
            log.info("🛡️ [LICENCIA] Módulo de licenciamiento por hardware en modo DESACTIVADO (Perfil Desarrollo).");
            return;
        }

        log.info("🛡️ [LICENCIA] Verificando sello de hardware del servidor municipal...");

        String currentMachineId = detectHostHardwareFingerprint();
        if (currentMachineId == null || currentMachineId.isBlank()) {
            abortBoot("No fue posible obtener la huella de hardware del servidor físico host (/etc/host-machine-id).");
            return;
        }

        Path licenseFilePath = Paths.get(licenseDir, LICENSE_FILE_NAME);
        File licenseFile = licenseFilePath.toFile();

        // CASO 1: Ya existe un sello previo -> Validar que coincida con la máquina actual
        if (licenseFile.exists()) {
            try {
                String existingContent = Files.readString(licenseFilePath, StandardCharsets.UTF_8).trim();
                String[] parts = existingContent.split("::");
                if (parts.length < 2) {
                    abortBoot("El archivo de licencia institucional está dañado o fue alterado.");
                    return;
                }

                String sealedMachineIdHash = parts[0];
                String expectedSignature = parts[1];

                String currentMachineHash = sha256(currentMachineId);
                String calculatedSignature = generateSignature(currentMachineHash);

                if (!sealedMachineIdHash.equals(currentMachineHash) || !expectedSignature.equals(calculatedSignature)) {
                    abortBoot("DISCORDANCIA DE HARDWARE: Este sistema fue sellado para otro servidor físico. La imagen no puede ejecutarse en esta máquina.");
                    return;
                }

                log.info("✅ [LICENCIA] Servidor validado exitosamente. Licencia válida para el H. Ayuntamiento de Tlaxiaco.");
            } catch (Exception e) {
                abortBoot("Error de lectura al validar el sello de hardware: " + e.getMessage());
            }
            return;
        }

        // CASO 2: Primer arranque (Virgen) -> Exige Clave Maestra y Auto-Sella el Hardware
        log.info("🔐 [LICENCIA] Primer arranque detectado: Procediendo a auto-sellado con el servidor...");
        if (activationKey == null || activationKey.isBlank() || !isValidMasterActivationKey(activationKey)) {
            abortBoot("PRIMER ARRANQUE NO AUTORIZADO: Se requiere la CLAVE MAESTRA DE ACTIVACIÓN en la variable ACTIVATION_KEY para sellar la licencia en este servidor.");
            return;
        }

        try {
            File dir = new File(licenseDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String machineHash = sha256(currentMachineId);
            String signature = generateSignature(machineHash);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String fileContent = machineHash + "::" + signature + "::" + timestamp + "::Heroica_Ciudad_de_Tlaxiaco";
            Files.writeString(licenseFilePath, fileContent, StandardCharsets.UTF_8);

            log.info("════════════════════════════════════════════════════════════════════════════════════");
            log.info("🎉 [LICENCIA] ¡SISTEMA AUTO-SELLADO Y LICENCIADO CON ÉXITO!");
            log.info("🏛️ Licenciado para: H. Ayuntamiento Constitucional de Heroica Ciudad de Tlaxiaco");
            log.info("🔒 Huella Digital Hardware Sellada: {}", machineHash.substring(0, 16) + "...");
            log.info("📅 Fecha y Hora de Sellado: {}", timestamp);
            log.info("════════════════════════════════════════════════════════════════════════════════════");
        } catch (IOException e) {
            abortBoot("Error al escribir el archivo de sello de hardware: " + e.getMessage());
        }
    }

    private String detectHostHardwareFingerprint() {
        // En contenedor Docker en Linux se monta: -v /etc/machine-id:/etc/host-machine-id:ro
        Path hostMachineIdPath = Paths.get("/etc/host-machine-id");
        if (Files.exists(hostMachineIdPath)) {
            try {
                String id = Files.readString(hostMachineIdPath, StandardCharsets.UTF_8).trim();
                if (!id.isBlank()) return id;
            } catch (Exception ignored) {}
        }

        // Respaldo DMI product_uuid en Linux
        Path dmiUuid = Paths.get("/sys/class/dmi/id/product_uuid");
        if (Files.exists(dmiUuid)) {
            try {
                String id = Files.readString(dmiUuid, StandardCharsets.UTF_8).trim();
                if (!id.isBlank()) return id;
            } catch (Exception ignored) {}
        }

        // Respaldo nativo de sistema operativo (para desarrollo / fallback)
        String osName = System.getProperty("os.name", "");
        String userHome = System.getProperty("user.home", "");
        String userName = System.getProperty("user.name", "");
        return osName + "::" + userHome + "::" + userName;
    }

    private boolean isValidMasterActivationKey(String key) {
        // Clave Maestra de Activación oficial
        return "Tlaxiaco2025_MasterActivacionOficial!".equals(key.trim());
    }

    private String generateSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(DEV_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Error al generar firma criptográfica de hardware", e);
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    private void abortBoot(String reason) {
        System.err.println("\n");
        System.err.println("╔════════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.err.println("║                       🚫 BLOQUEO DE SEGURIDAD INSTITUCIONAL — LICENCIA INVÁLIDA                   ║");
        System.err.println("╠════════════════════════════════════════════════════════════════════════════════════════════════════╣");
        System.err.println("║ MOTIVO DEL BLOQUEO:                                                                                ║");
        System.err.println("║ " + padRight(reason, 98) + " ║");
        System.err.println("║                                                                                                    ║");
        System.err.println("║ Esta instalación del 'Sistema de Gestión y Residencia de Obras Públicas' está protegida por ley y   ║");
        System.err.println("║ sellada criptográficamente para ejecutarse ÚNICAMENTE en el servidor físico autorizado del         ║");
        System.err.println("║ H. Ayuntamiento Constitucional de la Heroica Ciudad de Tlaxiaco, Oaxaca.                           ║");
        System.err.println("║                                                                                                    ║");
        System.err.println("║ Para soporte técnico, migración autorizada de servidor o reactivación de licencia, comuníquese con ║");
        System.err.println("║ el equipo de desarrollo de software:                                                               ║");
        System.err.println("║   • Jorge Luis Hernández Matra      (yo.hemati2322@gmail.com)                                      ║");
        System.err.println("║   • Kevin Andrew Osorio Aparicio    (andrewosorio589@gmail.com)                                    ║");
        System.err.println("║   • Nancy Olivera Santiago          (nancy.104.oliver@gmail.com)                                   ║");
        System.err.println("║   • Nayeli Morales López            (nayelhylop3@gmail.com)                                        ║");
        System.err.println("╚════════════════════════════════════════════════════════════════════════════════════════════════════╝\n");

        log.error("🛑 BLOQUEO FATAL DE LICENCIA: {}", reason);
        // Termina el proceso de inmediato para que el contenedor Docker se apague
        System.exit(1);
    }

    private String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) return s.substring(0, n);
        return String.format("%-" + n + "s", s);
    }
}
