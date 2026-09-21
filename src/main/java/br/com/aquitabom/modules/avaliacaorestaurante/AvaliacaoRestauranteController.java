package br.com.aquitabom.modules.avaliacaorestaurante;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.avaliacaorestaurante.dto.RequestAvaliacaoRestaurante;
import br.com.aquitabom.modules.avaliacaorestaurante.dto.ResponseAvaliacaoRestaurante;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/avaliacoes-restaurantes")
@Tag(
        name = "Avaliações de Restaurantes",
        description = "Avaliação dos restaurantes pelos usuários"
)
public class AvaliacaoRestauranteController {

    private final AvaliacaoRestauranteService avaliacaoService;

    public AvaliacaoRestauranteController(
            AvaliacaoRestauranteService avaliacaoService
    ) {
        this.avaliacaoService = avaliacaoService;
    }

    @DeleteMapping("/{avaliacaoId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Excluir avaliação",
            description = "Exclui uma avaliação criada pelo usuário autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Avaliação excluída com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "A avaliação pertence a outro usuário"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Avaliação não encontrada"
            )
    })
    public ResponseEntity<Void> deletarAvaliacao(
            @PathVariable UUID avaliacaoId,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {

        avaliacaoService.deletarAvaliacao(
                avaliacaoId,
                principal
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restaurante/{restauranteId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Avaliar restaurante",
            description = "Cria ou atualiza a avaliação do usuário para o restaurante."
    )
    public ResponseEntity<ResponseAvaliacaoRestaurante> avaliar(
            @PathVariable UUID restauranteId,
            @RequestBody @Valid RequestAvaliacaoRestaurante request,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {

        return ResponseEntity.ok(
                avaliacaoService.avaliar(
                        restauranteId,
                        request,
                        principal
                )
        );
    }

    @GetMapping("/restaurante/{restauranteId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(
            summary = "Listar avaliações do restaurante",
            description = "Retorna todas as avaliações feitas para o restaurante."
    )
    public ResponseEntity<List<ResponseAvaliacaoRestaurante>> listar(
            @PathVariable UUID restauranteId
    ) {

        return ResponseEntity.ok(
                avaliacaoService.listarPorRestaurante(restauranteId)
        );
    }
}