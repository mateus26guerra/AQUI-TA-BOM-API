package br.com.aquitabom.modules.restaurante;

import br.com.aquitabom.modules.restaurante.dto.Request.RequestRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestaurante;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;

    public RestauranteService(RestauranteRepository restauranteRepository) {
        this.restauranteRepository = restauranteRepository;
    }

    public void salvarRestaurante(RequestRestaurante dto) {
        // 1. Converte o DTO para a Entidade
        Restaurante restauranteNovo = new Restaurante();
        restauranteNovo.setNome(dto.nome());
        restauranteNovo.setIniciais(dto.iniciais());
        restauranteNovo.setLatitude(dto.latitude());
        restauranteNovo.setLongitude(dto.longitude());
        restauranteNovo.setEndereco(dto.endereco());
        restauranteNovo.setDescricao(dto.descricao());
        restauranteNovo.setTelefone(dto.telefone());

        // 2. Salva a Entidade no banco
        restauranteRepository.save(restauranteNovo);
    }

    public List<ResponseRestaurante> listarRestaurantes() {
        return restauranteRepository.findAll()
                .stream()
                .map(ResponseRestaurante::new)
                .toList();
    }

    public void deletarRestaurante(UUID id) {
        restauranteRepository.deleteById(id);
    }

    public Restaurante buscarRestaurantePorId(UUID id) {
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o ID: " + id));
    }

    public void atualizarRestaurante(UUID id, Restaurante restaurante) {
        Restaurante restauranteAtualizado = buscarRestaurantePorId(id);
        restauranteAtualizado.setNome(restaurante.getNome());
        restauranteAtualizado.setEndereco(restaurante.getEndereco());
        restauranteAtualizado.setTelefone(restaurante.getTelefone());
        restauranteAtualizado.setLatitude(restaurante.getLatitude());
        restauranteAtualizado.setLongitude(restaurante.getLongitude());
        restauranteAtualizado.setDescricao(restaurante.getDescricao());
        restauranteRepository.save(restauranteAtualizado);
    }
}