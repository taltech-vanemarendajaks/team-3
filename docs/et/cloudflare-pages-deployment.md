# Cloudflare Pages'i juurutamise juhend


## 📋 Table of Contents

  - [📋 Sisukord](#sisukord)
  - [🎯 Ülevaade](#ülevaade)
    - [Põhikomponendid:](#põhikomponendid)
  - [📦 Eeltingimused](#eeltingimused)
  - [🛠 Projekti ettevalmistus](#projekti-ettevalmistus)
    - [1. Sõltuvuste paigaldamine](#1-sõltuvuste-paigaldamine)
    - [2. Build skripti lisamine](#2-build-skripti-lisamine)
    - [3. `.npmrc` faili loomine](#3-npmrc-faili-loomine)
    - [4. `next.config.ts` uuendamine](#4-nextconfigts-uuendamine)
  - [⚙️ Cloudflare Pages'i konfigureerimine](#cloudflare-pagesi-konfigureerimine)
    - [Samm 1: Projekti loomine](#samm-1-projekti-loomine)
    - [Samm 2: Build konfigureerimine](#samm-2-build-konfigureerimine)
    - [Samm 3: Ühilduvuslipud](#samm-3-ühilduvuslipud)
    - [Samm 4: Keskkonna muutujad](#samm-4-keskkonna-muutujad)
  - [🔐 OAuth konfigureerimine](#oauth-konfigureerimine)
    - [1. Google OAuth'i seadistamine](#1-google-oauthi-seadistamine)
    - [2. Backend'i konfigureerimine](#2-backendi-konfigureerimine)
- [Frontend rakenduse URL (ILMA lõpuslanguseta!)](#frontend-rakenduse-url-ilma-lõpuslanguseta)
- [CORS'ile lubatud päritolud](#corsile-lubatud-päritolud)
  - [📝 Koodi muudatused](#koodi-muudatused)
    - [Cloudflare Pages'i muudatused](#cloudflare-pagesi-muudatused)
      - [1. `frontend/package.json`](#1-frontendpackagejson)
      - [2. `frontend/.npmrc` (uus fail)](#2-frontendnpmrc-uus-fail)
      - [3. `frontend/next.config.ts`](#3-frontendnextconfigts)
      - [4. Lisatud `export const runtime = 'edge'` kõigile API marsruutidele](#4-lisatud-export-const-runtime-edge-kõigile-api-marsruutidele)
      - [5. Lisatud `export const runtime = 'edge'` dünaamilistele lehtedele](#5-lisatud-export-const-runtime-edge-dünaamilistele-lehtedele)
    - [Muudetud failide kokkuvõte](#muudetud-failide-kokkuvõte)
  - [🐛 Probleemide lahendamine](#probleemide-lahendamine)
    - [Probleem: `cd: can't cd to frontend`](#probleem-cd-cant-cd-to-frontend)
    - [Probleem: `no nodejs_compat compatibility flag set`](#probleem-no-nodejs_compat-compatibility-flag-set)
    - [Probleem: `redirect_uri_mismatch` OAuth'is](#probleem-redirect_uri_mismatch-oauthis)
    - [Probleem: Topeltkaldkriips URL'is (`//api/auth/callback`)](#probleem-topeltkaldkriips-urlis-apiauthcallback)
    - [Probleem: `@cloudflare/next-on-pages` aegunud hoiatus](#probleem-cloudflarenext-on-pages-aegunud-hoiatus)
    - [Probleem: Build õnnestub, kuid näitab "Hello world"](#probleem-build-õnnestub-kuid-näitab-hello-world)
    - [Probleem: Marsruudid pole konfigureeritud Edge Runtime'i jaoks](#probleem-marsruudid-pole-konfigureeritud-edge-runtimei-jaoks)
  - [✅ Juurutamise kontrollnimekiri](#juurutamise-kontrollnimekiri)
  - [📚 Kasulikud lingid](#kasulikud-lingid)
  - [🔄 Juurutamise protsess](#juurutamise-protsess)
  - [📊 Build väljundi struktuur](#build-väljundi-struktuur)
  - [🎉 Valmis!](#valmis)

---



Täielik juhend Next.js rakenduse juurutamiseks Cloudflare Pages'ile.

## 📋 Sisukord

- [Ülevaade](#ülevaade)
- [Eeltingimused](#eeltingimused)
- [Projekti ettevalmistus](#projekti-ettevalmistus)
- [Cloudflare Pages'i konfigureerimine](#cloudflare-pagesi-konfigureerimine)
- [OAuth konfigureerimine](#oauth-konfigureerimine)
- [Koodi muudatused](#koodi-muudatused)
- [Probleemide lahendamine](#probleemide-lahendamine)

---

## 🎯 Ülevaade

See projekt kasutab Next.js 15 App Router'iga, mis on konfigureeritud juurutamiseks Cloudflare Pages'ile läbi `@cloudflare/next-on-pages`. Kõik API marsruudid ja dünaamilised lehed on konfigureeritud töötamiseks Edge Runtime'is.

### Põhikomponendid:

- ✅ **App Router** (Next.js 15)
- ✅ **Middleware** autentimiseks
- ✅ **SSR** Server Components'i kaudu
- ✅ **Edge Runtime** kõigile API marsruutidele
- ✅ **OAuth2** autentimine Google'i kaudu

---

## 📦 Eeltingimused

- GitHub'i repositoorium projekti jaoks
- Cloudflare'i konto
- Google OAuth'i identimisteave (OAuth'i jaoks)
- Backend API juurutatud ja avalikult ligipääsetav

---

## 🛠 Projekti ettevalmistus

### 1. Sõltuvuste paigaldamine

```bash
cd frontend
npm install -D @cloudflare/next-on-pages --legacy-peer-deps
```

> **Märkus**: `--legacy-peer-deps` on vajalik Next.js 15.5.7 ja `@cloudflare/next-on-pages@1.13.16` versioonide ühilduvuse probleemi tõttu. Pakett on märgitud kui aegunud, kuid töötab.

### 2. Build skripti lisamine

Lisa `frontend/package.json` faili:

```json
{
  "scripts": {
    "pages:build": "npx @cloudflare/next-on-pages"
  }
}
```

### 3. `.npmrc` faili loomine

Loo fail `frontend/.npmrc`:

```
legacy-peer-deps=true
```

See tagab `legacy-peer-deps` kasutamise kõigis npm operatsioonides.

### 4. `next.config.ts` uuendamine

Veendu, et `next.config.ts` **EI** sisalda `output: 'standalone'`:

```typescript
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  // ÄRA lisa output: 'standalone' - pole vaja Cloudflare Pages'i jaoks
};

export default nextConfig;
```

---

## ⚙️ Cloudflare Pages'i konfigureerimine

### Samm 1: Projekti loomine

1. Ava [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Mine **Workers & Pages** → **Pages**
3. Kliki **"Create application"** → **"Connect to Git"**
4. Vali oma GitHub'i repositoorium

### Samm 2: Build konfigureerimine

Projekti konfigureerimisel määra:

| Parameeter | Väärtus |
|----------|---------|
| **Project name** | `vanemarendaja-borsibaar` (või sinu valik) |
| **Production branch** | `main` (või `dev`) |
| **Root directory** | `frontend` |
| **Framework preset** | `None` (või `Next.js` kui saadaval) |
| **Build command** | `npm install && npm run pages:build` |
| **Build output directory** | `.vercel/output/static` |

### Samm 3: Ühilduvuslipud

**Kriitiline!** Lisa ühilduvuslipp:

1. Ava projekt Cloudflare Pages'is
2. Mine **Settings** → **Functions** → **Compatibility Flags**
3. Lisa lipp: **`nodejs_compat`**
4. Veendu, et lipp on lisatud nii **Production** kui ka **Preview** keskkondadele

> ⚠️ **Ilma selle liputa** saad vea: `no nodejs_compat compatibility flag set`

### Samm 4: Keskkonna muutujad

Jaotises **Environment variables** lisa:

| Muutuja | Kirjeldus | Näide |
|---------|-----------|-------|
| `NEXT_PUBLIC_BACKEND_URL` | Sinu backend API avalik URL | `https://api.yourdomain.com` |

---

## 🔐 OAuth konfigureerimine

### 1. Google OAuth'i seadistamine

1. Ava [Google Cloud Console](https://console.cloud.google.com/)
2. Vali oma projekt
3. Mine **APIs & Services** → **Credentials**
4. Ava oma **OAuth 2.0 Client ID**
5. Jaotises **"Authorized redirect URIs"** lisa:

```
https://sinu-backend-url/login/oauth2/code/google
```

Näide:
```
https://api.yourdomain.com/login/oauth2/code/google
```

### 2. Backend'i konfigureerimine

Veendu, et backend'is on õigesti konfigureeritud järgmised keskkonna muutujad:

```env
# Frontend rakenduse URL (ILMA lõpuslanguseta!)
app.frontend.url=https://vanemarendaja-borsibaar.pages.dev

# CORS'ile lubatud päritolud
APP_CORS_ALLOWED_ORIGINS=https://vanemarendaja-borsibaar.pages.dev,https://www.yourdomain.com
```

> ⚠️ **Oluline**: `app.frontend.url` peab olema **ilma lõpuslanguseta**, muidu tekivad probleemid redirect URL'idega.

---

## 📝 Koodi muudatused

### Cloudflare Pages'i muudatused

#### 1. `frontend/package.json`

Lisatud build skript:

```json
{
  "scripts": {
    "pages:build": "npx @cloudflare/next-on-pages"
  },
  "devDependencies": {
    "@cloudflare/next-on-pages": "^1.13.16"
  }
}
```

#### 2. `frontend/.npmrc` (uus fail)

```
legacy-peer-deps=true
```

#### 3. `frontend/next.config.ts`

Eemaldatud `output: 'standalone'`:

```typescript
const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  // output: 'standalone' eemaldatud - pole vaja Cloudflare Pages'i jaoks
};
```

#### 4. Lisatud `export const runtime = 'edge'` kõigile API marsruutidele

Kõik 22 API marsruuti `frontend/app/api/**/route.ts` failides nüüd sisaldavad:

```typescript
export const runtime = 'edge';
```

Näide:
```typescript
import { NextRequest, NextResponse } from "next/server";
import { backendUrl } from "@/utils/constants";

export const runtime = 'edge'; // ✅ Lisatud

export async function GET(request: NextRequest) {
  // ... kood
}
```

#### 5. Lisatud `export const runtime = 'edge'` dünaamilistele lehtedele

- `frontend/app/(public)/login/page.tsx`
- `frontend/app/(protected)/(sidebar)/pos/[stationId]/page.tsx`

Näide:
```typescript
export const runtime = 'edge'; // ✅ Lisatud
export const dynamic = "force-dynamic";

export default function LoginPage() {
  // ... kood
}
```

### Muudetud failide kokkuvõte

- ✅ **1 uus fail**: `frontend/.npmrc`
- ✅ **2 konfiguratsioonifaili**: `package.json`, `next.config.ts`
- ✅ **22 API marsruuti**: kõik `app/api/**/route.ts`
- ✅ **2 lehte**: `login/page.tsx`, `pos/[stationId]/page.tsx`

**Kokku**: 27 faili muudetud/loonud

---

## 🐛 Probleemide lahendamine

### Probleem: `cd: can't cd to frontend`

**Sümptomid**: Build ebaõnnestub veaga `can't cd to frontend`

**Lahendus**: 
- Veendu, et **Root directory** on seatud väärtusele `frontend`
- **Build command** peaks olema: `npm install && npm run pages:build` (ilma `cd frontend`)

---

### Probleem: `no nodejs_compat compatibility flag set`

**Sümptomid**: Lehe avamisel näed viga nodejs_compat'i kohta

**Lahendus**:
1. Ava projekt Cloudflare Dashboard'is
2. **Settings** → **Functions** → **Compatibility Flags**
3. Lisa lipp: `nodejs_compat`
4. Salvesta ja taaskäivita build

---

### Probleem: `redirect_uri_mismatch` OAuth'is

**Sümptomid**: Google OAuth näitab viga `Error 400: redirect_uri_mismatch`

**Lahendus**:
1. Veendu, et Google OAuth Console'is on lisatud õige redirect URI:
   ```
   https://sinu-backend-url/login/oauth2/code/google
   ```
2. Kontrolli, et backend'is `app.frontend.url` on **ilma lõpuslanguseta**
3. Oota mõned minutid pärast Google OAuth'i seadete muutmist

---

### Probleem: Topeltkaldkriips URL'is (`//api/auth/callback`)

**Sümptomid**: 404 viga `/api/auth/callback`'il, logid näitavad `//api`

**Lahendus**:
- Veendu, et backend'is `app.frontend.url` on **ilma lõpuslanguseta**:
  ```
  ✅ Õige: https://vanemarendaja-borsibaar.pages.dev
  ❌ Vale: https://vanemarendaja-borsibaar.pages.dev/
  ```

---

### Probleem: `@cloudflare/next-on-pages` aegunud hoiatus

**Sümptomid**: Näed hoiust, et pakett on aegunud

**Lahendus**:
- See on normaalne. Pakett on märgitud kui aegunud, kuid jätkab tööd Next.js 15.5.7'ga
- Tulevikus saab migreerida `@opennextjs/cloudflare`'ile, kuid see nõuab täiendavat seadistamist

---

### Probleem: Build õnnestub, kuid näitab "Hello world"

**Sümptomid**: Pärast edukat build'i näed ainult "Hello world"

**Lahendus**:
- Veendu, et kasutad **Cloudflare Pages'it**, mitte **Workers'it**
- URL peaks olema `*.pages.dev`, mitte `*.workers.dev`
- Veendu, et **Deploy command** on tühi või sisaldab `true`, mitte `npx wrangler deploy`

---

### Probleem: Marsruudid pole konfigureeritud Edge Runtime'i jaoks

**Sümptomid**: Build ebaõnnestub veaga, et marsruudid pole konfigureeritud Edge Runtime'i jaoks

**Lahendus**:
- Veendu, et kõik API marsruudid sisaldavad `export const runtime = 'edge'`
- Veendu, et dünaamilised lehed (`/login`, `/pos/[stationId]`) sisaldavad `export const runtime = 'edge'`

---

## ✅ Juurutamise kontrollnimekiri

Enne juurutamist veendu:

- [ ] `@cloudflare/next-on-pages` paigaldatud `devDependencies`'is
- [ ] `.npmrc` loodud `legacy-peer-deps=true`'ga
- [ ] `pages:build` skript lisatud `package.json`'i
- [ ] `output: 'standalone'` eemaldatud `next.config.ts`'ist
- [ ] Kõik API marsruudid sisaldavad `export const runtime = 'edge'`
- [ ] Dünaamilised lehed sisaldavad `export const runtime = 'edge'`
- [ ] Cloudflare Pages'i projekt loodud õige Root directory'ga
- [ ] Ühilduvuslipp `nodejs_compat` lisatud
- [ ] Keskkonna muutujad konfigureeritud (`NEXT_PUBLIC_BACKEND_URL`)
- [ ] Google OAuth redirect URI konfigureeritud Google Console'is
- [ ] Backend'i `app.frontend.url` konfigureeritud ilma lõpuslanguseta
- [ ] CORS konfigureeritud backend'is Pages'i domeeni jaoks

---

## 📚 Kasulikud lingid

- [Cloudflare Pages'i dokumentatsioon](https://developers.cloudflare.com/pages/)
- [@cloudflare/next-on-pages GitHub](https://github.com/cloudflare/next-on-pages)
- [Next.js Edge Runtime](https://nextjs.org/docs/app/building-your-application/rendering/edge-and-nodejs-runtimes)
- [Google OAuth'i seadistamine](https://console.cloud.google.com/apis/credentials)

---

## 🔄 Juurutamise protsess

1. **Git Push**: Muudatused kinnitatakse ja tõstetakse repositooriumi
2. **Automaatne Build**: Cloudflare Pages käivitab automaatselt build'i production branch'i push'imisel
3. **Build protsess**:
   - Kloonib repositooriumi
   - Paigaldab sõltuvused
   - Käivitab `npm run pages:build`
   - Loob `.vercel/output/static` Edge Functions'ide ja staatiliste failidega
4. **Automaatne Deploy**: Cloudflare Pages juurutab automaatselt tulemuse

---

## 📊 Build väljundi struktuur

Pärast edukat build'i loob:

```
.vercel/output/static/
├── _worker.js/           # Edge Functions ja Middleware
│   ├── index.js
│   └── nop-build-log.json
├── *.html               # Eelrenderdatud lehed
├── _next/               # Next.js ressursid
└── ...
```

---

## 🎉 Valmis!

Pärast kõigi sammude läbimist on sinu Next.js rakendus juurutatud Cloudflare Pages'ile ja ligipääsetav aadressil:

```
https://sinu-projekt.pages.dev
```

Kõik API marsruudid ja dünaamilised lehed töötavad Edge Runtime'i kaudu, tagades kiire ja globaalse ligipääsetavuse.

---

**Viimati uuendatud**: Jaanuar 2026  
**Next.js versioon**: 15.5.7  
**@cloudflare/next-on-pages versioon**: 1.13.16
