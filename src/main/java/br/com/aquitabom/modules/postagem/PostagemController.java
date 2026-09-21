package br.com.aquitabom.modules.postagem;

import br.com.aquitabom.core.security.UsuarioAutenticado;

import br.com.aquitabom.modules.postagem.cto.ResponseCurtida;
import br.com.aquitabom.modules.postagem.cto.ResponsePostagem;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/postagens")
public class PostagemController {

    private final PostagemService postagemService;

    public PostagemController(PostagemService postagemService) {
        this.postagemService = postagemService;
    }


    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletar(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuário não autenticado"
            );
        }

        postagemService.deletarPostagem(id, principal);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponsePostagem> criar(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam("restauranteId") UUID restauranteId,
            @RequestParam("imagem") MultipartFile imagem,
            @RequestParam("nota") Integer nota,
            @RequestParam(value = "status", required = false) StatusPostagem status,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {
    if (principal == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
    }

    ResponsePostagem postagem = postagemService.criarPostagem(titulo, descricao, restauranteId, imagem, principal, nota, status);
    return ResponseEntity.status(HttpStatus.CREATED).body(postagem);
}

    @GetMapping
    public ResponseEntity<List<ResponsePostagem>> listar() {
        return ResponseEntity.ok(postagemService.listarTodas());
    }

    @GetMapping("/minhas")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ResponsePostagem>> listarMinhasPostagens(
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return ResponseEntity.ok(postagemService.listarMinhasPostagens(principal));
    }

    @PostMapping("/{id}/like")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseCurtida> curtir(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return ResponseEntity.ok(postagemService.curtir(id, principal));
    }

    @DeleteMapping("/{id}/like")
    @SecurityRequirement(name = "bearer-jwt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseCurtida> descurtir(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UsuarioAutenticado principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        return ResponseEntity.ok(postagemService.descurtir(id, principal));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ResponsePostagem>> listarPostagensDoUsuario(
            @PathVariable UUID usuarioId
    ) {
        return ResponseEntity.ok(
                postagemService.listarPostagensDoUsuario(usuarioId)
        );
    }
}
