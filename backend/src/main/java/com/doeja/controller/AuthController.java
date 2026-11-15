package com.doeja.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.doeja.dto.AuthDTOs.CadastroRequest;
import com.doeja.dto.AuthDTOs.LoginRequest;
import com.doeja.dto.AuthDTOs.TokenResponse;
import com.doeja.dto.AuthDTOs.UsuarioDTO;
import com.doeja.security.UsuarioLogado;
import com.doeja.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        return service.cadastrar(req);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @GetMapping("/eu")
    public UsuarioDTO eu(@AuthenticationPrincipal Jwt jwt) {
        return service.buscar(UsuarioLogado.id(jwt));
    }
}
