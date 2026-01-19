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

Sinu `.env` fail peaks välja nägema umbes selline:

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

- **ID hankimine:** [Google Developer Console](https://developers.google.com/identity/gsi/web/guides/get-google-api-clientid#get_your_google_api_client_id)
- **Auth Platform:** Kui lood uue, vali **External**, mitte Internal.
- **Seadistamine:** Kui Client ID on loodud, kliki selle peale ja täida järgmised väljad:
  - **Authorized JavaScript origins:**
    - `http://localhost`
    - `http://localhost:3000` (või oma port, kui see on erinev)
  - **Authorized redirect URIs:**
    - `http://localhost:8080/login/oauth2/code/google`
- **Secret:** `GOOGLE_CLIENT_SECRET` asub samas kohas, kus Client ID.

---

### 3. JWT saladuse (Secret) genereerimine

`JWT_SECRET` jaoks on sul vaja Node.js-i. Jooksuta see käsk oma terminalis ja kopeeri väljund `.env` faili:

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

---

### 4. Rakenduse käivitamine

1.  **Backend & Database:** Kui `.env` on valmis, pane Docker Engine tööle (nt Docker Desktop). Projekti juurkataloogis käivita:

    ```bash
    docker compose up
    ```

2.  **Frontend:** Kui Docker on püsti, mine `frontend` kataloogi ja paigalda vajalikud paketid:

    ```bash
    npm i
    ```

3.  **Käivitamine:** Käivita frontend rakendus:
    ```bash
    npm run dev
    ```

Kui kõik sujus, kuvatakse konsoolis teade:
`Local: http://localhost:3000`

Hoia peal **CTRL** ja tee hiirega **vasakklikk** sellel lingil, et avada frontend brauseris.
