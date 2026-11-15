package br.com.aquitabom.modules.recomendacao;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente do serviço de NLP que ordena postagens por similaridade com os interesses do usuário.
 * Ele só mede proximidade entre textos: quem decide o perfil e as candidatas é o PostagemService.
 */
@Component
public class RecomendacaoClient {

    private static final Logger log = LoggerFactory.getLogger(RecomendacaoClient.class);

    private final RestClient restClient;

    public RecomendacaoClient(@Value("${recomendacao.url}") String url,
                              @Value("${recomendacao.timeout:10s}") Duration timeout) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(timeout);
        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Retorna os IDs das candidatas, do mais parecido com o perfil para o menos parecido.
     * Vazio se o serviço estiver fora do ar, demorar demais ou responder algo inesperado:
     * nesse caso quem chamou deve usar a ordem normal do feed.
     */
    public Optional<List<String>> ordenar(List<String> textosDoPerfil, List<Candidata> candidatas) {
        try {
            RespostaRecomendacao resposta = restClient.post()
                    .uri("/recommend")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PedidoRecomendacao(textosDoPerfil, candidatas))
                    .retrieve()
                    .body(RespostaRecomendacao.class);

            if (resposta == null || resposta.results() == null || resposta.results().isEmpty()) {
                log.warn("Serviço de recomendação respondeu sem resultados para {} candidatas", candidatas.size());
                return Optional.empty();
            }
            return Optional.of(resposta.results().stream()
                    .map(Resultado::id)
                    .filter(id -> id != null && !id.isBlank())
                    .toList());
        } catch (Exception e) {
            log.warn("Serviço de recomendação indisponível, usando a ordem normal do feed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record Candidata(String id, String text) {
    }

    record PedidoRecomendacao(List<String> profileTexts, List<Candidata> candidates) {
    }

    record RespostaRecomendacao(List<Resultado> results) {
    }

    record Resultado(String id, Double score) {
    }
}
