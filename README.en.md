# VulnStock — security demo app (SQLi / Command Injection / SSRF)

*🌍 Language: **English** · [Français](README.md)*

> ⚠️ **Deliberately vulnerable application, for educational purposes only.**
> It contains intentional security flaws. **Never expose it to the Internet or to
> a production network.** Use it locally, in an isolated environment, for demos
> and training.

A small Spring Boot application that simulates a stock-quote lookup service with
authentication. It serves as a demonstration playground for three classic
families of web vulnerabilities.

## Stack

- Java 11, Spring Boot 2.7 (Web + Thymeleaf + JDBC)
- **File-based H2 database** (`./data/vulnstock.mv.db`), created automatically at startup
- Session-based authentication
- No external dependency to install other than Maven

## Downloads (releases)

Ready-to-run executable JARs are published in the
[**Releases**](../../releases). Each JAR is compiled and validated for a given
Java runtime:

| JAR (release) | Runtime | Stack | Branch |
|---------------|---------|-------|--------|
| `stockconsult-java8.jar`  | Java 8+  | Spring Boot 2.7 (`javax`)   | [`main`](../../tree/main)     |
| `stockconsult-java11.jar` | Java 11+ | Spring Boot 2.7 (`javax`)   | [`java11`](../../tree/java11) |
| `stockconsult-java21.jar` | Java 21+ | Spring Boot 3.5 (`jakarta`) | [`java21`](../../tree/java21) |

> The Java 21 build uses **Spring Boot 3.5** (`jakarta.*` namespace), because
> Spring Boot 2.7 does not run cleanly on that recent runtime. The Java 8/11
> builds stay on **Spring Boot 2.7** (`javax.*` namespace).
>
> ℹ️ A **Java 25** build existed but was removed: the Dynatrace agent (RAP) does
> not support Java 25 yet. Use `java21` as the most recent version.

Run a downloaded JAR:

```bash
java -jar stockconsult-java21.jar
```

## Docker

Each branch provides a multi-stage `Dockerfile` (Maven build + JRE runtime) and a
`docker-compose.yml`, with the base image matching the branch's Java version:

| Branch | Build image | Runtime image |
|--------|-------------|---------------|
| `main`   | `maven:3.9-eclipse-temurin-8`  | `eclipse-temurin:8-jre`  |
| `java11` | `maven:3.9-eclipse-temurin-11` | `eclipse-temurin:11-jre` |
| `java21` | `maven:3.9-eclipse-temurin-21` | `eclipse-temurin:21-jre` |

### Pre-built images (Docker Hub)

Multi-arch images (`linux/amd64` + `linux/arm64`) are published on
[**Docker Hub — `laurentlevi/stockconsult`**](https://hub.docker.com/r/laurentlevi/stockconsult).
No build needed, just run them:

```bash
# latest = Java 21 (Spring Boot 3.5)
docker run --rm -p 8080:8080 laurentlevi/stockconsult:latest
```

Tag per Java version:

```bash
docker run --rm -p 8080:8080 laurentlevi/stockconsult:java8
docker run --rm -p 8080:8080 laurentlevi/stockconsult:java11
docker run --rm -p 8080:8080 laurentlevi/stockconsult:java21
```

| Docker Hub tag | Java | Stack |
|----------------|------|-------|
| `latest`, `java21` | 21 | Spring Boot 3.5 (`jakarta`) |
| `java11` | 11 | Spring Boot 2.7 (`javax`)  |
| `java8`  | 8  | Spring Boot 2.7 (`javax`)  |

Persist the H2 database with a volume:

```bash
docker run --rm -p 8080:8080 -v vulnstock-data:/app/data laurentlevi/stockconsult:latest
```

Then http://localhost:8080 — login **admin / admin**.

### Build the image locally

Build and run from source (the `Dockerfile` compiles the JAR, no local build required):

```bash
docker build -t stockconsult .
docker run --rm -p 8080:8080 stockconsult
```

Or via Compose (with a volume to persist the H2 database):

```bash
docker compose up --build
```

Then http://localhost:8080 — login **admin / admin**. The H2 database file is
written to `/app/data` in the container (mounted on the `vulnstock-data` volume).

## Kubernetes

Each branch provides manifests in [`k8s/`](k8s/) (`Deployment` + `Service` +
`kustomization.yaml`), pointing to the branch's Docker Hub image
(`main`→`java8`, `java11`, `java21`).

Deploy with kustomize:

```bash
kubectl apply -k k8s/
kubectl rollout status deploy/stockconsult
```

Access the app via a port-forward:

```bash
kubectl port-forward svc/stockconsult 8080:8080
```

Then http://localhost:8080 — login **admin / admin**.

Change the deployed Java version (without switching branches) by overriding the
image tag:

```bash
cd k8s && kustomize edit set image laurentlevi/stockconsult=laurentlevi/stockconsult:java21
```

The `Service` is `ClusterIP` (accessed via port-forward). For direct exposure,
change its `type` to `NodePort` or `LoadBalancer`. Delete the deployment:
`kubectl delete -k k8s/`.

> ⚠️ Reminder: the app is **deliberately vulnerable**. Only deploy it on an
> isolated test cluster, never exposed publicly.

## Getting started (from source)

```bash
mvn spring-boot:run
```

Then open http://localhost:8080

**Demo account:** `admin` / `admin` (also `john` / `password123`).

The database is (re)created and seeded at each startup from `schema.sql` and
`data.sql`. The database file lives in `./data/`.

## Features

| Page | Path | Function | Vulnerability |
|------|------|----------|---------------|
| Login | `/login` | Authentication | SQL injection (auth bypass) |
| Quotes | `/` | Look up a stock by ticker or name | SQL injection (UNION) |
| Diagnostics | `/tools/diagnostics` | Ping a host | Command injection |
| Remote import | `/tools/fetch` | Fetch a URL | SSRF |
| H2 console | `/h2-console` | SQL console (internal target for the SSRF demo) | — |

---

## Exploitation scenarios (demo)

### 1. SQL injection — authentication bypass
On the login page, enter as the **username**:
```
admin' --
```
and any password. The query becomes
`... WHERE username = 'admin' --' AND password = '...'`, the password check is
commented out → logged in as `admin`.

### 2. SQL injection — data extraction (UNION)
In the quote search field, enter:
```
' UNION SELECT username, password, role, 0, 'x' FROM users --
```
The list of accounts and passwords is shown in the "quotes" table.

### 3. Command injection
On the **Diagnostics** page, in the host field:
```
8.8.8.8; cat /etc/passwd
```
The `ping` output is followed by the contents of `/etc/passwd` (the command is
passed to `sh -c` without validation).

### 4. SSRF (Server-Side Request Forgery)
On the **Remote import** page, provide an internal URL that the server will fetch
on your behalf:
```
http://127.0.0.1:8080/h2-console
file:///etc/passwd
http://169.254.169.254/latest/meta-data/    (cloud metadata, in a cloud environment)
```

---

## Related fixes (for the "remediation" part of the demo)

- **SQLi**: use parameterized queries (`PreparedStatement` /
  `jdbcTemplate.query(sql, args)`), never concatenate user input.
- **Command injection**: avoid the shell; use a dedicated API
  (`InetAddress.isReachable`) or a strict allow-list + `ProcessBuilder` without
  `sh -c`.
- **SSRF**: domain allow-list, block private/loopback addresses and the `file:`
  scheme, controlled DNS resolution, disable redirects.
- **Auth**: hash passwords (bcrypt), Spring Security.

## License

Provided as-is, for educational purposes.
