package br.com.aquitabom.modules.avaliacaolotacao.dto;

import br.com.aquitabom.modules.avaliacaolotacao.StatusLotacao;

import java.util.UUID;

public record ResponseLotacao(
        UUID restauranteId,
        StatusLotacao status,
        long deBoa,
        long embacado,
        long cheioQueSo,
        long totalAvaliacoes
) {
}