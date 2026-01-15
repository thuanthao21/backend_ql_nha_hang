package com.dineflow.backend.config;

import com.dineflow.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // --- 1. [FIX] Bỏ qua kiểm tra JWT cho các API công khai ---
        String path = request.getServletPath();

        // Danh sách các đường dẫn không cần kiểm tra Token
        if (path.contains("/api/auth") || path.contains("/ws") || path.contains("/login")) {
            filterChain.doFilter(request, response);
            return; // Dừng filter này tại đây, chuyển tiếp cho filter tiếp theo
        }

        // --- 2. Lấy header Authorization ---
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Nếu không có header hoặc không bắt đầu bằng Bearer -> Cho qua (để SecurityConfig xử lý 403)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // --- 3. Trích xuất Token ---
        try {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            // --- 4. Xác thực Token ---
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Tạo đối tượng Authentication
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Lưu vào Context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Nếu token lỗi (hết hạn, sai format...), log ra nhưng KHÔNG throw exception
            // Để request đi tiếp, SecurityConfig sẽ chặn lại trả về 403 nếu cần thiết
            System.err.println("Lỗi xác thực JWT: " + e.getMessage());
        }

        // Chuyển tiếp request
        filterChain.doFilter(request, response);
    }
}