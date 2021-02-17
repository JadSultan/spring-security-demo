package com.icode.securitydemo.security.util;

import com.icode.securitydemo.security.service.MyUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expirationDurationMs}")
    private int expirationDurationMs;

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public int extractId(String token){
        final Claims claims = extractAllClaims(token);
        int id = (int) claims.get("id");
        return id;
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(MyUserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getId(), userDetails.getUsername(), userDetails.getAuthorities().toString());

    }

    public String generateRefreshToken(MyUserDetails userDetails){
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getId(), userDetails.getUsername(), userDetails.getAuthorities().toString());
    }

    public String createToken(Map<String, Object> claims, int id, String subject, String Role){
        claims.put("id", id);
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .claim("Role", Role)
                .setExpiration(new Date(System.currentTimeMillis() + expirationDurationMs))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    public boolean validateToken(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
