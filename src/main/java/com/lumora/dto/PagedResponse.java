package com.lumora.dto;

import org.springframework.data.domain.Page;
import java.util.List;

// ─────────────────────────────────────────────────────────────────────────────
// DTO/PAGEDRESPONSE.JAVA
//
// ATENÇÃO: Este arquivo deve estar em:
//   src/main/java/com/com.com.com.lumora.lumora.com.com.lumora.lumora/dto/PagedResponse.java
//
// Se continuar com erro após adicionar este arquivo:
//   1. Clique com botão direito na pasta dto no IntelliJ
//   2. Selecione "Reload from Disk"
//   3. Depois: Build → Rebuild Project
// ─────────────────────────────────────────────────────────────────────────────

public record PagedResponse<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements,
        int     totalPages,
        boolean first,
        boolean last
) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
