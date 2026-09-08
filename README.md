# VulnStock — appli de démo de sécurité (SQLi / Command Injection / SSRF)

> ⚠️ **Application volontairement vulnérable, à but pédagogique uniquement.**
> Elle contient des failles de sécurité intentionnelles. **Ne jamais l'exposer sur
> Internet ni sur un réseau de production.** À utiliser en local, en environnement
> isolé, pour des démonstrations et de la formation.

Petite application Spring Boot qui simule un service de consultation de cours de
bourse avec authentification. Elle sert de support de démonstration pour trois
familles de vulnérabilités web classiques.

## Stack

- Java 8, Spring Boot 2.7 (Web + Thymeleaf + JDBC)
- Base **H2 en fichier** (`./data/vulnstock.mv.db`), créée automatiquement au démarrage
- Authentification par session
- Aucune dépendance externe à installer hors Maven

## Démarrage

```bash
mvn spring-boot:run
```

Puis ouvrir http://localhost:8080

**Compte de démonstration :** `admin` / `admin` (également `john` / `password123`).

La base est (re)créée et alimentée à chaque démarrage à partir de `schema.sql` et
`data.sql`. Le fichier de base se trouve dans `./data/`.

## Fonctionnalités

| Page | Chemin | Fonction | Vulnérabilité |
|------|--------|----------|---------------|
| Connexion | `/login` | Authentification | Injection SQL (contournement d'auth) |
| Cours | `/` | Recherche d'une action par trigramme ou nom | Injection SQL (UNION) |
| Diagnostic | `/tools/diagnostics` | Ping d'un hôte | Injection de commande |
| Import distant | `/tools/fetch` | Téléchargement d'une URL | SSRF |
| Console H2 | `/h2-console` | Console SQL (cible interne pour la démo SSRF) | — |

---

## Scénarios d'exploitation (démo)

### 1. Injection SQL — contournement d'authentification
Sur la page de connexion, saisir comme **nom d'utilisateur** :
```
admin' --
```
et n'importe quel mot de passe. La requête devient
`... WHERE username = 'admin' --' AND password = '...'`, le contrôle du mot de
passe est commenté → connexion en tant qu'`admin`.

### 2. Injection SQL — extraction de données (UNION)
Dans le champ de recherche de cours, saisir :
```
' UNION SELECT username, password, role, 0, 'x' FROM users --
```
La liste des comptes et mots de passe s'affiche dans le tableau des « cours ».

### 3. Injection de commande
Sur la page **Diagnostic**, dans le champ hôte :
```
8.8.8.8; cat /etc/passwd
```
La sortie du `ping` est suivie du contenu de `/etc/passwd` (la commande est passée
à `sh -c` sans validation).

### 4. SSRF (Server-Side Request Forgery)
Sur la page **Import distant**, fournir une URL interne que le serveur va aller
chercher à votre place :
```
http://127.0.0.1:8080/h2-console
file:///etc/passwd
http://169.254.169.254/latest/meta-data/    (métadonnées cloud, en environnement cloud)
```

---

## Corrections associées (pour la partie « remédiation » de la démo)

- **SQLi** : utiliser des requêtes paramétrées (`PreparedStatement` /
  `jdbcTemplate.query(sql, args)`), ne jamais concaténer d'entrée utilisateur.
- **Command injection** : éviter le shell ; utiliser une API dédiée
  (`InetAddress.isReachable`) ou une liste blanche stricte + `ProcessBuilder`
  sans `sh -c`.
- **SSRF** : liste blanche de domaines, blocage des adresses privées/loopback et
  du schéma `file:`, résolution DNS contrôlée, désactivation des redirections.
- **Auth** : hachage des mots de passe (bcrypt), Spring Security.

## Licence

Fourni tel quel, à des fins éducatives.
