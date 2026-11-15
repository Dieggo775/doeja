package com.doeja.service;

import java.time.OffsetDateTime;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doeja.dto.AuthDTOs.CadastroRequest;
import com.doeja.dto.AuthDTOs.LoginRequest;
import com.doeja.dto.AuthDTOs.TokenResponse;
import com.doeja.dto.AuthDTOs.UsuarioDTO;
import com.doeja.entity.Papel;
import com.doeja.entity.Usuario;
import com.doeja.exceptions.ConflitoException;
import com.doeja.exceptions.NaoAutorizadoException;
import com.doeja.exceptions.RequisicaoInvalidaException;
import com.doeja.repository.UsuarioRepository;
import com.doeja.security.JwtService;
import com.doeja.security.LimitadorDeLogin;

@Service
public class AuthService {

    /** Mensagem única para e-mail inexistente OU senha errada: não revela quais e-mails existem. */
    static final String CREDENCIAIS_INVALIDAS = "E-mail ou senha incorretos.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LimitadorDeLogin limitador;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder passwordEncoder,
                       JwtService jwtService, LimitadorDeLogin limitador) {
        this.usuarios = usuarios;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limitador = limitador;
    }

    @Transactional
    public TokenResponse cadastrar(CadastroRequest req) {
        if (req.papel() == Papel.ADMIN) {
            throw new RequisicaoInvalidaException("Não é possível criar conta de administrador pelo cadastro.");
        }
        String email = normalizarEmail(req.email());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Já existe uma conta com este e-mail.");
        }

        Usuario usuario = new Usuario(req.nome().trim(), email, passwordEncoder.encode(req.senha()), req.papel());
        usuario.setLgpdConsentimentoEm(OffsetDateTime.now());
        usuarios.save(usuario);

        return emitirToken(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        String email = normalizarEmail(req.email());
        if (limitador.estaBloqueado(email)) {
            throw new NaoAutorizadoException("Muitas tentativas. Aguarde 15 minutos e tente de novo.", true);
        }

        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenhaHash()))
                .orElse(null);

        if (usuario == null) {
            limitador.registrarFalha(email);
            throw new NaoAutorizadoException(CREDENCIAIS_INVALIDAS);
        }

        limitador.registrarSucesso(email);
        return emitirToken(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioDTO buscar(Long id) {
        return usuarios.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .map(AuthService::toDTO)
                .orElseThrow(() -> new NaoAutorizadoException("Sessão inválida. Faça login novamente."));
    }

    private TokenResponse emitirToken(Usuario usuario) {
        JwtService.Token token = jwtService.gerar(usuario);
        return new TokenResponse(token.valor(), token.expiraEm(), toDTO(usuario));
    }

    static UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(u.getId(), u.getNome(), u.getEmail(), u.getPapel());
    }

    static String normalizarEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
