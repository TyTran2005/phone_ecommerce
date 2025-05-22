package com.poly.config;

import com.poly.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // Lấy đối tượng account từ session
        Account account = (Account) request.getSession().getAttribute("account");
        if (account != null && account.getAdmin() != null && account.getAdmin()) {
            // Nếu tồn tại và có quyền admin, cho phép request đi tiếp
            return true;
        }
        // Nếu không, chuyển hướng về trang đăng nhập
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return false;
    }
}
