package br.com.aquitabom.modules.comentario;

import br.com.aquitabom.modules.postagem.Postagem;
import br.com.aquitabom.modules.usuario.Usuario;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "comentario")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(name = "data_criacao", nullable = false)
    private OffsetDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postagem_id", nullable = false)
    private Postagem postagem;

    protected Comentario() {}

    public Comentario(String texto, Usuario usuario, Postagem postagem) {
        this.texto = texto;
        this.usuario = usuario;
        this.postagem = postagem;
        this.dataCriacao = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Postagem getPostagem() {
        return postagem;
    }
}