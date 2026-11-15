package br.com.aquitabom.modules.auth.api;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.auth.dto.AtualizarNomeRequest;
import br.com.aquitabom.modules.auth.dto.AutenticacaoResponse;
import br.com.aquitabom.modules.auth.dto.GoogleLoginRequest;
import br.com.aquitabom.modules.auth.dto.LoginRequest;
import br.com.aquitabom.modules.auth.dto.RefreshTokenRequest;
import br.com.aquitabom.modules.auth.dto.RegistroRequest;
import br.com.aquitabom.modules.auth.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Autenticação", description = "Registro, login e dados do usuário autenticado")
public interface AuthOpenApi {

    @Operation(summary = "Registrar novo usuário", description = "Cria um usuário e retorna seus dados. Rota pública.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado")
    })
    ResponseEntity<UsuarioResponse> registrar(@RequestBody @Valid RegistroRequest request);

    @Operation(summary = "Autenticar usuário", description = "Valida as credenciais e retorna um token JWT. Rota pública.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    AutenticacaoResponse login(@RequestBody @Valid LoginRequest request);

    @Operation(summary = "Entrar com Google", description = "Recebe o ID token do Google (gerado no app), valida e retorna os tokens. Cria a conta se o e-mail ainda não existir. Rota pública.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Token do Google inválido")
    })
    AutenticacaoResponse loginComGoogle(@RequestBody @Valid GoogleLoginRequest request);

    @Operation(summary = "Renovar token", description = "Troca um refresh token válido por um novo access token e um novo refresh token. O refresh token usado deixa de valer. Rota pública.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    })
    AutenticacaoResponse renovarToken(@RequestBody @Valid RefreshTokenRequest request);

    @Operation(summary = "Sair", description = "Invalida o refresh token informado. Rota pública.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sessão encerrada")
    })
    ResponseEntity<Void> logout(@RequestBody @Valid RefreshTokenRequest request);

    @Operation(summary = "Dados do usuário autenticado", description = "Retorna os dados do usuário dono do token JWT enviado.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    UsuarioResponse me(UsuarioAutenticado principal);

    @Operation(summary = "Alterar nome", description = "Altera o nome do usuário dono do token JWT enviado.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Nome alterado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Nome inválido (mínimo 3 caracteres)"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    UsuarioResponse atualizarNome(UsuarioAutenticado principal, @RequestBody @Valid AtualizarNomeRequest request);
}