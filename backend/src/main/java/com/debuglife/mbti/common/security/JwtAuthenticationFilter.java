package com.debuglife.mbti.common.security;

import com.debuglife.mbti.common.api.ProblemDetailWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final ProblemDetailWriter problemDetailWriter;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   @Qualifier("customUserDetailsService") UserDetailsService userDetailsService,
                                   ProblemDetailWriter problemDetailWriter) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.problemDetailWriter = problemDetailWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = getJwtFromRequest(request);
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!tokenProvider.validateAccessToken(jwt)) {
            problemDetailWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED", "登录凭证无效或已过期");
            return;
        }

        try {
            String username = tokenProvider.getUsernameFromToken(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!userDetails.isEnabled()) {
                problemDetailWriter.write(request, response, HttpStatus.FORBIDDEN,
                        "USER_DISABLED", "账号已被禁用");
                return;
            }
            Authentication existing = SecurityContextHolder.getContext().getAuthentication();
            if (existing == null || existing instanceof AnonymousAuthenticationToken) {
                var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.debug("Could not set user authentication in security context", ex);
            problemDetailWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED", "登录状态无效，请重新登录");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
