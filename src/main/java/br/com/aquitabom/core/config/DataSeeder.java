package br.com.aquitabom.core.config;

import br.com.aquitabom.modules.acesso.Acesso;
import br.com.aquitabom.modules.acesso.AcessoRepository;
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

    public DataSeeder(AcessoRepository acessoRepository,
                      RoleRepository roleRepository,
                      UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder) {
        this.acessoRepository = acessoRepository;
        this.roleRepository = roleRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
    }

    private Acesso criarAcessoSeNaoExistir(String nome, String descricao) {
        return acessoRepository.findByNome(nome)
                .orElseGet(() -> acessoRepository.save(new Acesso(nome, descricao)));
    }
}