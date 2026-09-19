package br.com.aquitabom.modules.postagem.cto;

import java.util.UUID;

public record ResponseCurtida(UUID postagemId, Long likes, boolean curtidoPeloUsuario) {}
