package com.obraspublicas.features.configuracion.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "institucion_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitucionConfigEntity {

    @Id
    private Long id;

    @Column(name = "nombre_municipio", nullable = false)
    private String nombreMunicipio;

    @Column(name = "lema_oficial")
    private String lemaOficial;

    @Column(name = "periodo_administracion")
    private String periodoAdministracion;

    @Column(name = "dependencia")
    private String dependencia;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "membrete_url")
    private String membreteUrl;

    @Column(name = "pie_pagina_reporte", columnDefinition = "TEXT")
    private String piePaginaReporte;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
