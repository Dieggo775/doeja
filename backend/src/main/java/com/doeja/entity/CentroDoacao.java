package com.doeja.entity;

import java.lang.annotation.Inherited;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "centros_doacao")
public class CentroDoacao {

    @Id
    @GeneratedValue(strategy = GenerationTyp.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private String endereco;

    private String bairro;

    
}