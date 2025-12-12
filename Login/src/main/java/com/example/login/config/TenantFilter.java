package com.example.login.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Reads X-Institution header and sets app.current_institution via SET LOCAL.
 * Requires each request to open a new DB connection; this uses DataSource to set
 * session-local variable for RLS policies in Postgres.
 */
@Component
public class TenantFilter implements Filter {
    private final DataSource ds;
    public TenantFilter(DataSource ds) { this.ds = ds; }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String inst = req.getHeader("X-Institution-Id");
        if (inst != null) {
            try (Connection c = ds.getConnection();
                 PreparedStatement ps = c.prepareStatement("SET LOCAL app.current_institution = ?;")) {
                ps.setString(1, inst);
                ps.execute();
            } catch (Exception e) {
                // log but continue
            }
        }
        chain.doFilter(request, response);
    }
}
