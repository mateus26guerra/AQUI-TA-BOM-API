package br.com.aquitabom.modules.auth;

import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.modules.auth.api.AuthOpenApi;
import br.com.aquitabom.modules.auth.dto.AutenticacaoResponse;
import br.com.aquitabom.modules.auth.dto.LoginRequest;
import br.com.aquitabom.modules.auth.dto.RegistroRequest;
import br.com.aquitabom.modules.auth.dto.UsuarioResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth")
public class AuthController implements AuthOpenApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(@RequestBody @Valid RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @Override
    @PostMapping("/login")
    public AutenticacaoResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @Override
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponse me(@AuthenticationPrincipal UsuarioAutenticado principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }
        return authService.dadosUsuario(principal.id());
    }
}