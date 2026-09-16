package br.com.aquitabom.modules.auth;

import java.util.List;
import java.util.UUID;

import br.com.aquitabom.modules.auth.dto.AutenticacaoResponse;
import br.com.aquitabom.modules.auth.dto.LoginRequest;
import br.com.aquitabom.modules.auth.dto.RegistroRequest;
import br.com.aquitabom.modules.auth.dto.UsuarioResponse;
import br.com.aquitabom.modules.auth.jwt.JwtService;
import br.com.aquitabom.core.security.CustomAuthentication;
import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.core.exception.EmailJaCadastradoException;
import br.com.aquitabom.modules.role.Role;
import br.com.aquitabom.modules.role.RoleRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String ROLE_PADRAO = "ROLE_USER";

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuthorityMapper authorityMapper;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       AuthorityMapper authorityMapper,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.authorityMapper = authorityMapper;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }

        Role rolePadrao = roleRepository.findByNome(ROLE_PADRAO)
                .orElseThrow(() -> new IllegalStateException(
                        "Role padrão '" + ROLE_PADRAO + "' não encontrada — é preciso semear os roles/acessos no banco"));

        Usuario usuario = new Usuario(request.nome().trim(), email, passwordEncoder.encode(request.senha()));
        usuario.adicionarRole(rolePadrao);
        usuarioRepository.save(usuario);

        return construirResposta(usuario);
    }

    public AutenticacaoResponse login(LoginRequest request) {
        CustomAuthentication autenticado = (CustomAuthentication) authenticationManager.authenticate(
                CustomAuthentication.naoAutenticado(request.email(), request.senha()));
        return gerarResposta((UsuarioAutenticado) autenticado.getPrincipal());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse dadosUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe mais"));
        return construirResposta(usuario);
    }

    private UsuarioResponse construirResposta(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(Role::getNome)
                .sorted()
                .toList();
        List<String> authorities = authorityMapper.mapear(usuario).stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), roles, authorities);
    }

    private AutenticacaoResponse gerarResposta(UsuarioAutenticado principal) {
        return AutenticacaoResponse.bearer(jwtService.gerarToken(principal), jwtService.getExpiracaoSegundos());
    }

    private static String normalizarEmail(String email) {
        return email.toLowerCase().trim();
    }
}
