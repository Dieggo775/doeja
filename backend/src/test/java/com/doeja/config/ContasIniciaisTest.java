package com.doeja.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.doeja.entity.Papel;
import com.doeja.entity.Usuario;
import com.doeja.repository.CentroDoacaoRepository;
import com.doeja.repository.UsuarioRepository;

/** A conta de ADMIN_EMAIL deve sempre entrar com a ADMIN_SENHA configurada na hospedagem. */
class ContasIniciaisTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final CentroDoacaoRepository centros = mock(CentroDoacaoRepository.class);

    private ContasIniciais contas(String email, String senha) {
        return new ContasIniciais(usuarios, centros, encoder, email, senha, "Admin", false, "");
    }

    @Test
    void criaOAdminQuandoNaoExiste() {
        when(usuarios.findByEmailIgnoreCase("admin@doeja.org")).thenReturn(Optional.empty());

        contas(" Admin@DoeJa.org ", "SenhaAdmin123\n").run(null);

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).save(salvo.capture());
        assertEquals("admin@doeja.org", salvo.getValue().getEmail());
        assertEquals(Papel.ADMIN, salvo.getValue().getPapel());
        assertTrue(encoder.matches("SenhaAdmin123", salvo.getValue().getSenhaHash()));
    }

    @Test
    void atualizaASenhaQuandoADMIN_SENHAMuda() {
        Usuario admin = new Usuario("Admin", "admin@doeja.org", encoder.encode("SenhaAntiga1"), Papel.ADMIN);
        when(usuarios.findByEmailIgnoreCase("admin@doeja.org")).thenReturn(Optional.of(admin));

        contas("admin@doeja.org", "SenhaNova123").run(null);

        assertTrue(encoder.matches("SenhaNova123", admin.getSenhaHash()));
    }

    @Test
    void promoveAContaQueJaExistiaComoDoador() {
        Usuario doador = new Usuario("Diego", "admin@doeja.org", encoder.encode("outra-senha"), Papel.USUARIO);
        doador.setAtivo(false);
        when(usuarios.findByEmailIgnoreCase("admin@doeja.org")).thenReturn(Optional.of(doador));

        contas("admin@doeja.org", "SenhaAdmin123").run(null);

        assertEquals(Papel.ADMIN, doador.getPapel());
        assertTrue(doador.getAtivo());
        assertTrue(encoder.matches("SenhaAdmin123", doador.getSenhaHash()));
        verify(usuarios, org.mockito.Mockito.never()).save(any());
    }
}
