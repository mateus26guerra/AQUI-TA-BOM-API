package br.com.aquitabom.modules.avaliacaorestaurante;

import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.usuario.Usuario;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "avaliacao_restaurante",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_avaliacao_usuario_restaurante",
                        columnNames = {"usuario_id", "restaurante_id"}
                )
        }
)
public class AvaliacaoRestaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer nota;

    @Column(length = 500)
    private String comentario;

    @Column(name = "data_criacao", nullable = false)
    private OffsetDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    protected AvaliacaoRestaurante() {}

    public AvaliacaoRestaurante(
            Integer nota,
            String comentario,
            Usuario usuario,
            Restaurante restaurante
    ) {

        if (nota == null || nota < 1 || nota > 5) {
            throw new IllegalArgumentException(
                    "A nota deve estar entre 1 e 5"
            );
        }

        this.nota = nota;
        this.comentario = comentario;
        this.usuario = usuario;
        this.restaurante = restaurante;
        this.dataCriacao = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Integer getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public void atualizar(
            Integer nota,
            String comentario
    ) {

        if (nota == null || nota < 1 || nota > 5) {
            throw new IllegalArgumentException(
                    "A nota deve estar entre 1 e 5"
            );
        }

        this.nota = nota;
        this.comentario = comentario;
        this.dataCriacao = OffsetDateTime.now();
    }
}