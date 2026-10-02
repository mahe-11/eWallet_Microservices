package com.app.filter;

import com.app.service.JWTService;
import com.app.service.MyUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.core.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Slf4j
@Component
public class JWTFilter extends OncePerRequestFilter {
    @Autowired
    JWTService jwtService;

    @Autowired
    private MyUserService myUserService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT FILTER HIT: {}", request.getRequestURI());
         String token = null;
         String username=null;

        /*HEADER CONTAINS TOKEN IN REQUEST IN AUTHORIZATION PART OF THE REQUEST SO WE ONLY
         NEED AUTH PART FIST TO CHECH IF TOEKN IS THERE*/

        String authHeader = request.getHeader("Authorization");


          /*Token in Header starts with word Bearer so we check if header exists,
         and it starts with Bearer IF NOT THEN LET SPRING SECURITY HANDLE THIS
          MOSTLY FOR LOGIN -- Where we don't put any jwt token which generates token for us*/
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*Token in Header starts with word Bearer so we check if header exists,
         and it starts with Bearer */
        if(authHeader!=null && authHeader.startsWith("Bearer ")){
            log.info("JWT token found");
            log.info("Authorization header: {}", authHeader);
            token = authHeader.substring(7); //skip 7 characters from start to skip bearer and get token
            username =jwtService.extractUsername(token); //get username from token
            log.info("Username from JWT: {}", username);

            /* NEXT STEP IS TO CHECK IF USERNAME EXISTS AND USER IS NOT ALREADY AUTHENTICATED
            IF USER IS ALREADY AUTHENTICATED THEN WE NEED NOT DO IT AGAIN */

            /* SecurityContextHolder.getContext().getAuthentication()==null -- this checks if its already authenticated
            * if null them we can authenticate  */

            if(username!=null && SecurityContextHolder.getContext().getAuthentication()==null){

                UserDetails userDetails = myUserService.loadUserByUsername(username);
                log.info("User loaded: {}", userDetails.getUsername());

                //validate token and get user from DB
                if(jwtService.validateToken(token,userDetails)){
                    //once token is valid create a authenticated object that we will add to security context

                    log.info("JWT VALID");

                    UsernamePasswordAuthenticationToken authenticated =
                            new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());

                    //extracts details from the current HTTP request : Remote IP address, Session ID etc. and add those to authentication object

                    authenticated.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    //store authenticated object into security context

                    SecurityContextHolder.getContext().setAuthentication(authenticated);
                    log.info("SecurityContext authentication set");
                }else {
                    log.info("JWT INVALID");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }

                //NOW ONCE AUTHENTICATION OBJECT IS BUILT WE WILL CONTINUE REQUEST
                log.info("Continuing filter chain");
                filterChain.doFilter(request,response);

            }
        }else {
            log.info("Invalid User");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

    }
}
