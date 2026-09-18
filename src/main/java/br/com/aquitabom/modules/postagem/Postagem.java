package br.com.aquitabom.modules.postagem;

import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.usuario.Usuario;
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
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "postagem")
public class Postagem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Column(name = "imagem_url", nullable = false, length = 300)
    private String imagemUrl;

    @Column(nullable = false)
    private Long likes = 0L;

    @Column(nullable = false)
    private Integer nota;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatusPostagem status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    protected Postagem() {}

    public Postagem(String titulo, String descricao, String imagemUrl, Usuario usuario, Restaurante restaurante,
                    Integer nota, StatusPostagem status) {
        if (nota == null || nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota deve estar entre 1 e 5");
        }

        this.titulo = titulo;
        this.descricao = descricao;
        this.imagemUrl = imagemUrl;
        this.usuario = usuario;
        this.restaurante = restaurante;
        this.nota = nota;
        this.status = status;
        this.likes = 0L;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getImagemUrl() { return imagemUrl; }
    public Long getLikes() { return likes; }
    public Integer getNota() { return nota; }
    public StatusPostagem getStatus() { return status; }
    public Usuario getUsuario() { return usuario; }
    public Restaurante getRestaurante() { return restaurante; }
    public OffsetDateTime getDataCriacao() { return dataCriacao; }
}
