package com.enlace.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoupleStory {
    private String partner1Name;   // max 100 chars, nullable
    private String partner2Name;   // max 100 chars, nullable
    private String message;        // max 400 chars, nullable

    /** Retorna true se ao menos um campo está preenchido */
    public boolean hasContent() {
        return (partner1Name != null && !partner1Name.isBlank())
            || (partner2Name != null && !partner2Name.isBlank())
            || (message != null && !message.isBlank());
    }
}
