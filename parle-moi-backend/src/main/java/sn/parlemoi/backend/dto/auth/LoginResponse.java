package sn.parlemoi.backend.dto.auth;

public record LoginResponse(
        String token,
        String role,
        String nom
) {}