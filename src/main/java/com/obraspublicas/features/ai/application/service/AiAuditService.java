package com.obraspublicas.features.ai.application.service;

import com.obraspublicas.features.avances.domain.model.ObraAvance;
import com.obraspublicas.features.avances.infrastructure.entity.ObraAvanceEntity;
import com.obraspublicas.features.avances.infrastructure.repository.ObraAvanceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiAuditService {

    private final ObraAvanceJpaRepository avanceJpaRepository;

    public AiAuditResult auditAvance(Long avanceId) {
        Optional<ObraAvanceEntity> optAvance = avanceJpaRepository.findById(avanceId);
        
        if (optAvance.isEmpty()) {
            return new AiAuditResult(0, List.of("El avance no existe en el sistema."), false);
        }

        ObraAvanceEntity avance = optAvance.get();
        return simulateLlmAnalysis(avance);
    }

    private AiAuditResult simulateLlmAnalysis(ObraAvanceEntity avance) {
        List<String> observations = new ArrayList<>();
        int score = 100;
        
        // Simulating AI RAG Analysis of text description
        if (avance.getTitulo() == null || avance.getTitulo().trim().length() < 10) {
            observations.add("El título del avance carece de detalle necesario para la evaluación.");
            score -= 15;
        }

        if (avance.getObservaciones() == null || avance.getObservaciones().trim().length() < 20) {
            observations.add("La descripción carece de detalle necesario para la evaluación de la coherencia físico-financiera.");
            score -= 25;
        }

        // Simulating AI Image/Metadata Analysis
        // In a real scenario, this would send the image EXIF or binary to Gemini Vision API
        boolean hasGps = false; // Simulated extracted metadata
        if (!hasGps) {
            observations.add("Ausencia de georreferencia sobre los estándares de soporte de las imágenes.");
            score -= 20;
        }

        // Logic rule
        if (score > 100) score = 100;
        if (score < 0) score = 0;
        
        boolean isValid = score >= 70;

        if (isValid && observations.isEmpty()) {
            observations.add("El avance cumple con los estándares de consistencia requeridos.");
        }

        return new AiAuditResult(score, observations, isValid);
    }

    public static class AiAuditResult {
        private int score;
        private List<String> observations;
        private boolean isValid;

        public AiAuditResult() {}

        public AiAuditResult(int score, List<String> observations, boolean isValid) {
            this.score = score;
            this.observations = observations;
            this.isValid = isValid;
        }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        
        public List<String> getObservations() { return observations; }
        public void setObservations(List<String> observations) { this.observations = observations; }
        
        public boolean getIsValid() { return isValid; }
        public void setIsValid(boolean valid) { isValid = valid; }
    }
}
