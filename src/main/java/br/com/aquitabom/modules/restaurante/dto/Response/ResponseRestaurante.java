package br.com.aquitabom.modules.restaurante.dto.Response;

import br.com.aquitabom.modules.formapagamento.dto.ResponseFormaPagamento;
import br.com.aquitabom.modules.restaurante.Restaurante;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record ResponseRestaurante(
        UUID id,
        String nome,
        String iniciais,
        String latitude,
        String longitude,
        String endereco,
        String descricao,
        String telefone,
        String urlImagem,
        String urlCardapio,
        @JsonFormat(pattern = "HH:mm") LocalTime horarioAbertura,
        @JsonFormat(pattern = "HH:mm") LocalTime horarioFechamento,
        List<ResponseFormaPagamento> formasPagamento
) {
    public ResponseRestaurante(Restaurante restaurante) {
        this(
                restaurante.getId(),
                restaurante.getNome(),
                restaurante.getIniciais(),
                restaurante.getLatitude(),
                restaurante.getLongitude(),
                restaurante.getEndereco(),
                restaurante.getDescricao(),
                restaurante.getTelefone(),
                restaurante.getURLImagem(),
                restaurante.getUrlCardapio(),
                restaurante.getHorarioAbertura(),
                restaurante.getHorarioFechamento(),
                restaurante.getFormasPagamento().stream()
                        .map(ResponseFormaPagamento::new)
                        .sorted(Comparator.comparing(ResponseFormaPagamento::nome))
                        .toList()
        );
    }
}