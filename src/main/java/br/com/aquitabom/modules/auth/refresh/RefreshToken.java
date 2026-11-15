package br.com.aquitabom.modules.auth.refresh;

import java.time.OffsetDateTime;
import java.util.UUID;

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
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Guardamos só o hash SHA-256: se o banco vazar, os tokens não podem ser usados
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    protected RefreshToken() {
    }

    public RefreshToken(String tokenHash, Usuario usuario, OffsetDateTime expiraEm) {
        this.tokenHash = tokenHash;
        this.usuario = usuario;
        this.expiraEm = expiraEm;
    }

    public boolean isExpirado() {
        return expiraEm.isBefore(OffsetDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public OffsetDateTime getExpiraEm() {
        return expiraEm;
    }
}
