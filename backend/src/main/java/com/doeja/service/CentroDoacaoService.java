package com.doeja.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doeja.dto.CategoriaDTO;
import com.doeja.dto.CentroDoacaoMapper;
import com.doeja.dto.CentroDoacaoRequestDTO;
import com.doeja.dto.CentroDoacaoResponseDTO;
import com.doeja.dto.NecessidadeDTO;
import com.doeja.entity.CategoriaDoacao;
import com.doeja.entity.CentroDoacao;
import com.doeja.entity.NecessidadeCentro;
import com.doeja.entity.StatusCentro;
import com.doeja.entity.Usuario;
import com.doeja.exceptions.RequisicaoInvalidaException;
import com.doeja.exceptions.ResourceNotFoundException;
import com.doeja.repository.CategoriaDoacaoRepository;
import com.doeja.repository.CentroDoacaoRepository;
import com.doeja.repository.CentroDoacaoSpecs;
import com.doeja.repository.NecessidadeCentroRepository;
import com.doeja.repository.UsuarioRepository;
import com.doeja.util.GeoUtils;

@Service
@Transactional(readOnly = true)
public class CentroDoacaoService {

    public static final double RAIO_MAXIMO_KM = 50.0;
    public static final int LIMITE_MAXIMO = 50;
    public static final int MAX_NECESSIDADES = 20;

    /** Campos pelos quais o público pode ordenar a listagem. */
    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("nome", "cidade", "bairro");

    private final CentroDoacaoRepository repository;
    private final NecessidadeCentroRepository necessidades;
    private final CategoriaDoacaoRepository categorias;
    private final UsuarioRepository usuarios;

    public CentroDoacaoService(CentroDoacaoRepository repository, NecessidadeCentroRepository necessidades,
                               CategoriaDoacaoRepository categorias, UsuarioRepository usuarios) {
        this.repository = repository;
        this.necessidades = necessidades;
        this.categorias = categorias;
        this.usuarios = usuarios;
    }

    // ------------------------------------------------------------------ público

    /** Lista centros APROVADOS, combinando todos os filtros informados. */
    public Page<CentroDoacaoResponseDTO> listar(String bairro, String cidade, String cep,
                                                Boolean ativo, String categoria, Pageable pageable) {
        Specification<CentroDoacao> filtro = CentroDoacaoSpecs.aprovado();

        if (temTexto(bairro)) {
            filtro = filtro.and(CentroDoacaoSpecs.campoContem("bairro", bairro));
        }
        if (temTexto(cidade)) {
            filtro = filtro.and(CentroDoacaoSpecs.campoContem("cidade", cidade));
        }
        if (temTexto(cep)) {
            filtro = filtro.and(CentroDoacaoSpecs.campoContem("cep", cep));
        }
        if (ativo != null) {
            filtro = filtro.and(CentroDoacaoSpecs.ativo(ativo));
        }
        if (temTexto(categoria)) {
            filtro = filtro.and(CentroDoacaoSpecs.temNecessidade(categoria.trim()));
        }

        Page<CentroDoacao> pagina = repository.findAll(filtro, normalizar(pageable));
        Map<Long, List<NecessidadeDTO>> porCentro = necessidadesDe(pagina.getContent());
        return pagina.map(c -> CentroDoacaoMapper.toResponseDTO(c, porCentro.get(c.getId())));
    }

    /** Busca um centro APROVADO; para o público, centro pendente ou rejeitado "não existe". */
    public CentroDoacaoResponseDTO buscarPorId(Long id) {
        CentroDoacao centro = repository.findById(id)
                .filter(c -> c.getStatus() == StatusCentro.APROVADO)
                .orElseThrow(() -> new ResourceNotFoundException("Centro não encontrado: " + id));
        return comNecessidades(centro);
    }

