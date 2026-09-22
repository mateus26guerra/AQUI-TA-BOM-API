package br.com.aquitabom.modules.curtida;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CurtidaRepository extends JpaRepository<Curtida, UUID> {

    boolean existsByPostagemIdAndUsuarioId(UUID postagemId, UUID usuarioId);

    long deleteByPostagemIdAndUsuarioId(UUID postagemId, UUID usuarioId);

    long deleteByPostagemId(UUID postagemId);

    List<Curtida> findTop2ByPostagemIdOrderByDataCriacaoAsc(UUID postagemId);

    List<Curtida> findTop2ByPostagemIdOrderByDataCriacaoDesc(UUID postagemId);
}
