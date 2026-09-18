package br.com.aquitabom.modules.restaurante.api;

import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.restaurante.dto.Request.RequestAtualizarRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Request.RequestRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestaurante;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "Restaurantes", description = "Gerenciamento de estabelecimentos parceiros")
@SecurityRequirement(name = "bearer-jwt")
public interface RestauranteSwagger {

    @Operation(summary = "Cadastrar restaurante", description = "Cria um novo restaurante no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Restaurante cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<Void> salvarRestaurante(@RequestBody @Valid RequestRestaurante restaurante);

    @Operation(summary = "Listar restaurantes", description = "Retorna uma lista com todos os restaurantes cadastrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<List<ResponseRestaurante>> listarRestaurantes();

    @Operation(summary = "Remover restaurante", description = "Exclui um restaurante existente pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Restaurante removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<Void> deletarRestaurante(
            @Parameter(description = "ID do restaurante", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    );

    @Operation(summary = "Atualizar restaurante parcialmente", description = "Atualiza dados específicos de um restaurante cadastrado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Restaurante atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Restaurante não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    ResponseEntity<Void> atualizarRestaurante(
            @Parameter(description = "ID do restaurante", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @RequestBody @Valid RequestAtualizarRestaurante dto
    );
}