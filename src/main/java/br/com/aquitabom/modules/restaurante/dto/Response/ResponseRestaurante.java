package br.com.aquitabom.modules.restaurante.dto.Response;

import br.com.aquitabom.modules.restaurante.Restaurante;
import java.util.UUID;

public record ResponseRestaurante(
        UUID id,
        String nome,
        String iniciais,
        String latitude,
        String longitude,
        String endereco,
        String descricao,
        String telefone
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
                restaurante.getTelefone()
        );
    }
}