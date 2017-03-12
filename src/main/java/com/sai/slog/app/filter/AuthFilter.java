package com.sai.slog.app.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Login Filter intercepts every request.
 * 1. Checks if the given url is to be avoided.
 * 1. Checks if the user is logged in.
 * 2. If the user is logged in , then checks if the user is authorised to check the following page resource.
 * 3. If the user is authorised , then the request is passed to the next filter.
 * 4. If the user is not logged in, it redirects the current page to landingPage.xhtml.
 *
 * @author Kumar Thangavel
 */
public class AuthFilter implements Filter {

    private Map<String, String> themes = new HashMap<>();
    private FilterConfig filterConfig;

    {
        themes.put("nature", "south-street");
        themes.put("dark", "dark-hive");
        themes.put("standard", "cupertino");
        themes.put("glassy", "glass-x");
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        if (request.getParameter("theme") != null) {
            httpServletRequest.getSession().setAttribute("theme", themes.get(request.getParameter("theme")));
            System.out.println(" Theme: "+httpServletRequest.getSession().getAttribute("theme"));
            ((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getRequestURI());

        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}