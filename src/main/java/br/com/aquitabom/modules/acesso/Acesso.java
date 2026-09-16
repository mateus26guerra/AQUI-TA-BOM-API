package br.com.aquitabom.modules.acesso;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "acesso")
public class Acesso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Column(length = 160)
    private String descricao;

    protected Acesso() {
    }
    // CONSTRUTOR PÚBLICO PARA O DATASEEDER (sem ID)
    public Acesso(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
