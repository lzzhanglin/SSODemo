package com.lastisee.sso.servlet;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainServlet extends HttpServlet {


    private ExecutorService service = Executors.newFixedThreadPool(10);

    private String servers;

    private void syncCookie(String server, String ticket, String method) {
        service.submit(new Runnable() {
            @Override
            public void run() {
                //创建请求方法的实例，并制定请求URL，如果需要发送post请求，就创建HTTPPost对象
                HttpPost httpPost = new HttpPost(server + "/" + method +"?ticket=" + ticket);
                CloseableHttpClient httpClient = null;
                CloseableHttpResponse response = null;
                try {
                    httpClient = HttpClients.createDefault();
                    response = httpClient.execute(httpPost);
                    //将请求发出去了 就会触发另一个应用的servlet中的servletPath为/setCookie的代码的执行，为另一个应用添加cookie
                    HttpEntity entity = response.getEntity();
                    //entity的值为添加cookie的同时添加的ok，得到ok即说明cookie添加成功
                    String responseContent = EntityUtils.toString(entity, "UTF-8");
                    System.out.println("==================responseContent is: " + responseContent);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        if (null != response) {
                            response.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (null != httpClient) {
                            httpClient.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (Objects.equals("/main", req.getServletPath())) {
            String domains = req.getParameter("domains");
            if (null != domains) {
                this.servers = domains;
            }
            String ticket = req.getParameter("ticket");
            if (null != domains && null != ticket) {
                for (String server : domains.split(",")) {
                    if (!Objects.equals(null, server) && !Objects.equals("", server.trim())) {
                        syncCookie(server, ticket,"setCookie");
                    }
                }
            }

            req.getRequestDispatcher("/WEB-INF/views/main.jsp").forward(req, resp);
        } else if (Objects.equals("/setCookie",req.getServletPath())) {
            String ticket = req.getParameter("ticket");
            resp.addCookie(new Cookie("Ticket_Granting_Ticket",ticket));
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/text; charset=utf-8");
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                out.write("ok");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (null != out) {
                    out.close();
                }
            }
        } else if (Objects.equals("/logout", req.getServletPath())) {
            Cookie cookie = new Cookie("Ticket_Granting_Ticket", null);
            cookie.setMaxAge(0);
            resp.addCookie(cookie);
            //通知其他应用退出
            if (null != servers) {
                for (String server : servers.split(",")) {
                    if (Objects.equals(null, server) && !Objects.equals("",server.trim())) {
                        syncCookie(server,"","removeCookie");

                    }
                }
            }
            req.getRequestDispatcher("/WEB-INF/views/logout.jsp").forward(req,resp);
        } else if (Objects.equals("/removeCookie", req.getServletPath())) {
            Cookie cookie = new Cookie("Ticket_Granting_Ticket", null);
            cookie.setMaxAge(0);
            resp.addCookie(cookie);

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("application/text; charset=utf-8");
            PrintWriter out = null;
            try {
                out = resp.getWriter();
                out.write("ok");
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                if (null != out) {
                    out.close();
                }
            }
        }
    }
}
