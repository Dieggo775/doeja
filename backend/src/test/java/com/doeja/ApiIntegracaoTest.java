package com.doeja;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testa a API de ponta a ponta com um PostgreSQL de verdade (o banco de testes do
 * docker-compose, perfil "test"). Cada teste cria seus próprios usuários com e-mails
 * únicos, então a ordem de execução não importa.
 */
@SpringBootTest(properties = {
        "spring.flyway.locations=classpath:db/migration,classpath:db/seed",
        "doeja.jwt.secret=segredo-so-para-testes-com-mais-de-32-caracteres",
        "doeja.admin.email=admin.teste@doeja.example",
        "doeja.admin.senha=SenhaAdmin123",
        "doeja.demo.enabled=true",
        "doeja.demo.senha=SenhaDemo123"
})
@AutoConfigureMockMvc
class ApiIntegracaoTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper json;

    // ------------------------------------------------------------------ consulta pública

    @Test
    void listaPublicaMostraSoCentrosAprovadosComNecessidades() throws Exception {
        mvc.perform(get("/api/centros").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome", hasItem("Associação Comunitária Mãos Unidas")))
                .andExpect(jsonPath("$.content[*].nome", not(hasItem("Coletivo Mulheres em Rede"))))
                .andExpect(jsonPath("$.content[*].status", everyItem(org.hamcrest.Matchers.is("APROVADO"))))
                .andExpect(jsonPath("$.content[?(@.nome == 'Associação Comunitária Mãos Unidas')].necessidades[*].categoria",
                        hasItem("alimentos")));
    }

    @Test
    void filtraCentrosPelaCategoriaQueEstaoPrecisando() throws Exception {
        mvc.perform(get("/api/centros").param("categoria", "moveis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome", hasItem("Instituto Recomeçar Taboão")))
                .andExpect(jsonPath("$.content[*].nome", not(hasItem("Projeto Criança Sorrindo"))));
    }

    @Test
    void buscaPorProximidadeOrdenaPelaDistancia() throws Exception {
        String corpo = mvc.perform(get("/api/centros/proximos")
                        .param("lat", "-23.61085").param("lng", "-46.76793").param("raioKm", "3"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode lista = json.readTree(corpo);
        org.junit.jupiter.api.Assertions.assertEquals("Associação Comunitária Mãos Unidas", lista.get(0).get("nome").asText());
        for (int i = 1; i < lista.size(); i++) {
            org.junit.jupiter.api.Assertions.assertTrue(
                    lista.get(i).get("distanciaKm").asDouble() >= lista.get(i - 1).get("distanciaKm").asDouble());
        }
    }

    @Test
    void categoriasEInfoSaoPublicas() throws Exception {
        mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].slug", hasItem("alimentos")));
        mvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.demonstracao").value(true));
    }

    // ------------------------------------------------------------------ autenticação

    @Test
    void cadastroValidaDadosEImpedeEmailRepetido() throws Exception {
        String email = emailUnico("doador");
        cadastrar("Ana Doadora", email, "USUARIO").andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.usuario.papel").value("USUARIO"));

        cadastrar("Outra Ana", email.toUpperCase(), "USUARIO").andExpect(status().isConflict());

        // Ninguém vira administrador pelo cadastro público
        cadastrar("Esperto", emailUnico("admin"), "ADMIN").andExpect(status().isBadRequest());

        // Sem aceitar o termo LGPD
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of(
                        "nome", "Sem Termo", "email", emailUnico("lgpd"), "senha", "SenhaForte123",
                        "papel", "USUARIO", "aceiteLgpd", false))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.aceiteLgpd").exists());
    }

    @Test
    void loginErradoNaoRevelaSeOEmailExisteEBloqueiaAposCincoTentativas() throws Exception {
        String email = emailUnico("bloqueio");
        cadastrar("Beto", email, "USUARIO").andExpect(status().isCreated());

        for (int i = 0; i < 5; i++) {
            login(email, "senha-errada").andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos."));
        }
        // Mesmo com a senha certa, agora está bloqueado
        login(email, "SenhaForte123").andExpect(status().isTooManyRequests());

        login(emailUnico("nao-existe"), "qualquer").andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos."));
    }

    @Test
    void rotasProtegidasExigemLoginEPerfilCerto() throws Exception {
        mvc.perform(get("/api/meus-centros")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/doacoes/minhas")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/meus-centros").header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"))
                .andExpect(status().isUnauthorized());

        String doador = tokenNovo("USUARIO");
        mvc.perform(comToken(get("/api/meus-centros"), doador)).andExpect(status().isForbidden());
        mvc.perform(comToken(get("/api/admin/centros"), doador)).andExpect(status().isForbidden());

        String centro = tokenNovo("CENTRO");
        mvc.perform(comToken(post("/api/doacoes"), centro).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------ fluxo completo

    @Test
    void fluxoCompletoCadastroAprovacaoDoacaoEConfirmacao() throws Exception {
        String admin = tokenDe("admin.teste@doeja.example", "SenhaAdmin123");
        String responsavel = tokenNovo("CENTRO");
        String doador = tokenNovo("USUARIO");
        String intruso = tokenNovo("USUARIO");

        // 1) Responsável cadastra um centro: fica PENDENTE e invisível ao público
        String nomeCentro = "Centro de Teste " + UUID.randomUUID();
        long centroId = idDe(mvc.perform(comToken(post("/api/meus-centros"), responsavel)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(novoCentro(nomeCentro))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE")));

        mvc.perform(get("/api/centros/" + centroId)).andExpect(status().isNotFound());

        // Doação para centro não aprovado: "não existe"
        registrarDoacao(doador, centroId, 1).andExpect(status().isNotFound());

        // 2) Admin vê na fila e aprova
        mvc.perform(comToken(get("/api/admin/centros").param("status", "PENDENTE"), admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem((int) centroId)));
        mvc.perform(comToken(patch("/api/admin/centros/" + centroId + "/status"), admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APROVADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
        mvc.perform(get("/api/centros/" + centroId)).andExpect(status().isOk());

        // 3) Responsável informa o que precisa
        mvc.perform(comToken(put("/api/meus-centros/" + centroId + "/necessidades"), responsavel)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(List.of(
                                Map.of("categoria", "alimentos", "tipo", "PRECISA", "descricao", "Arroz e feijão", "urgente", true),
                                Map.of("categoria", "roupas", "tipo", "ACEITA")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.necessidades.length()").value(2))
                .andExpect(jsonPath("$.necessidades[0].urgente").value(true));

        // Outro responsável não consegue mexer neste centro
        String outroResponsavel = tokenNovo("CENTRO");
        mvc.perform(comToken(put("/api/meus-centros/" + centroId + "/necessidades"), outroResponsavel)
                        .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isNotFound());

        // 4) Doador registra a doação
        long doacaoId = idDe(registrarDoacao(doador, centroId, 5)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PROMETIDA"))
                .andExpect(jsonPath("$.doadorNome").value(nullValue())));

        // Terceiros não enxergam nem alteram
        alterarStatus(intruso, doacaoId, "CANCELADA").andExpect(status().isNotFound());

        // 5) Doador avisa que entregou; ele não pode confirmar o próprio recebimento
        alterarStatus(doador, doacaoId, "ENTREGUE").andExpect(status().isOk());
        alterarStatus(doador, doacaoId, "CONFIRMADA").andExpect(status().isConflict());

        // 6) Centro vê a doação recebida (com o nome do doador) e confirma
        mvc.perform(comToken(get("/api/doacoes/recebidas"), responsavel))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(doacaoId))
                .andExpect(jsonPath("$[0].doadorNome").isNotEmpty());
        alterarStatus(responsavel, doacaoId, "CONFIRMADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        // Confirmada é estado final
        alterarStatus(responsavel, doacaoId, "CANCELADA").andExpect(status().isConflict());

        // 7) Aparece no histórico do doador e no painel de impacto
        mvc.perform(comToken(get("/api/doacoes/minhas"), doador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("CONFIRMADA"))
                .andExpect(jsonPath("$[0].itens[0].quantidade").value(5));
        mvc.perform(get("/api/estatisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doacoesPorStatus.CONFIRMADA").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.itensConfirmados").value(org.hamcrest.Matchers.greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.tempoMedioConfirmacaoHoras").isNumber());
    }

    @Test
    void cadastroDeCentroComDadosInvalidosExplicaOsErros() throws Exception {
        String responsavel = tokenNovo("CENTRO");
        Map<String, Object> invalido = new java.util.HashMap<>(novoCentro("X"));
        invalido.put("estado", "São Paulo");
        invalido.put("latitude", 120.0);
        invalido.remove("telefone");

        mvc.perform(comToken(post("/api/meus-centros"), responsavel)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.estado").exists())
                .andExpect(jsonPath("$.campos.latitude").exists())
                .andExpect(jsonPath("$.campos.telefone").exists());
    }

    @Test
    void contaDeDemonstracaoAdministraOCentroFicticio() throws Exception {
        String token = tokenDe("maos.unidas@doeja.example", "SenhaDemo123");
        mvc.perform(comToken(get("/api/meus-centros"), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Associação Comunitária Mãos Unidas"));
    }

    // ------------------------------------------------------------------ apoio

    private static String emailUnico(String prefixo) {
        return prefixo + "." + UUID.randomUUID().toString().substring(0, 8) + "@teste.example";
    }

    private ResultActions cadastrar(String nome, String email, String papel) throws Exception {
        return mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "nome", nome, "email", email, "senha", "SenhaForte123", "papel", papel, "aceiteLgpd", true))));
    }

    private ResultActions login(String email, String senha) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "senha", senha))));
    }

    private String tokenNovo(String papel) throws Exception {
        String corpo = cadastrar("Pessoa " + papel, emailUnico(papel.toLowerCase()), papel)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("token").asText();
    }

    private String tokenDe(String email, String senha) throws Exception {
        String corpo = login(email, senha).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("token").asText();
    }

    private static MockHttpServletRequestBuilder comToken(MockHttpServletRequestBuilder req, String token) {
        return req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    private long idDe(ResultActions resultado) throws Exception {
        return json.readTree(resultado.andReturn().getResponse().getContentAsString()).get("id").asLong();
    }

    private ResultActions registrarDoacao(String token, long centroId, int quantidade) throws Exception {
        return mvc.perform(comToken(post("/api/doacoes"), token).contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "centroId", centroId,
                        "observacao", "Levo no sábado de manhã",
                        "itens", List.of(Map.of("categoria", "alimentos", "descricao", "Pacote de arroz 5 kg",
                                "quantidade", quantidade))))));
    }

    private ResultActions alterarStatus(String token, long doacaoId, String novo) throws Exception {
        return mvc.perform(comToken(patch("/api/doacoes/" + doacaoId + "/status"), token)
                .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + novo + "\"}"));
    }

    private static Map<String, Object> novoCentro(String nome) {
        return Map.ofEntries(
                Map.entry("nome", nome),
                Map.entry("descricao", "Centro criado pelo teste automatizado"),
                Map.entry("endereco", "Rua de Teste, 10"),
                Map.entry("bairro", "Centro"),
                Map.entry("cidade", "Taboão da Serra"),
                Map.entry("estado", "SP"),
                Map.entry("cep", "06750000"),
                Map.entry("telefone", "(11) 90000-0000"),
                Map.entry("whatsapp", ""),
                Map.entry("horarioFuncionamento", "Seg a Sex, 9h às 17h"),
                Map.entry("latitude", -23.62),
                Map.entry("longitude", -46.78));
    }
}
