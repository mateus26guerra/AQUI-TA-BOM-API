package br.com.aquitabom.modules.restaurante.dto.Request;

import br.com.aquitabom.modules.restaurante.Restaurante;

public record RequestRestaurante(
        String nome,
        String iniciais,
        String latitude,
        String longitude,
        String endereco,
        String descricao,
        String telefone
) {
    public RequestRestaurante(Restaurante restaurante) {
        this(
                restaurante.getNome(),
                restaurante.getIniciais(),
                restaurante.getLatitude(),
                restaurante.getLongitude(),
                restaurante.getEndereco(),
                restaurante.getDescricao(),
                restaurante.getTelefone()
        );
    }
}