    /** Centros ativos e aprovados a até raioKm do ponto, do mais perto para o mais longe. */
    public List<CentroDoacaoResponseDTO> listarProximos(double lat, double lng, double raioKm, int limite) {
        if (!Double.isFinite(lat) || lat < -90 || lat > 90) {
            throw new RequisicaoInvalidaException("lat deve estar entre -90 e 90.");
        }
        if (!Double.isFinite(lng) || lng < -180 || lng > 180) {
            throw new RequisicaoInvalidaException("lng deve estar entre -180 e 180.");
        }
        if (!Double.isFinite(raioKm) || raioKm <= 0 || raioKm > RAIO_MAXIMO_KM) {
            throw new RequisicaoInvalidaException("raioKm deve ser maior que 0 e no máximo " + (int) RAIO_MAXIMO_KM + ".");
        }
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new RequisicaoInvalidaException("limite deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }

        GeoUtils.Limites caixa = GeoUtils.caixa(lat, lng, raioKm);

        List<CentroComDistancia> perto = repository
                .findByStatusAndAtivoTrueAndLatitudeBetweenAndLongitudeBetween(
                        StatusCentro.APROVADO, caixa.minLat(), caixa.maxLat(), caixa.minLng(), caixa.maxLng())
                .stream()
                .map(centro -> new CentroComDistancia(centro,
                        GeoUtils.distanciaKm(lat, lng, centro.getLatitude(), centro.getLongitude())))
                .filter(item -> item.distanciaKm() <= raioKm)
                .sorted(Comparator.comparingDouble(CentroComDistancia::distanciaKm))
                .limit(limite)
                .toList();

        Map<Long, List<NecessidadeDTO>> porCentro = necessidadesDe(perto.stream().map(CentroComDistancia::centro).toList());
        return perto.stream()
                .map(item -> CentroDoacaoMapper.toResponseDTO(item.centro(), arredondar(item.distanciaKm()),
                        porCentro.get(item.centro().getId())))
                .toList();
    }

    public List<CategoriaDTO> listarCategorias() {
        return categorias.findAllByOrderByOrdemAscNomeAsc().stream()
                .map(c -> new CategoriaDTO(c.getSlug(), c.getNome()))
                .toList();
    }

    // ------------------------------------------------------------------ responsável (perfil CENTRO)

    public List<CentroDoacaoResponseDTO> listarDoResponsavel(Long usuarioId) {
        List<CentroDoacao> centros = repository.findByResponsavelIdOrderByNomeAsc(usuarioId);
        Map<Long, List<NecessidadeDTO>> porCentro = necessidadesDe(centros);
        return centros.stream()
                .map(c -> CentroDoacaoMapper.toResponseDTO(c, porCentro.get(c.getId())))
                .toList();
    }

    /** Novo centro entra como PENDENTE e só aparece ao público depois que o admin aprovar. */
    @Transactional
    public CentroDoacaoResponseDTO cadastrar(Long usuarioId, CentroDoacaoRequestDTO req) {
        Usuario responsavel = usuarios.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        CentroDoacao centro = new CentroDoacao();
        CentroDoacaoMapper.copiar(req, centro);
        centro.setStatus(StatusCentro.PENDENTE);
        centro.setAtivo(true);
        centro.setResponsavel(responsavel);
        repository.save(centro);

        return CentroDoacaoMapper.toResponseDTO(centro, List.of());
    }

    /** Edita os dados. Se o cadastro tinha sido REJEITADO, volta para análise (PENDENTE). */
    @Transactional
    public CentroDoacaoResponseDTO atualizar(Long usuarioId, Long centroId, CentroDoacaoRequestDTO req) {
        CentroDoacao centro = doResponsavel(usuarioId, centroId);
        CentroDoacaoMapper.copiar(req, centro);
        if (centro.getStatus() == StatusCentro.REJEITADO) {
            centro.setStatus(StatusCentro.PENDENTE);
        }
        return comNecessidades(centro);
    }

    @Transactional
    public CentroDoacaoResponseDTO alterarAtivo(Long usuarioId, Long centroId, boolean ativo) {
        CentroDoacao centro = doResponsavel(usuarioId, centroId);
        centro.setAtivo(ativo);
        return comNecessidades(centro);
    }

    /** Substitui toda a lista "o que precisamos / aceitamos" do centro. */
    @Transactional
    public CentroDoacaoResponseDTO substituirNecessidades(Long usuarioId, Long centroId, List<NecessidadeDTO> lista) {
        CentroDoacao centro = doResponsavel(usuarioId, centroId);

        if (lista == null) {
            throw new RequisicaoInvalidaException("Envie a lista de necessidades (pode ser vazia).");
        }
        for (NecessidadeDTO n : lista) {
            if (n == null || n.categoria() == null || n.categoria().isBlank()) {
                throw new RequisicaoInvalidaException("Informe a categoria de cada necessidade.");
            }
            if (n.tipo() == null) {
                throw new RequisicaoInvalidaException("Informe o tipo (PRECISA ou ACEITA) de cada necessidade.");
            }
            if (n.descricao() != null && n.descricao().length() > 300) {
                throw new RequisicaoInvalidaException("A descrição da necessidade pode ter até 300 caracteres.");
            }
        }
        if (lista.size() > MAX_NECESSIDADES) {
            throw new RequisicaoInvalidaException("No máximo " + MAX_NECESSIDADES + " necessidades por centro.");
        }
        Set<String> slugs = lista.stream().map(NecessidadeDTO::categoria).collect(Collectors.toSet());
        if (slugs.size() != lista.size()) {
            throw new RequisicaoInvalidaException("Cada categoria pode aparecer só uma vez.");
        }

        necessidades.apagarDoCentro(centro.getId());
        CentroDoacao gerenciado = repository.getReferenceById(centro.getId());

        for (NecessidadeDTO n : lista) {
            CategoriaDoacao categoria = categorias.findBySlug(n.categoria())
                    .orElseThrow(() -> new RequisicaoInvalidaException("Categoria inexistente: " + n.categoria()));
            String descricao = n.descricao() == null || n.descricao().isBlank() ? null : n.descricao().trim();
            necessidades.save(new NecessidadeCentro(gerenciado, categoria, n.tipo(), descricao,
                    Boolean.TRUE.equals(n.urgente())));
        }
        necessidades.flush();

        return comNecessidades(repository.findById(centroId).orElseThrow());
    }

    // ------------------------------------------------------------------ admin

    public List<CentroDoacaoResponseDTO> listarParaAdmin(StatusCentro status) {
        List<CentroDoacao> centros = status == null
                ? repository.findAllByOrderByCriadoEmDesc()
                : repository.findByStatusOrderByCriadoEmAsc(status);
        Map<Long, List<NecessidadeDTO>> porCentro = necessidadesDe(centros);
        return centros.stream()
                .map(c -> CentroDoacaoMapper.toResponseDTO(c, porCentro.get(c.getId())))
                .toList();
    }

    @Transactional
    public CentroDoacaoResponseDTO moderar(Long centroId, StatusCentro novoStatus) {
        if (novoStatus == null) {
            throw new RequisicaoInvalidaException("Informe o status (APROVADO, REJEITADO ou PENDENTE).");
        }
        CentroDoacao centro = repository.findById(centroId)
                .orElseThrow(() -> new ResourceNotFoundException("Centro não encontrado: " + centroId));
        centro.setStatus(novoStatus);
        return comNecessidades(centro);
    }

    // ------------------------------------------------------------------ apoio

    /** Busca um centro garantindo que pertence ao usuário. Se não pertencer, responde 404 (não revela que existe). */
    private CentroDoacao doResponsavel(Long usuarioId, Long centroId) {
        return repository.findById(centroId)
                .filter(c -> c.getResponsavel() != null && c.getResponsavel().getId().equals(usuarioId))
                .orElseThrow(() -> new ResourceNotFoundException("Centro não encontrado: " + centroId));
    }

    private CentroDoacaoResponseDTO comNecessidades(CentroDoacao centro) {
        return CentroDoacaoMapper.toResponseDTO(centro, necessidadesDe(List.of(centro)).get(centro.getId()));
    }

    private Map<Long, List<NecessidadeDTO>> necessidadesDe(List<CentroDoacao> centros) {
        if (centros.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = centros.stream().map(CentroDoacao::getId).toList();
        return necessidades.buscarAtivasDosCentros(ids).stream()
                .collect(Collectors.groupingBy(n -> n.getCentro().getId(),
                        Collectors.mapping(CentroDoacaoMapper::toNecessidadeDTO, Collectors.toList())));
    }

    private record CentroComDistancia(CentroDoacao centro, double distanciaKm) {
    }

    private static boolean temTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private static double arredondar(double km) {
        return Math.round(km * 100.0) / 100.0;
    }

    /** Aceita só campos de ordenação permitidos e sempre desempata por id (páginas estáveis). */
    private static Pageable normalizar(Pageable pageable) {
        List<Sort.Order> ordens = pageable.getSort().stream()
                .filter(ordem -> CAMPOS_ORDENAVEIS.contains(ordem.getProperty()))
                .toList();
        Sort sort = ordens.isEmpty() ? Sort.by("nome") : Sort.by(ordens);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort.and(Sort.by("id")));
    }
}
