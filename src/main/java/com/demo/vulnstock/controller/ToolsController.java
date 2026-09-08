package com.demo.vulnstock.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Controller
public class ToolsController {

    // ===================================================================
    //  OUTIL 1 : Diagnostic reseau  ->  INJECTION DE COMMANDE
    // ===================================================================
    @GetMapping("/tools/diagnostics")
    public String diagnostics(@RequestParam(name = "host", required = false) String host,
                              HttpSession session,
                              Model model) {
        model.addAttribute("user", session.getAttribute("user"));

        if (host != null && !host.trim().isEmpty()) {
            // === VULNERABILITE : INJECTION DE COMMANDE ===
            // On passe l'entree utilisateur a un shell sans aucune validation.
            // Exemple :  8.8.8.8; cat /etc/passwd
            String command = "ping -c 1 " + host;
            model.addAttribute("command", command);
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                String output = readAll(process.getInputStream());
                process.waitFor();
                model.addAttribute("output", output);
            } catch (Exception e) {
                model.addAttribute("output", "Erreur : " + e.getMessage());
            }
            model.addAttribute("host", host);
        }
        return "diagnostics";
    }

    // ===================================================================
    //  OUTIL 2 : Recuperation d'un flux distant  ->  SSRF
    // ===================================================================
    @GetMapping("/tools/fetch")
    public String fetch(@RequestParam(name = "url", required = false) String url,
                        HttpSession session,
                        Model model) {
        model.addAttribute("user", session.getAttribute("user"));

        if (url != null && !url.trim().isEmpty()) {
            // === VULNERABILITE : SSRF ===
            // Le serveur va chercher n'importe quelle URL fournie par l'utilisateur,
            // sans liste blanche ni filtrage des adresses internes. Exemples :
            //   http://127.0.0.1:8080/h2-console
            //   http://169.254.169.254/latest/meta-data/   (metadata cloud)
            //   file:///etc/passwd
            model.addAttribute("url", url);
            try {
                URL target = new URL(url);
                String body;
                java.net.URLConnection rawConn = target.openConnection();
                if (rawConn instanceof HttpURLConnection) {
                    HttpURLConnection conn = (HttpURLConnection) rawConn;
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.setInstanceFollowRedirects(true);
                    int code = conn.getResponseCode();
                    model.addAttribute("status", code);
                    InputStream in = (code >= 400) ? conn.getErrorStream() : conn.getInputStream();
                    body = (in != null) ? readAll(in) : "(corps vide)";
                } else {
                    // gere aussi file:// , etc.
                    body = readAll(rawConn.getInputStream());
                }
                if (body.length() > 20000) {
                    body = body.substring(0, 20000) + "\n... (tronque)";
                }
                model.addAttribute("body", body);
            } catch (Exception e) {
                model.addAttribute("body", "Erreur : " + e.getMessage());
            }
        }
        return "fetch";
    }

    private String readAll(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
