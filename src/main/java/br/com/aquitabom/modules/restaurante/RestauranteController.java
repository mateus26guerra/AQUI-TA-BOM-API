package br.com.aquitabom.modules.restaurante;

import br.com.aquitabom.modules.restaurante.api.RestauranteSwagger;
import br.com.aquitabom.modules.restaurante.dto.Request.RequestRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestaurante;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/restaurantes")
public class RestauranteController implements RestauranteSwagger {

    private final RestauranteService restauranteService;

    public RestauranteController(RestauranteService restauranteService) {
        this.restauranteService = restauranteService;
    }

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> salvarRestaurante(@ModelAttribute @Valid RequestRestaurante restaurante) {
        restauranteService.salvarRestaurante(restaurante);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping
    public ResponseEntity<List<ResponseRestaurante>> listarRestaurantes() {
        List<ResponseRestaurante> restaurantes = restauranteService.listarRestaurantes();
        return ResponseEntity.ok(restaurantes);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRestaurante(@PathVariable UUID id) {
        restauranteService.deletarRestaurante(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<Void> atualizarRestaurante(@PathVariable UUID id, @RequestBody @Valid Restaurante restaurante) {
        restauranteService.atualizarRestaurante(id, restaurante);
        return ResponseEntity.ok().build();
    }
}