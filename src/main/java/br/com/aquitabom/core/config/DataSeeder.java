package br.com.aquitabom.core.config;

import br.com.aquitabom.modules.acesso.Acesso;
import br.com.aquitabom.modules.acesso.AcessoRepository;
import br.com.aquitabom.modules.formapagamento.CategoriaPagamento;
import br.com.aquitabom.modules.formapagamento.FormaPagamento;
import br.com.aquitabom.modules.formapagamento.FormaPagamentoRepository;
import br.com.aquitabom.modules.role.Role;
import br.com.aquitabom.modules.role.RoleRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataSeeder implements CommandLineRunner {

    private final AcessoRepository acessoRepository;
    private final RoleRepository roleRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final FormaPagamentoRepository formaPagamentoRepository;

    public DataSeeder(AcessoRepository acessoRepository,
                      RoleRepository roleRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      FormaPagamentoRepository formaPagamentoRepository) {
        this.acessoRepository = acessoRepository;
        this.roleRepository = roleRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.formaPagamentoRepository = formaPagamentoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Criar Acessos/Permissões do Sistema
        Acesso acessoPing = criarAcessoSeNaoExistir("PING", "Permissão para ping");
        Acesso acessoRestauranteRead = criarAcessoSeNaoExistir("RESTAURANTE_READ", "Listar restaurantes");
        Acesso acessoRestauranteWrite = criarAcessoSeNaoExistir("RESTAURANTE_WRITE", "Criar/Editar restaurantes");

        // --- NOVAS PERMISSÕES DE POSTAGEM ---
        Acesso acessoPostagemRead = criarAcessoSeNaoExistir("POSTAGEM_READ", "Visualizar postagens");
        Acesso acessoPostagemWrite = criarAcessoSeNaoExistir("POSTAGEM_WRITE", "Criar/Editar suas próprias postagens");

        // 2. Criar ROLE_USER com permissões de Usuário Comum (inclui criar postagens!)
        Role roleUser = roleRepository.findByNome("ROLE_USER").orElseGet(() -> {
            Role role = new Role("ROLE_USER", "Usuário padrão");
            role.getAcessos().add(acessoPing);
            role.getAcessos().add(acessoRestauranteRead);
            role.getAcessos().add(acessoPostagemRead);  // Pode ver postagens
            role.getAcessos().add(acessoPostagemWrite); // Pode Criar Fotos/Postagens de Comida!
            return roleRepository.save(role);
        });

        // 3. Criar ROLE_ADMIN (Tem todas as permissões)
        Role roleAdmin = roleRepository.findByNome("ROLE_ADMIN").orElseGet(() -> {
            Role role = new Role("ROLE_ADMIN", "Administrador do sistema");
            role.getAcessos().add(acessoPing);
            role.getAcessos().add(acessoRestauranteRead);
            role.getAcessos().add(acessoRestauranteWrite);
            role.getAcessos().add(acessoPostagemRead);
            role.getAcessos().add(acessoPostagemWrite);
            return roleRepository.save(role);
        });

        // 4. Criar o Usuário Admin Padrão
        String emailAdmin = "admin@aquitabom.com.br";
        if (!usuarioRepository.existsByEmail(emailAdmin)) {
            Usuario admin = new Usuario(
                    "Administrador",
                    emailAdmin,
                    passwordEncoder.encode("Admin@123456")
            );
            admin.adicionarRole(roleAdmin);
            admin.adicionarRole(roleUser);
            usuarioRepository.save(admin);
        }

        // 5. Criar as Formas de Pagamento aceitas pelos restaurantes
        criarFormaPagamentoSeNaoExistir("Dinheiro", CategoriaPagamento.DINHEIRO);
        criarFormaPagamentoSeNaoExistir("Pix", CategoriaPagamento.PIX);
        criarFormaPagamentoSeNaoExistir("Cartão de Crédito", CategoriaPagamento.CARTAO_CREDITO);
        criarFormaPagamentoSeNaoExistir("Cartão de Débito", CategoriaPagamento.CARTAO_DEBITO);

        criarFormaPagamentoSeNaoExistir("Alelo Refeição", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Pluxee Refeição (Sodexo)", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Ticket Restaurante", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("VR Refeição", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Ben Refeição", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Up Refeição", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("VeroCard Refeição", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Caju", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Flash", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("iFood Benefícios", CategoriaPagamento.VALE_REFEICAO);
        criarFormaPagamentoSeNaoExistir("Swile", CategoriaPagamento.VALE_REFEICAO);

        criarFormaPagamentoSeNaoExistir("Alelo Alimentação", CategoriaPagamento.VALE_ALIMENTACAO);
        criarFormaPagamentoSeNaoExistir("Pluxee Alimentação (Sodexo)", CategoriaPagamento.VALE_ALIMENTACAO);
        criarFormaPagamentoSeNaoExistir("Ticket Alimentação", CategoriaPagamento.VALE_ALIMENTACAO);
        criarFormaPagamentoSeNaoExistir("VR Alimentação", CategoriaPagamento.VALE_ALIMENTACAO);
    }

    private void criarFormaPagamentoSeNaoExistir(String nome, CategoriaPagamento categoria) {
        if (!formaPagamentoRepository.existsByNome(nome)) {
            formaPagamentoRepository.save(new FormaPagamento(nome, categoria));
        }
    }

    private Acesso criarAcessoSeNaoExistir(String nome, String descricao) {
        return acessoRepository.findByNome(nome)
                .orElseGet(() -> acessoRepository.save(new Acesso(nome, descricao)));
    }
}