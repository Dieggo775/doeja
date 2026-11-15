package com.doeja.config;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.doeja.entity.Papel;
import com.doeja.entity.Usuario;
import com.doeja.repository.CentroDoacaoRepository;
import com.doeja.repository.UsuarioRepository;

/**
 * Cria contas ao subir a aplicação (idempotente: não duplica nem altera contas existentes).
 *
 * 1) Administrador: se ADMIN_EMAIL e ADMIN_SENHA estiverem definidos.
 * 2) Demonstração: se DEMO_ENABLED=true e DEMO_SENHA definido, cria um doador e um
 *    responsável para cada centro fictício do seed (db/seed) e liga o responsável ao centro.
 *    Os e-mails usam o domínio reservado ".example", que não existe na internet.
 */
@Component
public class ContasIniciais implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContasIniciais.class);

    /** e-mail do responsável -> nome do centro fictício no seed. */
    record ContaCentro(String email, String nome, String centro) {
    }

    static final List<ContaCentro> CONTAS_CENTROS = List.of(
            new ContaCentro("maos.unidas@doeja.example", "Responsável - Mãos Unidas", "Associação Comunitária Mãos Unidas"),
            new ContaCentro("semear@doeja.example", "Responsável - Semear", "Comunidade Cristã Semear"),
            new ContaCentro("ninho.de.luz@doeja.example", "Responsável - Ninho de Luz", "Casa de Acolhimento Ninho de Luz"),
            new ContaCentro("recomecar@doeja.example", "Responsável - Recomeçar", "Instituto Recomeçar Taboão"),
            new ContaCentro("vizinho.solidario@doeja.example", "Responsável - Vizinho Solidário", "Banco de Alimentos Vizinho Solidário"),
            new ContaCentro("crianca.sorrindo@doeja.example", "Responsável - Criança Sorrindo", "Projeto Criança Sorrindo"),
            new ContaCentro("lar.acolher@doeja.example", "Responsável - Lar Acolher", "Lar Acolher - Residência para Idosos"),
            new ContaCentro("mulheres.em.rede@doeja.example", "Responsável - Mulheres em Rede", "Coletivo Mulheres em Rede"));

    static final String EMAIL_DOADOR_DEMO = "doador@doeja.example";

    private final UsuarioRepository usuarios;
    private final CentroDoacaoRepository centros;
    private final PasswordEncoder passwordEncoder;

    private final String adminEmail;
    private final String adminSenha;
    private final String adminNome;
    private final boolean demoAtivo;
    private final String demoSenha;

    public ContasIniciais(UsuarioRepository usuarios, CentroDoacaoRepository centros, PasswordEncoder passwordEncoder,
                          @Value("${doeja.admin.email:}") String adminEmail,
                          @Value("${doeja.admin.senha:}") String adminSenha,
                          @Value("${doeja.admin.nome:Administrador DoeJá}") String adminNome,
                          @Value("${doeja.demo.enabled:false}") boolean demoAtivo,
                          @Value("${doeja.demo.senha:}") String demoSenha) {
        this.usuarios = usuarios;
        this.centros = centros;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
        this.adminNome = adminNome;
        this.demoAtivo = demoAtivo;
        this.demoSenha = demoSenha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarAdmin();
        criarContasDemo();
    }

    private void criarAdmin() {
        if (adminEmail.isBlank() || adminSenha.isBlank()) {
            if (!usuarios.existsByPapel(Papel.ADMIN)) {
                log.warn("Nenhum administrador cadastrado. Defina ADMIN_EMAIL e ADMIN_SENHA para criar um.");
            }
            return;
        }
        if (adminSenha.length() < 8) {
            log.error("ADMIN_SENHA precisa ter pelo menos 8 caracteres. Administrador não criado.");
            return;
        }
        if (criarSeNaoExiste(adminEmail, adminNome, adminSenha, Papel.ADMIN) != null) {
            log.info("Administrador criado: {}", adminEmail);
        }
    }

    private void criarContasDemo() {
        if (!demoAtivo) {
            return;
        }
        if (demoSenha.length() < 8) {
            log.warn("DEMO_ENABLED=true, mas DEMO_SENHA não tem 8+ caracteres: contas de demonstração não criadas.");
            return;
        }

        criarSeNaoExiste(EMAIL_DOADOR_DEMO, "Doador de Demonstração", demoSenha, Papel.USUARIO);

        var todosCentros = centros.findAll();
        for (ContaCentro conta : CONTAS_CENTROS) {
            Usuario responsavel = usuarios.findByEmailIgnoreCase(conta.email())
                    .orElseGet(() -> criarSeNaoExiste(conta.email(), conta.nome(), demoSenha, Papel.CENTRO));

            // Liga o responsável ao centro do seed, se o centro ainda não tiver dono
            todosCentros.stream()
                    .filter(c -> c.getNome().equals(conta.centro()) && c.getResponsavel() == null)
                    .forEach(c -> c.setResponsavel(responsavel));
        }
        log.info("Contas de demonstração conferidas ({} centros + 1 doador).", CONTAS_CENTROS.size());
    }

    /** Cria a conta e devolve; se o e-mail já existe, não mexe e devolve null. */
    private Usuario criarSeNaoExiste(String email, String nome, String senha, Papel papel) {
        String normalizado = email.trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCase(normalizado)) {
            return null;
        }
        Usuario u = new Usuario(nome, normalizado, passwordEncoder.encode(senha), papel);
        u.setLgpdConsentimentoEm(OffsetDateTime.now());
        return usuarios.save(u);
    }
}
