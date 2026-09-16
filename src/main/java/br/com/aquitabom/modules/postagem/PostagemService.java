package br.com.aquitabom.modules.postagem;

import br.com.aquitabom.core.Storage.StorageService;
import br.com.aquitabom.core.security.UsuarioAutenticado;


import br.com.aquitabom.modules.postagem.cto.ResponsePostagem;
import br.com.aquitabom.modules.restaurante.Restaurante;
import br.com.aquitabom.modules.restaurante.RestauranteRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class PostagemService {

    private final PostagemRepository postagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteRepository restauranteRepository;
    private final StorageService storageService;

    public PostagemService(PostagemRepository postagemRepository,
                           UsuarioRepository usuarioRepository,
                           RestauranteRepository restauranteRepository,
                           StorageService storageService) {
        this.postagemRepository = postagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteRepository = restauranteRepository;
        this.storageService = storageService;
    }

    @Transactional
    public ResponsePostagem criarPostagem(String titulo, String descricao, UUID restauranteId, MultipartFile imagem, UsuarioAutenticado principal) {
        if (principal == null) {
            throw new IllegalStateException("Usuário autenticado não informado");
        }

        Usuario usuario = usuarioRepository.findById(principal.id())
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurante não encontrado"));

        String urlImagem;
        try {
            urlImagem = storageService.upload(imagem);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar imagem para o Cloudflare R2", e);
        }

        Postagem postagem = new Postagem(titulo, descricao, urlImagem, usuario, restaurante);
        postagemRepository.save(postagem);

        return new ResponsePostagem(postagem);
    }

    @Transactional(readOnly = true)
    public List<ResponsePostagem> listarTodas() {
        return postagemRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(ResponsePostagem::new)
                .toList();
    }
}