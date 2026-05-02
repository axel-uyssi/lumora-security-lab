package com.lumora.dto;

public record ApiResponse<T>(
        boolean success,
        String  message,
        T       data,
        long    timestamp
) {

    // Retornar dados
    // Ex: ApiResponse.ok(hotelResponse)
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, System.currentTimeMillis());
    }

    // Retornar dados com mensagem customizada
    // Ex: ApiResponse.ok("Hotel criado", hotelResponse)
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, System.currentTimeMillis());
    }

    // Sucesso SEM dados — apenas mensagem
    // Use em: cancel, checkIn, checkOut, delete, patch de status
    // Ex: ApiResponse.okMessage("Reserva cancelada")
    public static <T> ApiResponse<T> okMessage(String message) {
        return new ApiResponse<>(true, message, null, System.currentTimeMillis());
    }

    // Erro — usado no GlobalExceptionHandler
    // Ex: ApiResponse.error("Hotel não encontrado")
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, System.currentTimeMillis());
    }
}

