package br.com.aquitabom.modules.restaurante.dto.Response;

import br.com.aquitabom.modules.avaliacaolotacao.StatusLotacao;

import java.util.UUID;

public record ResponseRestauranteMapa(
        UUID id,
        String nome,
        String iniciais,
        String latitude,
        String longitude,
        String endereco,
        String urlImagem,
        StatusLotacao statusLotacao
) {
}