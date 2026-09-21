package br.com.aquitabom.modules.comentario;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.comentario.dto.RequestComentario;
import br.com.aquitabom.modules.comentario.dto.ResponseComentario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/comentarios")
@Tag(
        name = "Comentários",
        description = "Endpoints para criar e consultar comentários das postagens"
)
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @DeleteMapping("/{comentarioId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Excluir comentário",
            description = "Exclui um comentário criado pelo usuário autenticado."
    )
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Comentário excluído com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "O comentário pertence a outro usuário"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Comentário não encontrado"
            )
    })
    public ResponseEntity<Void> deletarComentario(
            @PathVariable UUID comentarioId,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {

        comentarioService.deletarComentario(
                comentarioId,
                principal
        );

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/postagem/{postagemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Criar comentário",
            description = "Adiciona um comentário em uma postagem. É necessário estar autenticado."
    )
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comentário criado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados do comentário inválidos"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Postagem ou usuário não encontrado"
            )
    })
    public ResponseEntity<ResponseComentario> criarComentario(
            @PathVariable UUID postagemId,
            @RequestBody @Valid RequestComentario request,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {

        return ResponseEntity.ok(
                comentarioService.criar(
                        postagemId,
                        request,
                        principal
                )
        );
    }

    @GetMapping("/postagem/{postagemId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Listar comentários da postagem",
            description = "Retorna todos os comentários de uma postagem."
    )
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comentários encontrados"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuário não autenticado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Postagem não encontrada"
            )
    })
    public ResponseEntity<List<ResponseComentario>> listarComentarios(
            @PathVariable UUID postagemId
    ) {

        return ResponseEntity.ok(
                comentarioService.listarPorPostagem(postagemId)
        );
    }
}