package com.minsait.onesait.initializrv2.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JWTAuthorizationFilter extends OncePerRequestFilter {

	private static final String HEADER = "Authorization";
	private static final String PREFIX = "Bearer ";
	private String secret;
	private List<String> activeProfile;

	public JWTAuthorizationFilter(String secret, String activeProfiles) {
		super();
		this.secret = secret;
		this.activeProfile = Arrays.asList(activeProfiles.split(","));
		log.info(activeProfile.toString());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (!activeProfile.contains("default")) {
			try {
				if (checkJWTToken(request)) {
					Claims claims = validateToken(request);
					if (claims.get("authorities") != null) {
						setUpSpringAuthentication(claims);
					} else {
						response.setStatus(HttpServletResponse.SC_FORBIDDEN);
						response.sendError(HttpServletResponse.SC_FORBIDDEN,
								"No Authentication header");
						return;
					}
				}
				chain.doFilter(request, response);
			} catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
			}
		} else {
			log.info("Default profile... Skipping security");
			setUpSpringAuthentication();
			chain.doFilter(request, response);
		}
	}
	


	private Claims validateToken(HttpServletRequest request) {
		String jwtToken = request.getHeader(HEADER).replace(PREFIX, "");
		return Jwts.parser().setSigningKey(secret.getBytes()).parseClaimsJws(jwtToken).getBody();
	}

	/**
	 * Authentication method in Spring flow
	 * 
	 * @param claims
	 */
	private void setUpSpringAuthentication(Claims claims) {
		@SuppressWarnings("unchecked")
		List<String> authorities = (List<String>) claims.get("authorities");

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		SecurityContextHolder.getContext().setAuthentication(auth);

	}

	private void setUpSpringAuthentication() {
		List<String> authorities=new ArrayList<>();
		authorities.add("ROLE_DEVELOP");

		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("DEVELOPMENT", null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		SecurityContextHolder.getContext().setAuthentication(auth);

	}

	private boolean checkJWTToken(HttpServletRequest request) {
		String authenticationHeader = request.getHeader(HEADER);
		return !(authenticationHeader == null || !authenticationHeader.startsWith(PREFIX));
	}

}