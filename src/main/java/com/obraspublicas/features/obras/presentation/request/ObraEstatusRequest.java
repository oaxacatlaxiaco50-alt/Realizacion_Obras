package com.obraspublicas.features.obras.presentation.request;

import com.obraspublicas.features.obras.domain.model.ObraEstatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObraEstatusRequest {

    @NotNull(message = "El estatus es obligatorio")
    private ObraEstatus estatus;
}
