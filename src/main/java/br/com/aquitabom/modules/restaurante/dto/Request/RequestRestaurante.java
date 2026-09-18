package br.com.aquitabom.modules.restaurante.dto.Request;

import org.springframework.web.multipart.MultipartFile;

public record RequestRestaurante(
        String nome,
        String iniciais,
        String latitude,
        String longitude,
        String endereco,
        String descricao,
        String telefone,
        MultipartFile imagem
) {
}