package br.com.aquitabom.modules.restaurante.dto.Request;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record RequestAtualizarRestaurante(
        String nome,
        String latitude,
        String longitude,
        String endereco,
        String descricao,
        String telefone,
        String urlCardapio,
        @DateTimeFormat(pattern = "HH:mm") LocalTime horarioAbertura,
        @DateTimeFormat(pattern = "HH:mm") LocalTime horarioFechamento,
        List<UUID> formasPagamentoIds,
        MultipartFile imagem
) {
}