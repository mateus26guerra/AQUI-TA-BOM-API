package br.com.aquitabom.modules.restaurante.dto.Response;

import br.com.aquitabom.modules.avaliacaolotacao.StatusLotacao;
import br.com.aquitabom.modules.formapagamento.dto.ResponseFormaPagamento;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record ResponseRestauranteMapa(
        UUID id,
        String nome,
        String iniciais,
        String latitude,
        String longitude,
        String endereco,
        String urlImagem,
        String urlCardapio,
        @JsonFormat(pattern = "HH:mm") LocalTime horarioAbertura,
        @JsonFormat(pattern = "HH:mm") LocalTime horarioFechamento,
        StatusLotacao statusLotacao,
        List<ResponseFormaPagamento> formasPagamento
) {
}