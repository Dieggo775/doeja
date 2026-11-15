package com.doeja.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Categorias fixas (alimentos, roupas...). Criadas pela migration V2. */
@Entity
@Table(name = "categorias_doacao")
public class CategoriaDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String slug;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false)
    private Integer ordem = 0;

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getNome() { return nome; }
    public Integer getOrdem() { return ordem; }
}
