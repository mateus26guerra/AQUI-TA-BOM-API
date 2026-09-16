package br.com.aquitabom.modules.auth.api;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.auth.dto.AutenticacaoResponse;
import br.com.aquitabom.modules.auth.dto.LoginRequest;
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

    @Operation(summary = "Dados do usuário autenticado", description = "Retorna os dados do usuário dono do token JWT enviado.")
    @SecurityRequirement(name = "bearer-jwt")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado")
    })
    UsuarioResponse me(UsuarioAutenticado principal);
}