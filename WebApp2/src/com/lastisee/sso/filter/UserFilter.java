package com.lastisee.sso.filter;


import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class UserFilter implements Filter {

    private String server;

    private String app;
    public UserFilter() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        server = filterConfig.getInitParameter("server");
        app = filterConfig.getInitParameter("app");

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (Objects.equals("/ssoLogout", ((HttpServletRequest) servletRequest).getServletPath())) {
            ((HttpServletResponse)servletResponse).sendRedirect(server + "/ssoLogout?source="+app);
        }
        String ticket = null;
        if (null != ((HttpServletRequest) servletRequest).getCookies()) {
            for (Cookie cookie : ((HttpServletRequest) servletRequest).getCookies()) {
                if (Objects.equals(cookie.getName(), "Ticket_Granting_Ticket")) {
                    ticket = cookie.getValue();
                    break;
                }
            }
        }
        if (!Objects.equals(null, ticket)) {

            //判断超时时间
            String[] values = ticket.split(":");
            ticket = servletRequest.getParameter("ticket");
            if (Long.valueOf(values[1]) < System.currentTimeMillis()) {
                //已超时
                if (Objects.equals(null, ticket)) {
                    ((HttpServletResponse) servletResponse).sendRedirect(server + "/ssoLogin?source=" + app);
                    return;
                } else {
                    ticket = ticket + ":" + (System.currentTimeMillis() + 10000);
                    ((HttpServletResponse)servletResponse).addCookie(new Cookie("Ticket_Granting_Ticket",ticket));
                    filterChain.doFilter(servletRequest, servletResponse);
                    return;
                }


            }
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        ticket = servletRequest.getParameter("ticket");
        if (!Objects.equals(null, ticket) && !Objects.equals("", ticket.trim())) {
            ticket = ticket + ":" + (System.currentTimeMillis() + 10000);
            ((HttpServletResponse) servletResponse).addCookie(new Cookie("Ticket_Granting_Ticket", ticket));
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            ((HttpServletResponse)servletResponse).sendRedirect(server + "/ssoLogin?source=" + app);
        }

    }

    @Override
    public void destroy() {

    }
}
