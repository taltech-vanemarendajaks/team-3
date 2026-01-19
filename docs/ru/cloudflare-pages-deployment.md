# Cloudflare Pages Deployment Guide


## 📋 Table of Contents

  - [📋 Содержание](#содержание)
  - [🎯 Обзор](#обзор)
    - [Основные компоненты:](#основные-компоненты)
  - [📦 Предварительные требования](#предварительные-требования)
  - [🛠 Подготовка проекта](#подготовка-проекта)
    - [1. Установка зависимостей](#1-установка-зависимостей)
    - [2. Добавление скрипта сборки](#2-добавление-скрипта-сборки)
    - [3. Создание `.npmrc`](#3-создание-npmrc)
    - [4. Обновление `next.config.ts`](#4-обновление-nextconfigts)
  - [⚙️ Настройка Cloudflare Pages](#настройка-cloudflare-pages)
    - [Шаг 1: Создание проекта](#шаг-1-создание-проекта)
    - [Шаг 2: Конфигурация сборки](#шаг-2-конфигурация-сборки)
    - [Шаг 3: Compatibility Flags](#шаг-3-compatibility-flags)
    - [Шаг 4: Environment Variables](#шаг-4-environment-variables)
  - [🔐 Конфигурация OAuth](#конфигурация-oauth)
    - [1. Настройка Google OAuth](#1-настройка-google-oauth)
    - [2. Настройка Backend](#2-настройка-backend)
- [URL frontend приложения (БЕЗ trailing slash!)](#url-frontend-приложения-без-trailing-slash)
- [Разрешенные origins для CORS](#разрешенные-origins-для-cors)
  - [📝 Изменения в коде](#изменения-в-коде)
    - [Изменения для Cloudflare Pages](#изменения-для-cloudflare-pages)
      - [1. `frontend/package.json`](#1-frontendpackagejson)
      - [2. `frontend/.npmrc` (новый файл)](#2-frontendnpmrc-новый-файл)
      - [3. `frontend/next.config.ts`](#3-frontendnextconfigts)
      - [4. Добавлен `export const runtime = 'edge'` во все API routes](#4-добавлен-export-const-runtime-edge-во-все-api-routes)
      - [5. Добавлен `export const runtime = 'edge'` в динамические страницы](#5-добавлен-export-const-runtime-edge-в-динамические-страницы)
    - [Сводка измененных файлов](#сводка-измененных-файлов)
  - [🐛 Troubleshooting](#troubleshooting)
    - [Проблема: `cd: can't cd to frontend`](#проблема-cd-cant-cd-to-frontend)
    - [Проблема: `no nodejs_compat compatibility flag set`](#проблема-no-nodejs_compat-compatibility-flag-set)
    - [Проблема: `redirect_uri_mismatch` при OAuth](#проблема-redirect_uri_mismatch-при-oauth)
    - [Проблема: Двойной слэш в URL (`//api/auth/callback`)](#проблема-двойной-слэш-в-url-apiauthcallback)
    - [Проблема: `@cloudflare/next-on-pages` deprecated warning](#проблема-cloudflarenext-on-pages-deprecated-warning)
    - [Проблема: Build завершается, но показывает "Hello world"](#проблема-build-завершается-но-показывает-hello-world)
    - [Проблема: Routes не настроены для Edge Runtime](#проблема-routes-не-настроены-для-edge-runtime)
  - [✅ Чеклист деплоя](#чеклист-деплоя)
  - [📚 Полезные ссылки](#полезные-ссылки)
  - [🔄 Процесс развертывания](#процесс-развертывания)
  - [📊 Структура build output](#структура-build-output)
  - [🎉 Готово!](#готово)

---



Полное руководство по развертыванию Next.js приложения на Cloudflare Pages.

## 📋 Содержание

- [Обзор](#обзор)
- [Предварительные требования](#предварительные-требования)
- [Подготовка проекта](#подготовка-проекта)
- [Настройка Cloudflare Pages](#настройка-cloudflare-pages)
- [Конфигурация OAuth](#конфигурация-oauth)
- [Изменения в коде](#изменения-в-коде)
- [Troubleshooting](#troubleshooting)

---

## 🎯 Обзор

Этот проект использует Next.js 15 с App Router, который был настроен для развертывания на Cloudflare Pages через `@cloudflare/next-on-pages`. Все API routes и динамические страницы настроены для работы в Edge Runtime.

### Основные компоненты:

- ✅ **App Router** (Next.js 15)
- ✅ **Middleware** для аутентификации
- ✅ **SSR** через Server Components
- ✅ **Edge Runtime** для всех API routes
- ✅ **OAuth2** аутентификация через Google

---

## 📦 Предварительные требования

- GitHub репозиторий с вашим проектом
- Аккаунт Cloudflare
- Google OAuth credentials (для OAuth)
- Backend API развернутый и доступный публично

---

## 🛠 Подготовка проекта

### 1. Установка зависимостей

```bash
cd frontend
npm install -D @cloudflare/next-on-pages --legacy-peer-deps
```

> **Примечание**: `--legacy-peer-deps` требуется из-за несовместимости версий Next.js 15.5.7 с `@cloudflare/next-on-pages@1.13.16`. Пакет помечен как deprecated, но работает.

### 2. Добавление скрипта сборки

В `frontend/package.json` добавьте:

```json
{
  "scripts": {
    "pages:build": "npx @cloudflare/next-on-pages"
  }
}
```

### 3. Создание `.npmrc`

Создайте файл `frontend/.npmrc`:

```
legacy-peer-deps=true
```

Это обеспечит использование `legacy-peer-deps` во всех npm операциях.

### 4. Обновление `next.config.ts`

Убедитесь, что `next.config.ts` **не содержит** `output: 'standalone'`:

```typescript
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  // НЕ добавляйте output: 'standalone' - не требуется для Cloudflare Pages
};

export default nextConfig;
```

---

## ⚙️ Настройка Cloudflare Pages

### Шаг 1: Создание проекта

1. Откройте [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Перейдите в **Workers & Pages** → **Pages**
3. Нажмите **"Create application"** → **"Connect to Git"**
4. Выберите ваш GitHub репозиторий

### Шаг 2: Конфигурация сборки

При настройке проекта укажите:

| Параметр | Значение |
|----------|----------|
| **Project name** | `vanemarendaja-borsibaar` (или ваш выбор) |
| **Production branch** | `main` (или `dev`) |
| **Root directory** | `frontend` |
| **Framework preset** | `None` (или `Next.js`, если доступен) |
| **Build command** | `npm install && npm run pages:build` |
| **Build output directory** | `.vercel/output/static` |

### Шаг 3: Compatibility Flags

**Критически важно!** Добавьте compatibility flag:

1. Откройте проект в Cloudflare Pages
2. Перейдите в **Settings** → **Functions** → **Compatibility Flags**
3. Добавьте флаг: **`nodejs_compat`**
4. Убедитесь, что флаг добавлен для **Production** и **Preview** окружений

> ⚠️ **Без этого флага** вы получите ошибку: `no nodejs_compat compatibility flag set`

### Шаг 4: Environment Variables

В разделе **Environment variables** добавьте:

| Переменная | Описание | Пример |
|------------|----------|--------|
| `NEXT_PUBLIC_BACKEND_URL` | Публичный URL вашего backend API | `https://api.yourdomain.com` |

---

## 🔐 Конфигурация OAuth

### 1. Настройка Google OAuth

1. Откройте [Google Cloud Console](https://console.cloud.google.com/)
2. Выберите ваш проект
3. Перейдите в **APIs & Services** → **Credentials**
4. Откройте ваш **OAuth 2.0 Client ID**
5. В разделе **"Authorized redirect URIs"** добавьте:

```
https://ваш-backend-url/login/oauth2/code/google
```

Пример:
```
https://api.yourdomain.com/login/oauth2/code/google
```

### 2. Настройка Backend

Убедитесь, что в backend правильно настроены следующие переменные окружения:

```env
# URL frontend приложения (БЕЗ trailing slash!)
app.frontend.url=https://vanemarendaja-borsibaar.pages.dev

# Разрешенные origins для CORS
APP_CORS_ALLOWED_ORIGINS=https://vanemarendaja-borsibaar.pages.dev,https://www.yourdomain.com
```

> ⚠️ **Важно**: `app.frontend.url` должен быть **без trailing slash**, иначе будут проблемы с redirect URLs.

---

## 📝 Изменения в коде

### Изменения для Cloudflare Pages

#### 1. `frontend/package.json`

Добавлен скрипт сборки:

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

#### 2. `frontend/.npmrc` (новый файл)

```
legacy-peer-deps=true
```

#### 3. `frontend/next.config.ts`

Убран `output: 'standalone'`:

```typescript
const nextConfig: NextConfig = {
  typescript: {
    ignoreBuildErrors: true,
  },
  // output: 'standalone' удален - не требуется для Cloudflare Pages
};
```

#### 4. Добавлен `export const runtime = 'edge'` во все API routes

Все 22 API routes в `frontend/app/api/**/route.ts` теперь содержат:

```typescript
export const runtime = 'edge';
```

Пример:
```typescript
import { NextRequest, NextResponse } from "next/server";
import { backendUrl } from "@/utils/constants";

export const runtime = 'edge'; // ✅ Добавлено

export async function GET(request: NextRequest) {
  // ... код
}
```

#### 5. Добавлен `export const runtime = 'edge'` в динамические страницы

- `frontend/app/(public)/login/page.tsx`
- `frontend/app/(protected)/(sidebar)/pos/[stationId]/page.tsx`

Пример:
```typescript
export const runtime = 'edge'; // ✅ Добавлено
export const dynamic = "force-dynamic";

export default function LoginPage() {
  // ... код
}
```

### Сводка измененных файлов

- ✅ **1 новый файл**: `frontend/.npmrc`
- ✅ **2 конфигурационных файла**: `package.json`, `next.config.ts`
- ✅ **22 API routes**: все `app/api/**/route.ts`
- ✅ **2 страницы**: `login/page.tsx`, `pos/[stationId]/page.tsx`

**Всего**: 27 файлов изменено/создано

---

## 🐛 Troubleshooting

### Проблема: `cd: can't cd to frontend`

**Симптомы**: Build падает с ошибкой `can't cd to frontend`

**Решение**: 
- Убедитесь, что **Root directory** установлен в `frontend`
- **Build command** должен быть: `npm install && npm run pages:build` (без `cd frontend`)

---

### Проблема: `no nodejs_compat compatibility flag set`

**Симптомы**: При открытии страницы видите ошибку о nodejs_compat

**Решение**:
1. Откройте проект в Cloudflare Dashboard
2. **Settings** → **Functions** → **Compatibility Flags**
3. Добавьте флаг: `nodejs_compat`
4. Сохраните и перезапустите build

---

### Проблема: `redirect_uri_mismatch` при OAuth

**Симптомы**: Google OAuth показывает ошибку `Error 400: redirect_uri_mismatch`

**Решение**:
1. Убедитесь, что в Google OAuth Console добавлен правильный redirect URI:
   ```
   https://ваш-backend-url/login/oauth2/code/google
   ```
2. Проверьте, что `app.frontend.url` в backend **без trailing slash**
3. Подождите несколько минут после изменения настроек Google OAuth

---

### Проблема: Двойной слэш в URL (`//api/auth/callback`)

**Симптомы**: 404 ошибка на `/api/auth/callback`, в логах видно `//api`

**Решение**:
- Убедитесь, что `app.frontend.url` в backend **без trailing slash**:
  ```
  ✅ Правильно: https://vanemarendaja-borsibaar.pages.dev
  ❌ Неправильно: https://vanemarendaja-borsibaar.pages.dev/
  ```

---

### Проблема: `@cloudflare/next-on-pages` deprecated warning

**Симптомы**: Видите предупреждение о том, что пакет deprecated

**Решение**:
- Это нормально. Пакет помечен как deprecated, но продолжает работать с Next.js 15.5.7
- В будущем можно мигрировать на `@opennextjs/cloudflare`, но это требует дополнительной настройки

---

### Проблема: Build завершается, но показывает "Hello world"

**Симптомы**: После успешного build видите только "Hello world"

**Решение**:
- Убедитесь, что вы используете **Cloudflare Pages**, а не **Workers**
- URL должен быть `*.pages.dev`, а не `*.workers.dev`
- Убедитесь, что **Deploy command** пустой или содержит `true`, а не `npx wrangler deploy`

---

### Проблема: Routes не настроены для Edge Runtime

**Симптомы**: Build падает с ошибкой о том, что routes не настроены для Edge Runtime

**Решение**:
- Убедитесь, что все API routes содержат `export const runtime = 'edge'`
- Убедитесь, что динамические страницы (`/login`, `/pos/[stationId]`) содержат `export const runtime = 'edge'`

---

## ✅ Чеклист деплоя

Перед развертыванием убедитесь:

- [ ] `@cloudflare/next-on-pages` установлен в `devDependencies`
- [ ] `.npmrc` создан с `legacy-peer-deps=true`
- [ ] `pages:build` скрипт добавлен в `package.json`
- [ ] `output: 'standalone'` убран из `next.config.ts`
- [ ] Все API routes содержат `export const runtime = 'edge'`
- [ ] Динамические страницы содержат `export const runtime = 'edge'`
- [ ] Cloudflare Pages проект создан с правильным Root directory
- [ ] Compatibility flag `nodejs_compat` добавлен
- [ ] Environment variables настроены (`NEXT_PUBLIC_BACKEND_URL`)
- [ ] Google OAuth redirect URI настроен в Google Console
- [ ] Backend `app.frontend.url` настроен без trailing slash
- [ ] CORS настроен в backend для Pages домена

---

## 📚 Полезные ссылки

- [Cloudflare Pages Documentation](https://developers.cloudflare.com/pages/)
- [@cloudflare/next-on-pages GitHub](https://github.com/cloudflare/next-on-pages)
- [Next.js Edge Runtime](https://nextjs.org/docs/app/building-your-application/rendering/edge-and-nodejs-runtimes)
- [Google OAuth Setup](https://console.cloud.google.com/apis/credentials)

---

## 🔄 Процесс развертывания

1. **Git Push**: Изменения коммитятся и пушатся в репозиторий
2. **Automatic Build**: Cloudflare Pages автоматически запускает build при push в production branch
3. **Build Process**:
   - Клонирует репозиторий
   - Устанавливает зависимости
   - Запускает `npm run pages:build`
   - Создает `.vercel/output/static` с Edge Functions и статическими файлами
4. **Automatic Deploy**: Cloudflare Pages автоматически разворачивает результат

---

## 📊 Структура build output

После успешного build создается:

```
.vercel/output/static/
├── _worker.js/           # Edge Functions и Middleware
│   ├── index.js
│   └── nop-build-log.json
├── *.html               # Prerendered pages
├── _next/               # Next.js assets
└── ...
```

---

## 🎉 Готово!

После завершения всех шагов ваше Next.js приложение будет развернуто на Cloudflare Pages и доступно по адресу:

```
https://ваш-проект.pages.dev
```

Все API routes и динамические страницы будут работать через Edge Runtime, обеспечивая быструю и глобальную доступность.

---

**Последнее обновление**: Январь 2026  
**Версия Next.js**: 15.5.7  
**Версия @cloudflare/next-on-pages**: 1.13.16
