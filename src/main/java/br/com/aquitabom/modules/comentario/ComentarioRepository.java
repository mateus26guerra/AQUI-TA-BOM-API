package br.com.aquitabom.modules.comentario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComentarioRepository extends JpaRepository<Comentario, UUID> {

    List<Comentario> findByPostagemIdOrderByDataCriacaoAsc(UUID postagemId);

    List<Comentario> findByUsuarioIdOrderByDataCriacaoDesc(UUID usuarioId);

    long deleteByPostagemId(UUID postagemId);
    long deleteByIdAndUsuarioId(UUID comentarioId, UUID usuarioId);
    long countByPostagemId(UUID postagemId);
}