package controller;

import com.monframework.annotations.Controller;
import com.monframework.annotations.GetMapping;
import com.monframework.annotations.PostMapping;
import com.monframework.annotations.Anonym;
import com.monframework.annotations.Authorized;
import com.monframework.annotations.Role;
import com.monframework.models.ModelView;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SecurityController {

    @GetMapping("/test/anonym")
    @Anonym
    public ModelView anonym() {
        ModelView mv = new ModelView("../views/security/test-anonym.jsp");
        mv.addObject("message", "Accessible par tout le monde (anonyme)");
        return mv;
    }

    @GetMapping("/test/auth")
    @Authorized
    public ModelView auth(HttpSession session) {
        ModelView mv = new ModelView("/views/security/test-auth.jsp");
        mv.addObject("user", session.getAttribute("user"));
        return mv;
    }

    @GetMapping("/test/role")
    @Role({"admin"})
    public ModelView role(HttpSession session) {
        ModelView mv = new ModelView("/views/security/test-role.jsp");
        mv.addObject("user", session.getAttribute("user"));
        mv.addObject("role", session.getAttribute("userRole"));
        return mv;
    }

    @GetMapping("/login")
    @Anonym
    public ModelView loginForm() {
        return new ModelView("/views/security/login.jsp");
    }

    @PostMapping("/login")
    @Anonym
    public ModelView doLogin(HttpServletRequest req) {
        String username = req.getParameter("username");
        String role = req.getParameter("role");
        HttpSession session = req.getSession(true);
        session.setAttribute("user", username);
        session.setAttribute("userRole", role);
        return new ModelView("redirect:/");
    }

    @PostMapping("/logout")
    public ModelView logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        return new ModelView("redirect:/");
    }
}
