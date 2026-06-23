# Hansal Verrechnungsprogramm — Setup auf dem Laptop

Diese Anleitung ist für die Installation auf einem Laptop (Windows / Mac / Linux).
Alles läuft lokal über Docker — es muss **kein** Java, Node oder PostgreSQL installiert werden.

Es gibt zwei Fälle:

- **A — Erste Installation** (das Programm läuft noch nicht auf dem Laptop)
- **B — Update** (das Programm läuft schon, es gibt nur neuen Code)

---

## Vorbereitung (einmalig)

1. **Docker Desktop installieren**
   - Download: https://www.docker.com/products/docker-desktop
   - Installieren und **starten**. Warten bis das Docker-Symbol "running" anzeigt.
   - Windows: Beim ersten Start fragt Docker nach WSL 2 → zulassen und PC neu starten.

2. **Prüfen, dass Docker läuft** (Terminal / PowerShell):
   ```bash
   docker --version
   docker compose version
   ```
   Beide Befehle müssen eine Versionsnummer ausgeben.

> **Hinweis:** Auf neuem Docker heißt der Befehl `docker compose` (mit Leerzeichen).
> Auf älterem `docker-compose` (mit Bindestrich). Beide funktionieren gleich.
> In dieser Anleitung steht `docker compose`.

**Benötigte freie Ports:** `80`, `8080`, `5432`

---

## A — Erste Installation

1. **Projektordner auf den Laptop kopieren**
   - Per Git: `git clone <repository-url>`
   - Oder den ganzen Ordner `hansal-verrechnungsprogramm-v3/` per USB / Netzwerk kopieren.

2. **In den Ordner wechseln**
   ```bash
   cd hansal-verrechnungsprogramm-v3
   ```

3. **Starten** (erster Start dauert 5–10 Minuten, weil alles gebaut wird)
   ```bash
   docker compose up -d --build
   ```

4. **Prüfen, dass alles läuft**
   ```bash
   docker compose ps
   ```
   Es müssen 3 Container laufen:
   - `hansal-postgres` (Datenbank)
   - `hansal-backend` (API)
   - `hansal-frontend` (Web-Oberfläche)

5. **Öffnen im Browser**
   - Programm: **http://localhost**
   - Status-Check: http://localhost:8080/actuator/health → soll `{"status":"UP"}` zeigen

✅ Fertig. Die Datenbank ist leer — Produkte, Bestellungen usw. werden in der Oberfläche angelegt.

> Die Container starten nach einem Neustart des Laptops automatisch wieder
> (Einstellung `restart: unless-stopped`). Es muss nichts manuell gestartet werden.

---

## B — Update (neuer Code, vorhandene Daten behalten)

> ⚠️ **Wichtig:** Beim Update **niemals** `docker compose down -v` benutzen —
> das `-v` löscht die Datenbank mit allen Daten!

1. **In den Ordner wechseln**
   ```bash
   cd hansal-verrechnungsprogramm-v3
   ```

2. **(Empfohlen) Datenbank sichern** — siehe Abschnitt "Backup" unten.

3. **Stoppen** (Daten bleiben erhalten)
   ```bash
   docker compose down
   ```

4. **Neuen Code holen**
   ```bash
   git pull origin master
   ```
   *(Oder: die neuen Dateien manuell in den Ordner kopieren / überschreiben.)*

5. **Neu bauen und starten**
   ```bash
   docker compose up -d --build
   ```
   Dabei passiert automatisch:
   - Backend und Frontend werden mit dem neuen Code neu gebaut
   - Neue Datenbank-Änderungen (Liquibase-Migrationen) laufen automatisch
   - Die Daten bleiben erhalten

6. **Prüfen**
   ```bash
   docker compose ps
   ```

### Update in einem Rutsch (Copy & Paste)
```bash
cd hansal-verrechnungsprogramm-v3
docker compose down
git pull origin master
docker compose up -d --build
docker compose ps
```

---

## Tägliche Befehle

| Was | Befehl |
|-----|--------|
| Stoppen (Daten bleiben) | `docker compose down` |
| Wieder starten | `docker compose up -d` |
| Neu starten | `docker compose restart` |
| Status ansehen | `docker compose ps` |
| Logs ansehen (Strg+C zum Beenden) | `docker compose logs -f` |
| Logs nur Backend | `docker compose logs -f backend` |

---

## Backup der Datenbank

**Sichern** (erzeugt eine Datei mit dem heutigen Datum):
```bash
docker compose exec postgres pg_dump -U hansal_user hansal_db > backup_$(date +%Y%m%d).sql
```

**Wiederherstellen** (Dateiname anpassen):
```bash
cat backup_20260623.sql | docker compose exec -T postgres psql -U hansal_user hansal_db
```

> Windows PowerShell: Beim Wiederherstellen `cat` durch `Get-Content` ersetzen:
> `Get-Content backup_20260623.sql | docker compose exec -T postgres psql -U hansal_user hansal_db`

---

## Probleme & Lösungen

**Port 80 oder 8080 ist belegt**
Ein anderes Programm benutzt den Port. In `docker-compose.yml` umstellen, z. B.:
```yaml
  frontend:
    ports:
      - "3000:80"      # dann läuft das Programm auf http://localhost:3000
  backend:
    ports:
      - "8081:8080"
```
Danach `docker compose up -d --build`.

**Windows: Port 80 belegt durch IIS**
```powershell
netstat -ano | findstr :80
iisreset /stop
```
Oder den Frontend-Port wie oben ändern.

**Ein Container startet nicht / Programm lädt nicht**
Logs ansehen, um die Ursache zu finden:
```bash
docker compose logs backend
docker compose logs frontend
docker compose logs postgres
```

**Backend findet die Datenbank nicht (gleich nach dem Start)**
PostgreSQL braucht ein paar Sekunden länger. Backend einmal neu starten:
```bash
docker compose restart backend
```

**Neue Funktionen erscheinen nach Update nicht im Browser**
Browser-Cache leeren: `Strg+Shift+R` (Windows/Linux) bzw. `Cmd+Shift+R` (Mac),
oder ein privates / Inkognito-Fenster öffnen.

**Build schlägt fehl — sauber neu bauen**
```bash
docker compose build --no-cache
docker compose up -d
```

**Liquibase-Migration hängt (selten)**
```bash
docker compose exec postgres psql -U hansal_user -d hansal_db -c "UPDATE databasechangeloglock SET locked=false;"
docker compose restart backend
```

---

## Komplett von vorne anfangen (⚠️ löscht ALLE Daten)

Nur wenn man wirklich alles zurücksetzen will:
```bash
docker compose down -v
docker compose up -d --build
```

---

**Mehr Details:** technische Doku in [README.md](README.md), Kurzanleitung in [QUICKSTART.md](QUICKSTART.md).
