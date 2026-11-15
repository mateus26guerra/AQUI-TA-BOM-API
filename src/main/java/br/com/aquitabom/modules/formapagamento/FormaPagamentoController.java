package br.com.aquitabom.modules.formapagamento;

import br.com.aquitabom.modules.formapagamento.dto.ResponseFormaPagamento;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/api/formas-pagamento")
public class FormaPagamentoController {

    private final FormaPagamentoRepository formaPagamentoRepository;

    public FormaPagamentoController(FormaPagamentoRepository formaPagamentoRepository) {
        this.formaPagamentoRepository = formaPagamentoRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<ResponseFormaPagamento>> listarFormasPagamento() {
        return ResponseEntity.ok(
                formaPagamentoRepository.findAllByOrderByCategoriaAscNomeAsc()
                        .stream()
                        .map(ResponseFormaPagamento::new)
                        .toList()
        );
    }
}
