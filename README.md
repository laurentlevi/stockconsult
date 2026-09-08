# VulnStock — appli de démo de sécurité (SQLi / Command Injection / SSRF)

*🌍 Langue : **Français** · [English](README.en.md)*

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

## Téléchargements (releases)

Des JAR exécutables prêts à l'emploi sont publiés dans les
[**Releases**](../../releases). Chaque JAR est compilé et validé pour un runtime
Java donné :

| JAR (release) | Runtime | Stack | Branche |
|---------------|---------|-------|---------|
| `stockconsult-java8.jar`  | Java 8+  | Spring Boot 2.7 (`javax`)   | [`main`](../../tree/main)     |
| `stockconsult-java11.jar` | Java 11+ | Spring Boot 2.7 (`javax`)   | [`java11`](../../tree/java11) |
| `stockconsult-java21.jar` | Java 21+ | Spring Boot 3.5 (`jakarta`) | [`java21`](../../tree/java21) |

> Le build Java 21 utilise **Spring Boot 3.5** (namespace `jakarta.*`), car Spring
> Boot 2.7 ne tourne pas proprement sur ce runtime récent. Les builds Java 8/11
> restent en **Spring Boot 2.7** (namespace `javax.*`).
>
> ℹ️ Un build **Java 25** a existé mais a été retiré : l'agent Dynatrace (RAP)
> ne prend pas encore en charge Java 25. Utilisez `java21` comme version la plus
> récente.

Lancer un JAR téléchargé :

```bash
java -jar stockconsult-java21.jar
```

## Docker

Chaque branche fournit un `Dockerfile` multi-stage (build Maven + runtime JRE) et
un `docker-compose.yml`, avec l'image de base adaptée au Java de la branche :

| Branche | Image build | Image runtime |
|---------|-------------|---------------|
| `main`   | `maven:3.9-eclipse-temurin-8`  | `eclipse-temurin:8-jre`  |
| `java11` | `maven:3.9-eclipse-temurin-11` | `eclipse-temurin:11-jre` |
| `java21` | `maven:3.9-eclipse-temurin-21` | `eclipse-temurin:21-jre` |

### Images pré-construites (Docker Hub)

Des images multi-arch (`linux/amd64` + `linux/arm64`) sont publiées sur
[**Docker Hub — `laurentlevi/stockconsult`**](https://hub.docker.com/r/laurentlevi/stockconsult).
Aucun build nécessaire, il suffit de les lancer :

```bash
# latest = Java 21 (Spring Boot 3.5)
docker run --rm -p 8080:8080 laurentlevi/stockconsult:latest
```

Tag par version de Java :

```bash
docker run --rm -p 8080:8080 laurentlevi/stockconsult:java8
docker run --rm -p 8080:8080 laurentlevi/stockconsult:java11
docker run --rm -p 8080:8080 laurentlevi/stockconsult:java21
```

| Tag Docker Hub | Java | Stack |
|----------------|------|-------|
| `latest`, `java21` | 21 | Spring Boot 3.5 (`jakarta`) |
| `java11` | 11 | Spring Boot 2.7 (`javax`)  |
| `java8`  | 8  | Spring Boot 2.7 (`javax`)  |

Persister la base H2 avec un volume :

```bash
docker run --rm -p 8080:8080 -v vulnstock-data:/app/data laurentlevi/stockconsult:latest
```

Puis http://localhost:8080 — login **admin / admin**.

### Construire l'image localement

Construire et lancer depuis les sources (le `Dockerfile` compile le JAR, aucun build local requis) :

```bash
docker build -t stockconsult .
docker run --rm -p 8080:8080 stockconsult
```

Ou via Compose (avec volume pour persister la base H2) :

```bash
docker compose up --build
```

Puis http://localhost:8080 — login **admin / admin**. Le fichier de base H2 est
écrit dans `/app/data` du conteneur (monté sur le volume `vulnstock-data`).

## Kubernetes

Chaque branche fournit des manifests dans [`k8s/`](k8s/) (`Deployment` + `Service`
+ `kustomization.yaml`), pointant vers l'image Docker Hub de la branche
(`main`→`java8`, `java11`, `java21`).

Déployer avec kustomize :

```bash
kubectl apply -k k8s/
kubectl rollout status deploy/stockconsult
```

Accéder à l'appli via un port-forward :

```bash
kubectl port-forward svc/stockconsult 8080:8080
```

Puis http://localhost:8080 — login **admin / admin**.

Changer la version de Java déployée (sans changer de branche) en surchargeant le
tag d'image :

```bash
cd k8s && kustomize edit set image laurentlevi/stockconsult=laurentlevi/stockconsult:java21
```

Le `Service` est en `ClusterIP` (accès par port-forward). Pour une exposition
directe, passer son `type` en `NodePort` ou `LoadBalancer`. Supprimer le
déploiement : `kubectl delete -k k8s/`.

> ⚠️ Rappel : l'appli est **volontairement vulnérable**. Ne la déployez que sur un
> cluster de test isolé, jamais exposée publiquement.

## Démarrage (depuis les sources)

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
