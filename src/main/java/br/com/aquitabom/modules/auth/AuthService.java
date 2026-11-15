package br.com.aquitabom.modules.auth;

import java.util.List;
import java.util.UUID;

import br.com.aquitabom.modules.auth.dto.AtualizarNomeRequest;
import br.com.aquitabom.modules.auth.dto.AutenticacaoResponse;
import br.com.aquitabom.modules.auth.dto.GoogleLoginRequest;
import br.com.aquitabom.modules.auth.dto.LoginRequest;
import br.com.aquitabom.modules.auth.dto.RefreshTokenRequest;
import br.com.aquitabom.modules.auth.dto.RegistroRequest;
import br.com.aquitabom.modules.auth.dto.UsuarioResponse;
import br.com.aquitabom.modules.auth.google.GoogleTokenVerifier;
import br.com.aquitabom.modules.auth.jwt.JwtService;
import br.com.aquitabom.modules.auth.refresh.RefreshTokenService;
import br.com.aquitabom.core.security.CustomAuthentication;
import br.com.aquitabom.core.security.UsuarioAutenticado;
import br.com.aquitabom.core.exception.EmailJaCadastradoException;
import br.com.aquitabom.modules.role.Role;
import br.com.aquitabom.modules.role.RoleRepository;
import br.com.aquitabom.modules.usuario.Usuario;
import br.com.aquitabom.modules.usuario.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
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
    private final RefreshTokenService refreshTokenService;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(UsuarioRepository usuarioRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       AuthorityMapper authorityMapper,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       GoogleTokenVerifier googleTokenVerifier) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.authorityMapper = authorityMapper;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }

        Usuario usuario = criarUsuario(request.nome().trim(), email, passwordEncoder.encode(request.senha()));
        return construirResposta(usuario);
    }

    @Transactional
    public AutenticacaoResponse login(LoginRequest request) {
        CustomAuthentication autenticado = (CustomAuthentication) authenticationManager.authenticate(
                CustomAuthentication.naoAutenticado(request.email(), request.senha()));
        UsuarioAutenticado principal = (UsuarioAutenticado) autenticado.getPrincipal();
        Usuario usuario = usuarioRepository.getReferenceById(principal.id());
        return gerarResposta(principal, usuario);
    }

    @Transactional
    public AutenticacaoResponse loginComGoogle(GoogleLoginRequest request) {
        GoogleTokenVerifier.UsuarioGoogle usuarioGoogle = googleTokenVerifier.verificar(request.idToken());
        String email = normalizarEmail(usuarioGoogle.email());

        // Se já existe conta com esse e-mail, entra nela; senão cria uma conta nova.
        // Contas criadas pelo Google recebem uma senha aleatória (só entram pelo Google até definirem outra)
        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {
            String nome = usuarioGoogle.nome() == null || usuarioGoogle.nome().isBlank()
                    ? email.substring(0, email.indexOf('@'))
                    : usuarioGoogle.nome().trim();
            return criarUsuario(nome, email, passwordEncoder.encode(UUID.randomUUID().toString()));
        });

        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuário inativo");
        }
        return gerarResposta(usuario);
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, DisabledException.class})
    public AutenticacaoResponse renovarToken(RefreshTokenRequest request) {
        Usuario usuario = refreshTokenService.consumir(request.refreshToken());
        return gerarResposta(usuario);
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revogar(request.refreshToken());
    }

    private Usuario criarUsuario(String nome, String email, String senhaCodificada) {
        Role rolePadrao = roleRepository.findByNome(ROLE_PADRAO)
                .orElseThrow(() -> new IllegalStateException(
                        "Role padrão '" + ROLE_PADRAO + "' não encontrada — é preciso semear os roles/acessos no banco"));

        Usuario usuario = new Usuario(nome, email, senhaCodificada);
        usuario.adicionarRole(rolePadrao);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse dadosUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe mais"));
        return construirResposta(usuario);
    }

    @Transactional
    public UsuarioResponse atualizarNome(UUID id, AtualizarNomeRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuário do token não existe mais"));
        usuario.setNome(request.nome().trim());
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

    private AutenticacaoResponse gerarResposta(Usuario usuario) {
        UsuarioAutenticado principal = new UsuarioAutenticado(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), authorityMapper.mapear(usuario));
        return gerarResposta(principal, usuario);
    }

    private AutenticacaoResponse gerarResposta(UsuarioAutenticado principal, Usuario usuario) {
        return AutenticacaoResponse.bearer(
                jwtService.gerarToken(principal),
                jwtService.getExpiracaoSegundos(),
                refreshTokenService.criar(usuario));
    }

    private static String normalizarEmail(String email) {
        return email.toLowerCase().trim();
    }
}
