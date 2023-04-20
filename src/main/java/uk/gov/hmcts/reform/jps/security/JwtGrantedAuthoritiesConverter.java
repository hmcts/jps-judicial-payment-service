package uk.gov.hmcts.reform.jps.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.jps.security.idam.IdamRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;

@Component
@Slf4j
public class JwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String TOKEN_NAME = "tokenName";

    private final IdamRepository idamRepository;

    @Autowired
    public JwtGrantedAuthoritiesConverter(IdamRepository idamRepository) {
        this.idamRepository = idamRepository;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (jwt.containsClaim(TOKEN_NAME) && jwt.getClaim(TOKEN_NAME).equals(ACCESS_TOKEN)) {
            UserInfo userInfo = idamRepository.getUserInfo(jwt.getTokenValue());
            log.info("JwtGrantedAuthoritiesConverter retrieved user info from idamRepository. User Id={}. Roles={}.",
                userInfo.getUid(), userInfo.getRoles()
            );
            authorities = extractAuthorityFromClaims(userInfo.getRoles());
        }
        return authorities;
    }

    private List<GrantedAuthority> extractAuthorityFromClaims(List<String> roles) {
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
