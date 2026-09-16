package br.com.aquitabom.auth;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import br.com.aquitabom.usuario.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class AuthorityMapper {

    public List<GrantedAuthority> mapear(Usuario usuario) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        usuario.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getNome()));
            role.getAcessos().forEach(acesso ->
                    authorities.add(new SimpleGrantedAuthority(acesso.getNome())));
        });
        return List.copyOf(authorities);
    }

    public List<GrantedAuthority> mapear(Collection<String> nomes) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        nomes.forEach(nome -> authorities.add(new SimpleGrantedAuthority(nome)));
        return List.copyOf(authorities);
    }
}
