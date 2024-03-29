package com.mnp.store.services.authorization;

import com.mnp.store.common.security.CookieProvider;
import com.mnp.store.common.security.jwt.JwtToken;
import com.mnp.store.common.security.jwt.JwtTokenProvider;
import com.mnp.store.contracts.authorization.AccountService;
import com.mnp.store.contracts.authorization.dtos.LoginRequestDto;
import com.mnp.store.contracts.authorization.dtos.LoginResponseDto;
import com.mnp.store.contracts.authorization.dtos.LoginStatus;
import com.mnp.store.contracts.authorization.dtos.RefreshTokenDto;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.transaction.Transactional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider tokenProvider;
    private CookieProvider cookieProvider;

    public AccountServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, CookieProvider cookieProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.cookieProvider = cookieProvider;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request, HttpHeaders httpHeaders) {
        String login = request.getUsernameOrEmail();
        String password = request.getPassword();
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login, password);
        Authentication authentication = authenticationManager.authenticate(authToken);

        if (!authentication.isAuthenticated())
            return new LoginResponseDto(LoginStatus.FAILURE, String.format("Failed to authenticate user '%s'.", request.getUsernameOrEmail()));

        JwtToken accessToken = tokenProvider.generateAccessToken(authentication);
        HttpCookie accessCookie = cookieProvider.createAccessTokenCookie(accessToken.getTokenValue(), accessToken.getDuration());

        JwtToken refreshToken = tokenProvider.generateRefreshToken(authentication);
        HttpCookie refreshCookie = cookieProvider.createRefreshTokenCookie(refreshToken.getTokenValue(), refreshToken.getDuration());

        httpHeaders.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        httpHeaders.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new LoginResponseDto(LoginStatus.SUCCESS, String.format("User '%s' is successfully authenticated.", request.getUsernameOrEmail()));
    }

    @Override
    public RefreshTokenDto refreshToken(String refreshToken, HttpHeaders httpHeaders) {
        if (!StringUtils.hasText(refreshToken) || !tokenProvider.validate(refreshToken))
            throw new JwtException("Invalid refresh token");

        Authentication authentication = tokenProvider.getAuthentication(refreshToken);
        JwtToken accessToken = tokenProvider.generateAccessToken(authentication);
        HttpCookie accessCookie = cookieProvider.createAccessTokenCookie(accessToken.getTokenValue(), accessToken.getDuration());
        httpHeaders.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        return new RefreshTokenDto(true, "Access token is refreshed.");
    }
}
