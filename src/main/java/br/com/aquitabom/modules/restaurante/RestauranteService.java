package br.com.aquitabom.modules.restaurante;

import br.com.aquitabom.core.Storage.StorageService;
import br.com.aquitabom.modules.restaurante.dto.Request.RequestRestaurante;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestaurante;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class RestauranteService {

    private final RestauranteRepository restauranteRepository;
    private final StorageService storageService;

    public RestauranteService(RestauranteRepository restauranteRepository, StorageService storageService) {
        this.restauranteRepository = restauranteRepository;
        this.storageService = storageService;
    }

    @Transactional
    public void salvarRestaurante(RequestRestaurante dto) {
        String urlImagem = null;

        // Se uma imagem foi enviada no DTO, faz o upload para o R2
        if (dto.imagem() != null && !dto.imagem().isEmpty()) {
            try {
                urlImagem = storageService.upload(dto.imagem());
            } catch (Exception e) {
                throw new RuntimeException("Erro ao enviar imagem para o serviço de armazenamento", e);
            }
        }

        // Converte o DTO para a Entidade
        Restaurante restauranteNovo = new Restaurante();
        restauranteNovo.setNome(dto.nome());
        restauranteNovo.setIniciais(dto.iniciais());
        restauranteNovo.setLatitude(dto.latitude());
        restauranteNovo.setLongitude(dto.longitude());
        restauranteNovo.setEndereco(dto.endereco());
        restauranteNovo.setDescricao(dto.descricao());
        restauranteNovo.setTelefone(dto.telefone());
        restauranteNovo.setURLImagem(urlImagem);

        // Salva a Entidade no banco
        restauranteRepository.save(restauranteNovo);
    }

    @Transactional(readOnly = true)
    public List<ResponseRestaurante> listarRestaurantes() {
        return restauranteRepository.findAll()
                .stream()
                .map(ResponseRestaurante::new)
                .toList();
    }

    @Transactional
    public void deletarRestaurante(UUID id) {
        restauranteRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Restaurante buscarRestaurantePorId(UUID id) {
        return restauranteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurante não encontrado com o ID: " + id));
    }

    @Transactional
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