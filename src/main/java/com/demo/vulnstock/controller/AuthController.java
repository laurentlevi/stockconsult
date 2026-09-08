package com.demo.vulnstock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private final JdbcTemplate jdbc;

    @Autowired
    public AuthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {

        // === VULNERABILITE : INJECTION SQL (contournement d'authentification) ===
        // La requete est construite par concatenation de chaines, sans requete
        // parametree. Un attaquant peut injecter :  admin' --
        String sql = "SELECT id, username, role FROM users " +
                "WHERE username = '" + username + "' AND password = '" + password + "'";

        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql);
            if (!rows.isEmpty()) {
                Map<String, Object> u = rows.get(0);
                session.setAttribute("user", String.valueOf(u.get("USERNAME")));
                session.setAttribute("role", String.valueOf(u.get("ROLE")));
                return "redirect:/";
            }
            model.addAttribute("error", "Identifiants invalides");
        } catch (Exception e) {
            // On expose volontairement l'erreur SQL (utile pour la demo)
            model.addAttribute("error", "Erreur SQL : " + e.getMessage());
        }
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
