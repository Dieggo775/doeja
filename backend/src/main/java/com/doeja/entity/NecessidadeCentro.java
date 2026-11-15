package com.doeja.entity;

import java.time.OffsetDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** O que um centro está precisando (ou aceitando) em cada categoria. */
@Entity
@Table(name = "necessidades_centro")
public class NecessidadeCentro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private CentroDoacao centro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaDoacao categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoNecessidade tipo = TipoNecessidade.PRECISA;

    @Column(length = 300)
    private String descricao;

    @Column(nullable = false)
    private Boolean urgente = false;

    @Column(nullable = false)
    private Boolean ativa = true;

    @UpdateTimestamp
    @Column(nullable = false)
    private OffsetDateTime atualizadaEm;

    public NecessidadeCentro() {
    }

    public NecessidadeCentro(CentroDoacao centro, CategoriaDoacao categoria, TipoNecessidade tipo,
                             String descricao, boolean urgente) {
        this.centro = centro;
        this.categoria = categoria;
        this.tipo = tipo;
        this.descricao = descricao;
        this.urgente = urgente;
    }

    public Long getId() { return id; }
    public CentroDoacao getCentro() { return centro; }
    public CategoriaDoacao getCategoria() { return categoria; }
    public TipoNecessidade getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public Boolean getUrgente() { return urgente; }
    public Boolean getAtiva() { return ativa; }
    public OffsetDateTime getAtualizadaEm() { return atualizadaEm; }
}
