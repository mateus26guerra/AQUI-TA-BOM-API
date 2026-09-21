package br.com.aquitabom.modules.avaliacaolotacao;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.avaliacaolotacao.dto.ResponseLotacao;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/v1/api/avaliacoes-lotacao")
public class AvaliacaoLotacaoController {

    private final AvaliacaoLotacaoService avaliacaoService;

    public AvaliacaoLotacaoController(
            AvaliacaoLotacaoService avaliacaoService
    ) {
        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping("/restaurante/{restauranteId}")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> avaliar(
            @PathVariable UUID restauranteId,
            @RequestParam StatusLotacao status,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {

        avaliacaoService.avaliar(
                restauranteId,
                status,
                principal
        );

        return ResponseEntity.ok().build();
    }
    @GetMapping("/restaurante/{restauranteId}")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseLotacao> consultarLotacao(
            @PathVariable UUID restauranteId
    ) {
        return ResponseEntity.ok(
                avaliacaoService.consultarLotacao(restauranteId)
        );
    }
}