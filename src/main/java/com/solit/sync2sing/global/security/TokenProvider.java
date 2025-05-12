package com.solit.sync2sing.global.security;

import com.solit.sync2sing.global.response.ResponseCode;
import io.jsonwebtoken.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TokenProvider {

    @Getter
    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${JWT_ACCESS_EXPIRATION}")
    private long accessTokenExpiration;

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshTokenExpiration;

    public String createAccessToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String createRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Token 검증 (만료 여부, 서명 확인)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            throw new JwtException(ResponseCode.INVALID_JWT_SIGNATURE.getMessage());
        } catch (MalformedJwtException e) {
            throw new JwtException(ResponseCode.INVALID_JWT_TOKEN.getMessage());
        } catch (ExpiredJwtException e) {
            throw new JwtException(ResponseCode.EXPIRED_JWT_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new JwtException(ResponseCode.UNSUPPORTED_JWT_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            throw new JwtException(ResponseCode.EMPTY_JWT_CLAIMS.getMessage());
        }

    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public Long getExpirationFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }
    public boolean isTokenExpired(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration().before(new Date());
    }

}
