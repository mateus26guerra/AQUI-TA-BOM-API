package br.com.aquitabom.modules.exportacao;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping(value = "/v1/api/csv", produces = "text/csv")
public class ExportacaoCsvController {

    private final ExportacaoCsvService exportacaoCsvService;

    public ExportacaoCsvController(ExportacaoCsvService exportacaoCsvService) {
        this.exportacaoCsvService = exportacaoCsvService;
    }

    @GetMapping("/postagens")
    public ResponseEntity<byte[]> exportarPostagens() {
        return respostaCsv(exportacaoCsvService.exportarPostagens(), "postagens.csv");
    }

    @GetMapping("/postagens/{id}/curtidas")
    public ResponseEntity<byte[]> exportarCurtidasDaPostagem(@PathVariable("id") UUID id) {
        return respostaCsv(exportacaoCsvService.exportarCurtidasDaPostagem(id), "curtidas-postagem-" + id + ".csv");
    }

    @GetMapping("/usuarios/{usuarioId}/curtidas")
    public ResponseEntity<byte[]> exportarCurtidasDoUsuario(@PathVariable("usuarioId") UUID usuarioId) {
        return respostaCsv(exportacaoCsvService.exportarCurtidasDoUsuario(usuarioId), "curtidas-usuario-" + usuarioId + ".csv");
    }

    @GetMapping("/curtidas")
    public ResponseEntity<byte[]> exportarTodasCurtidas() {
        return respostaCsv(exportacaoCsvService.exportarTodasCurtidas(), "curtidas.csv");
    }

    private ResponseEntity<byte[]> respostaCsv(String conteudo, String nomeArquivo) {
        // BOM UTF-8 para o Excel reconhecer acentuação
        byte[] csv = ("﻿" + conteudo).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }
}
