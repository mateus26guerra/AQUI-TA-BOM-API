package br.com.aquitabom.modules.avaliacaolotacao;

import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.usuario.Usuario;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "avaliacao_lotacao",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_avaliacao_usuario_restaurante",
                        columnNames = {"usuario_id", "restaurante_id"}
                )
        }
)
public class AvaliacaoLotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private Restaurante restaurante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLotacao status;

    @Column(name = "data_criacao", nullable = false)
    private OffsetDateTime dataCriacao;

    protected AvaliacaoLotacao() {
    }

    public AvaliacaoLotacao(
            Restaurante restaurante,
            Usuario usuario,
            StatusLotacao status
    ) {
        this.restaurante = restaurante;
        this.usuario = usuario;
        this.status = status;
        this.dataCriacao = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public StatusLotacao getStatus() {
        return status;
    }

    public OffsetDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setStatus(StatusLotacao status) {
        this.status = status;
    }

    public void setDataCriacao(OffsetDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}