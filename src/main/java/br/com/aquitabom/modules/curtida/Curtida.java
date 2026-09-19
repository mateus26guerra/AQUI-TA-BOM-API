package br.com.aquitabom.modules.curtida;

import br.com.aquitabom.modules.postagem.Postagem;
import br.com.aquitabom.modules.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "curtida",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_curtida_postagem_usuario",
                columnNames = {"postagem_id", "usuario_id"})
)
public class Curtida {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postagem_id", nullable = false)
    private Postagem postagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    protected Curtida() {}

    public Curtida(Postagem postagem, Usuario usuario) {
        this.postagem = postagem;
        this.usuario = usuario;
    }

    public UUID getId() { return id; }
    public Postagem getPostagem() { return postagem; }
    public Usuario getUsuario() { return usuario; }
    public OffsetDateTime getDataCriacao() { return dataCriacao; }
}
