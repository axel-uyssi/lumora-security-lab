package com.lumora.exception;

// ─────────────────────────────────────────────────────────────────────────────
// EXCEPTION/RESOURCENOTFOUNDEXCEPTION.JAVA — Recurso não encontrado (404)
//
// Usada quando buscamos algo pelo ID e não existe no banco.
// O GlobalExceptionHandler captura e retorna HTTP 404 automaticamente.
//
// Exemplo de uso no Service:
//   Hotel hotel = hotelRepository.findById(id)
//       .orElseThrow(() -> new ResourceNotFoundException("Hotel", "id", id));
//
// Mensagem gerada:
//   "Hotel não encontrado com id: 550e8400-e29b-41d4-a716-446655440000"
// ─────────────────────────────────────────────────────────────────────────────

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(resource + " não encontrado com " + field + ": " + value);
    }
}