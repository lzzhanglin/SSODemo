package com.lastisee.sso.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class LoginServlet extends HttpServlet {

    private String domains;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        domains = config.getInitParameter("domains");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (Objects.equals("/login", req.getServletPath())) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String source = req.getParameter("source");
            if (null == source || Objects.equals("", source)) {
                String referer = req.getHeader("referer");
                source = referer.substring(referer.indexOf("source=") + 7);
            }
            if (Objects.equals(username, password)) {
                String ticket = UUID.randomUUID().toString().replace("-", "");
                System.out.println("************ticket is: "+ticket);
                resp.sendRedirect(source+"main?ticket=" + ticket + "&domains=" +
                        domains.replace(source+",","").replace(","+source,"").replace(source,""));
            } else {
                req.setAttribute("source",source);
                req.getRequestDispatcher("WEB-INF/views/login.jsp").forward(req,resp);
            }
        } else if (Objects.equals("ssoLogin", req.getServletPath())) {
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req,resp );
        } else if (Objects.equals("ssoLogout", req.getServletPath())) {
            String source = req.getParameter("source");
            if (null == source || Objects.equals("", source)) {
                String referer = req.getHeader("referer");
                source = referer.substring(referer.indexOf("source=") + 7);
            }
            resp.sendRedirect(source + "/logout?domains="+
                    domains.replace(source+",","").replace(","+source,"").replace(source,"")));
        }
    }
}
