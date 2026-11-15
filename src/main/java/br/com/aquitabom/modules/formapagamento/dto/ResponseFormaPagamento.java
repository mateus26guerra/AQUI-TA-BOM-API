package br.com.aquitabom.modules.formapagamento.dto;

import br.com.aquitabom.modules.formapagamento.CategoriaPagamento;
import br.com.aquitabom.modules.formapagamento.FormaPagamento;

import java.util.UUID;

public record ResponseFormaPagamento(
        UUID id,
        String nome,
        CategoriaPagamento categoria
) {
    public ResponseFormaPagamento(FormaPagamento formaPagamento) {
        this(
                formaPagamento.getId(),
                formaPagamento.getNome(),
                formaPagamento.getCategoria()
        );
    }
}
