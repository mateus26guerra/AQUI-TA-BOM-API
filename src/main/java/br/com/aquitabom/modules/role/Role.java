package br.com.aquitabom.modules.role;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.aquitabom.modules.acesso.Acesso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(length = 160)
    private String descricao;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_acesso",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "acesso_id"))
    private Set<Acesso> acessos = new HashSet<>();

    protected Role() {
    }

    // CONSTRUTOR PARA O DATASEEDER (Não precisa passar o ID!)
    public Role(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    // Construtor completo (se precisar)
    public Role(UUID id, String nome, String descricao, Set<Acesso> acessos) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.acessos = acessos != null ? acessos : new HashSet<>();
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

    public Set<Acesso> getAcessos() {
        return acessos;
    }
}