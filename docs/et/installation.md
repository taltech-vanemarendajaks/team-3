# Projekti seadistamise juhend

## 📋 Table of Contents

- [Installerimine, Windows](#installerimine-windows)
  - [1. Keskkonnamuutujate seadistamine (.env)](#1-keskkonnamuutujate-seadistamine-env)
  - [2. Google API seadistamine](#2-google-api-seadistamine)
  - [3. JWT saladuse (Secret) genereerimine](#3-jwt-saladuse-secret-genereerimine)
  - [4. Rakenduse käivitamine](#4-rakenduse-käivitamine)

---

## Installerimine, Windows

Selleks, et projekt tööle saada, tuleb esmalt luua keskkonnamuutujate fail. Tee koopia `.sample.env` failist ja nimeta see ümber `.env`.

### 1. Keskkonnamuutujate seadistamine (.env)

Sinu `.env` fail peaks lõpus välja nägema umbes selline, alumised peatükid juhendavad edasi:

```env
POSTGRES_DB=vanemarendaja_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD={Suvaline parool}

SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/vanemarendaja_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD={Suvaline parool}

GOOGLE_CLIENT_ID={Sinu Google Client ID}
GOOGLE_CLIENT_SECRET={Sinu Google Client Secret}

JWT_SECRET={Sinu genereeritud JWT secret}
```

---

### 2. Google API seadistamine

`GOOGLE_CLIENT_ID` ja `GOOGLE_CLIENT_SECRET` hankimiseks järgi neid juhiseid:

Ava [Google Auth Platvorm > Clients](https://console.cloud.google.com/auth/clients)

Esimene kord klikka seal GET STARTED

**"(1) App information"**

- App name = borsibaar
- email = sinu email
- NEXT

**"(2) audience"**

- Vali radiobutton external
- NEXT

**"(3) Contact information"**

- Enda email pane uuesti
- NEXT

**"(4) I AGREE"**

- Pane linnuke ja "Continue"
- Siis klikka "CREATE" ja oota 10 sek.

Hetkel on Sul vasakul menüüs **"Overview"** - selle asemel vali **"Clients"** ja klikka **"+ Create Client"**

- **Application type** all võta esimene "Web application"
- **Name** = Börsibaar
- **Authorized JavaScript origins:**
  - `http://localhost`
  - `http://localhost:3000`
- **Authorized redirect URIs:**
  - `http://localhost:8080/login/oauth2/code/google`
- kliki **"Create"**
- Viskab lahti uue lehe, sealt kopeerimise
  - Client ID (N: 1086996599762-ahv3vpops0qr047obu0h9td14e48morj.apps.googleusercontent.com)
  - Client secret (N: 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)
- vajuta **OK**, sulge

### 3. JWT saladuse (Secret) genereerimine

Kui pole veel NodeJS installeeritud, siis [Lae alla](https://nodejs.org/en/download).

Jooksuta alumine käsk oma terminalis ja kopeeri väljund `.env` faili:

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Vastus (N: WPAi6OYn2VxFhWkwSsYuJIQphtx/GBu6FZP617XgMic=) kopeeri enda .env faili

---

### 4. Rakenduse käivitamine

1. **Backend & Database:**

Kui `.env` on valmis, pane Docker Engine tööle (laadi alla nt [Docker Desktop](https://www.docker.com/products/docker-desktop/) ja AMD64, ca 500MB).

Ava terminal, projekti juurkataloogis käivita (võib võtta minuteid):

```bash
docker compose up
```

Antud käsk tekitab Su Docker Engine sisse uue konteineri (näed seda ja CPU aktiivust).

2.  **Frontend:** Kui Docker on püsti, mine `frontend` kataloogi ja paigalda vajalikud paketid:

Eelmine terminal jäta dockeriga jooksma (näed backend logisi) ja tee uus terminal:

```bash
npm i
```

3.  **Käivitamine:** Käivita frontend rakendus:

```bash
npm run dev
```

Kui ütleb READY, saad tööle aadressilt "http://localhost:3000"

Ava, sulle tuleb "Login with GOOGLE"

Vali TalTech ja pane linnuke, Sinu kasutaja tekitatakse administraatoriks ja saad ringi brausida vasakult: Inventory jne.

Hurraa!
