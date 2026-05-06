package com.skybooker.airline.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {

    private final String SECRET = "OPqzAzTkewv1#+WU9&Z*_BsJ7w2z1O!QIv+i&+049po94&%Y$JjwH645*StuUYW^";

    // Generates secure key from secret
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
    // Validates JWT token (checks signature + expiry)
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT validation failed: " + e.getMessage());
            return false;
        }
    }
    // Extracts email
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    // Extracts user role from token (default = PASSENGER)

    public String extractRole(String token) {
        Object role = Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody().get("role");
        return role != null ? role.toString() : "PASSENGER";
    }
}
