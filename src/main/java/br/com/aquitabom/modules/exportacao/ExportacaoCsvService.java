package br.com.aquitabom.modules.exportacao;

import br.com.aquitabom.modules.curtida.Curtida;
import br.com.aquitabom.modules.curtida.CurtidaRepository;
import br.com.aquitabom.modules.postagem.PostagemRepository;
import br.com.aquitabom.modules.postagem.PostagemService;
import br.com.aquitabom.modules.postagem.cto.ResponsePostagem;
import br.com.aquitabom.modules.restaurante.RestauranteService;
import br.com.aquitabom.modules.restaurante.dto.Response.ResponseRestaurante;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ExportacaoCsvService {

    private static final String CABECALHO_CURTIDAS =
            "postagemId,tituloPostagem,nomeRestaurante,usuarioId,nomeUsuario,dataCurtida\n";

    private final PostagemService postagemService;
    private final PostagemRepository postagemRepository;
    private final CurtidaRepository curtidaRepository;
    private final UsuarioRepository usuarioRepository;
    private final RestauranteService restauranteService;

    public ExportacaoCsvService(
            PostagemService postagemService,
            PostagemRepository postagemRepository,
            CurtidaRepository curtidaRepository,
            UsuarioRepository usuarioRepository,
            RestauranteService restauranteService
    ) {
        this.postagemService = postagemService;
        this.postagemRepository = postagemRepository;
        this.curtidaRepository = curtidaRepository;
        this.usuarioRepository = usuarioRepository;
        this.restauranteService = restauranteService;
    }

    @Transactional(readOnly = true)
    public String exportarPostagens() {

        StringBuilder csv = new StringBuilder();
        csv.append("id,usuarioId,titulo,descricao,imagemUrl,likes,nota,status,nomeUsuario,")
                .append("nomeRestaurante,dataCriacao,quantidadeComentarios,nomesUsuariosCurtiram\n");

        for (ResponsePostagem p : postagemService.listarTodas()) {
            csv.append(String.join(",",
                    escaparCsv(p.id()),
                    escaparCsv(p.usuarioId()),
                    escaparCsv(p.titulo()),
                    escaparCsv(p.descricao()),
                    escaparCsv(p.imagemUrl()),
                    escaparCsv(p.likes()),
                    escaparCsv(p.nota()),
                    escaparCsv(p.status()),
                    escaparCsv(p.nomeUsuario()),
                    escaparCsv(p.nomeRestaurante()),
                    escaparCsv(p.dataCriacao()),
                    escaparCsv(p.quantidadeComentarios()),
                    escaparCsv(String.join("; ", p.nomesUsuariosCurtiram()))
            )).append("\n");
        }

        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String exportarCurtidasDaPostagem(UUID postagemId) {

        if (!postagemRepository.existsById(postagemId)) {
            throw new IllegalArgumentException(
                    "Postagem não encontrada"
            );
        }

        return montarCsvCurtidas(curtidaRepository.findByPostagemIdOrderByDataCriacaoDesc(postagemId));
    }

    @Transactional(readOnly = true)
    public String exportarCurtidasDoUsuario(UUID usuarioId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new IllegalArgumentException(
                    "Usuário não encontrado"
            );
        }

        return montarCsvCurtidas(curtidaRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuarioId));
    }

    @Transactional(readOnly = true)
    public String exportarTodasCurtidas() {
        return montarCsvCurtidas(curtidaRepository.findAllByOrderByDataCriacaoDesc());
    }

    @Transactional(readOnly = true)
    public String exportarRestaurantes() {

        StringBuilder csv = new StringBuilder();
        csv.append("id,nome,iniciais,latitude,longitude,endereco,descricao,telefone,urlImagem\n");

        for (ResponseRestaurante r : restauranteService.listarRestaurantes()) {
            csv.append(String.join(",",
                    escaparCsv(r.id()),
                    escaparCsv(r.nome()),
                    escaparCsv(r.iniciais()),
                    escaparCsv(r.latitude()),
                    escaparCsv(r.longitude()),
                    escaparCsv(r.endereco()),
                    escaparCsv(r.descricao()),
                    escaparCsv(r.telefone()),
                    escaparCsv(r.urlImagem())
            )).append("\n");
        }

        return csv.toString();
    }

    private String montarCsvCurtidas(List<Curtida> curtidas) {

        StringBuilder csv = new StringBuilder(CABECALHO_CURTIDAS);

        for (Curtida c : curtidas) {
            csv.append(String.join(",",
                    escaparCsv(c.getPostagem().getId()),
                    escaparCsv(c.getPostagem().getTitulo()),
                    escaparCsv(c.getPostagem().getRestaurante().getNome()),
                    escaparCsv(c.getUsuario().getId()),
                    escaparCsv(c.getUsuario().getNome()),
                    escaparCsv(c.getDataCriacao())
            )).append("\n");
        }

        return csv.toString();
    }

    private String escaparCsv(Object valor) {
        if (valor == null) {
            return "";
        }

        String texto = valor.toString();

        if (texto.contains(",") || texto.contains("\"") || texto.contains("\n") || texto.contains("\r")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }

        return texto;
    }
}
