package com.doeja.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doeja.dto.CentroDoacaoMapper;
import com.doeja.dto.CentroDoacaoResponseDTO;
import com.doeja.entity.CentroDoacao;
import com.doeja.entity.StatusCentro;
import com.doeja.exceptions.RequisicaoInvalidaException;
import com.doeja.exceptions.ResourceNotFoundException;
import com.doeja.repository.CentroDoacaoRepository;
import com.doeja.repository.CentroDoacaoSpecs;
import com.doeja.util.GeoUtils;

@Service
@Transactional(readOnly = true)
public class CentroDoacaoService {

    public static final double RAIO_MAXIMO_KM = 50.0;
    public static final int LIMITE_MAXIMO = 50;

    /** Campos pelos quais o público pode ordenar a listagem. */
    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("nome", "cidade", "bairro");

    private final CentroDoacaoRepository repository;

    public CentroDoacaoService(CentroDoacaoRepository repository) {
        this.repository = repository;
    }

    /** Lista centros APROVADOS, combinando todos os filtros informados. */
    public Page<CentroDoacaoResponseDTO> listar(String bairro, String cidade, String cep,
                                                Boolean ativo, Pageable pageable) {
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

        return repository.findAll(filtro, normalizar(pageable))
                .map(centro -> CentroDoacaoMapper.toResponseDTO(centro));
    }

    /** Busca um centro APROVADO; para o público, centro pendente ou rejeitado "não existe". */
    public CentroDoacaoResponseDTO buscarPorId(Long id) {
        return repository.findById(id)
                .filter(centro -> centro.getStatus() == StatusCentro.APROVADO)
                .map(centro -> CentroDoacaoMapper.toResponseDTO(centro))
                .orElseThrow(() -> new ResourceNotFoundException("Centro não encontrado: " + id));
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

        return repository
                .findByStatusAndAtivoTrueAndLatitudeBetweenAndLongitudeBetween(
                        StatusCentro.APROVADO, caixa.minLat(), caixa.maxLat(), caixa.minLng(), caixa.maxLng())
                .stream()
                .map(centro -> new CentroComDistancia(centro,
                        GeoUtils.distanciaKm(lat, lng, centro.getLatitude(), centro.getLongitude())))
                .filter(item -> item.distanciaKm() <= raioKm)
                .sorted(Comparator.comparingDouble(CentroComDistancia::distanciaKm))
                .limit(limite)
                .map(item -> CentroDoacaoMapper.toResponseDTO(item.centro(), arredondar(item.distanciaKm())))
                .toList();
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
