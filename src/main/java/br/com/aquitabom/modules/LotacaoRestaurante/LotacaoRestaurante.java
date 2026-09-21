package br.com.aquitabom.modules.LotacaoRestaurante;

import br.com.aquitabom.modules.avaliacaolotacao.StatusLotacao;
import br.com.aquitabom.modules.restaurante.Restaurante;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lotacao_restaurante",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_lotacao_restaurante",
                        columnNames = "restaurante_id"
                )
        }
)
public class LotacaoRestaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id", nullable = false, unique = true)
    private Restaurante restaurante;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusLotacao status;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    protected LotacaoRestaurante() {
    }

    public LotacaoRestaurante(
            Restaurante restaurante,
            StatusLotacao status
    ) {
        this.restaurante = restaurante;
        this.status = status;
        this.atualizadoEm = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Restaurante getRestaurante() {
        return restaurante;
    }

    public StatusLotacao getStatus() {
        return status;
    }

    public OffsetDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void atualizar(StatusLotacao status) {
        this.status = status;
        this.atualizadoEm = OffsetDateTime.now();
    }
}