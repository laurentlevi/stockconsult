package com.demo.vulnstock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Controller
public class StockController {

    private final JdbcTemplate jdbc;

    @Autowired
    public StockController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/")
    public String home(@RequestParam(name = "q", required = false) String q,
                       HttpSession session,
                       Model model) {

        model.addAttribute("user", session.getAttribute("user"));
        model.addAttribute("role", session.getAttribute("role"));
        model.addAttribute("query", q);

        if (q != null && !q.trim().isEmpty()) {
            // === VULNERABILITE : INJECTION SQL (recherche) ===
            // Recherche par trigramme (symbol) ou par nom. La valeur saisie est
            // concatenee directement -> UNION SELECT possible pour extraire
            // d'autres tables, ex :
            //   ' UNION SELECT username, password, role, 0, 'x' FROM users --
            String sql = "SELECT symbol, name, sector, price, currency FROM stocks " +
                    "WHERE symbol LIKE '%" + q + "%' OR name LIKE '%" + q + "%'";
            try {
                List<Map<String, Object>> results = jdbc.queryForList(sql);
                model.addAttribute("results", results);
                model.addAttribute("sql", sql);
            } catch (Exception e) {
                model.addAttribute("error", "Erreur SQL : " + e.getMessage());
                model.addAttribute("sql", sql);
            }
        }
        return "home";
    }
}
