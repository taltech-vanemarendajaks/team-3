# Borsibaari rakenduse äriloogika

## Üldkirjeldus
Lao-/inventari haldamise süsteem baaridele/restoranidele mitme rentniku (multi-tenant) arhitektuuriga. Iga organisatsioon haldab oma tooteid, inventari, kategooriaid, kasutajaid ja baarijaamu.

---

## Põhilised domeeniüksused

### 1. Organization (Organisatsioon)
- Mitme rentniku arhitektuuri juurüksus
- Dünaamilise hinnastamise parameetrid: `priceIncreaseStep`, `priceDecreaseStep`
- Seaded rakenduvad kõigile organisatsiooni toodetele

### 2. User (Kasutaja)
- Autentimine OAuth2 kaudu (Google)
- Rollid: `USER`, `ADMIN`
- Seotus ühe organisatsiooniga (`organizationId`)
- Many-to-many seos `BarStation`-iga (ligipääs jaamadele)

### 3. Category (Kategooria)
- Toodete grupeerimine
- Lipp `dynamicPricing` (dünaamilise hinnastamise lubamine)

### 4. Product (Toode)
- Toodete kataloog
- Hinnad: `basePrice`, `minPrice`, `maxPrice`
- Lipp `isActive` (soft delete)
- Seos kategooria ja organisatsiooniga

### 5. Inventory (Inventar)
- Tootepõhised laoseisud
- `quantity` (mittenegatiivne väärtus)
- `adjustedPrice` (hetkehind koos dünaamika arvestusega)

### 6. InventoryTransaction (Inventari tehing)
- Laoseisu ja hinna muutuste audit
- Tüübid: `SALE`, `PURCHASE`, `ADJUSTMENT`, `RETURN`, `TRANSFER_IN`, `TRANSFER_OUT`, `INITIAL`
- Salvestab: `quantityBefore`, `quantityAfter`, `priceBefore`, `priceAfter`, `createdBy`, `barStationId`, `referenceId`

### 7. BarStation (Baarijaam)
- Müügipunktid (POS)
- Many-to-many seos kasutajatega
- Kasutatakse müügi jälgimiseks jaamade kaupa

---

## Põhilised äriprotsessid

### 1. Autentimine ja autoriseerimine
- OAuth2 Google kaudu
- JWT tokenid HTTP-only küpsistes
- Organisatsiooni kontroll igal päringul
- Onboarding: organisatsiooni esimene kasutaja saab rolli `ADMIN`

### 2. Toodete haldus
- **Loomine**: automaatselt luuakse inventari kirje väärtusega `quantity = 0` ja tehing `INITIAL`
- **Kustutamine**: soft delete (`isActive = false`)
- **Valideerimine**: nime unikaalsus organisatsiooni piires (case-insensitive)

### 3. Inventari haldus

**Laoseisu lisamine (`PURCHASE`)**:
- Suurendab `quantity`
- Loob tehingu tüüpi `PURCHASE`
- Hind ei muutu

**Laoseisu eemaldamine (`ADJUSTMENT`)**:
- Vähendab `quantity` (kontroll mittenegatiivsuse jaoks)
- Loob tehingu tüüpi `ADJUSTMENT` negatiivse `quantityChange` väärtusega
- Võimalik määrata `referenceId` ja `notes`

**Laoseisu korrigeerimine (`ADJUSTMENT`)**:
- Seab uue `quantity` väärtuse
- Loob tehingu muutuse vahega

### 4. Dünaamiline hinnastamine

**Müügi korral (`SalesService.processSaleItem`)**:
- Kui kategoorial on `dynamicPricing = true`:
  - Hind suureneb `organization.priceIncreaseStep` võrra
  - Piirang: mitte üle `product.maxPrice`
  - Uus hind salvestatakse `inventory.adjustedPrice` väljale
- Kui `dynamicPricing = false`: hind ei muutu

**Automaatne hinnalangetus (`PriceCorrectionJob`)**:
- Käivitub iga minuti järel (`@Scheduled(cron = "0 * * * * *")`)
- Leiab tooted, millel pole viimase minuti jooksul müüke
- Vähendab hinda `organization.priceDecreaseStep` võrra
- Piirang: mitte alla `product.minPrice`
- Loob tehingu tüüpi `ADJUSTMENT` hinnamuutusega (kogus ei muutu)

### 5. Müük (POS)

**Müügi protsess (`SalesService.processSale`)**:
1. Genereeritakse unikaalne `saleId` (vorming: `SALE-{timestamp}`)
2. Iga toote puhul:
   - Laoseisu piisavuse kontroll
   - Toote aktiivsuse kontroll
   - Organisatsiooni kuuluvuse kontroll
   - Hinnaarvutus (koos dünaamilise hinnastamisega)
   - Laoseisu vähendamine
   - Hinna uuendamine (kui dünaamiline hinnastamine on lubatud)
   - Tehingu `SALE` loomine negatiivse `quantityChange` väärtusega
   - Seostamine `barStationId` ja `userId` väärtustega
3. Tagastab: müüdud toodete nimekiri, kogusumma, `saleId`

**Valideerimised**:
- Ebapiisav laoseis → viga
- Toode pole aktiivne → viga
- Toode ei kuulu organisatsioonile → viga 403

### 6. Baarijaamade haldus

**Loomine/uuendamine**:
- Ainult `ADMIN` jaoks
- Kasutajate määramine (many-to-many)
- Nime unikaalsuse kontroll organisatsiooni piires

**Ligipääs**:
- `ADMIN`: näeb kõiki organisatsiooni jaamu
- `USER`: näeb ainult talle määratud jaamu
- Kui `USER`-il on üks jaam → automaatne suunamine POS-i

### 7. Analüütika ja aruandlus

**Kasutajapõhine statistika (`getUserSalesStats`)**:
- Grupeerimine `userId` + `barStationId` järgi
- Unikaalsete müükide loendus (`referenceId`)
- Käibe arvutus (kasutades `basePrice`)

**Jaamapõhine statistika (`getStationSalesStats`)**:
- Grupeerimine `barStationId` järgi
- Unikaalsete müükide loendus
- Kogukäibe arvutus

**Kuvamine Dashboardil**:
- Jaamade edetabel
- Kasutajate müügitulemused
- Organisatsiooni seaded (ainult `ADMIN`)

### 8. Tehingute ajalugu

**Ajaloo vaatamine (`getTransactionHistory`)**:
- Kõik toote tehingud
- Sortimine kuupäeva järgi (uusimad ees)
- Kuvatakse: tüüp, koguse muutus, hinnad enne/pärast, kasutaja, kuupäev, `referenceId`, `notes`

---

## Ärireeglid ja piirangud

### 1. Multi-tenant
- Kõik operatsioonid on isoleeritud `organizationId` järgi
- Kuuluvuse kontroll enne igat operatsiooni

### 2. Laoseis
- Ei tohi olla negatiivne (CHECK constraint + koodipoolne valideerimine)
- Müügi korral kontrollitakse laoseisu piisavust

### 3. Hinnastamine
- Dünaamiline hinnastamine töötab ainult kategooriatele, millel `dynamicPricing = true`
- Hinnad on piiratud `minPrice` ja `maxPrice` vahemikku
- Automaatne hinnalangetus ainult toodetele, millel pole müüke

### 4. Tooted
- Nime unikaalsus organisatsiooni piires (case-insensitive)
- Soft delete (`isActive = false`)
- Loomisel luuakse automaatselt null-laoseisuga inventari kirje

### 5. Kasutajad
- Üks kasutaja = üks organisatsioon
- Organisatsiooni esimene kasutaja saab automaatselt rolli `ADMIN`
- Kasutajaid saab määrata mitmele baarijaamale

### 6. Tehingud
- Muutumatud (immutable)
- Salvestavad alati oleku enne ja pärast muutust
- Seos kasutajaga (`createdBy`) ja jaamaga (`barStationId`)

---

## Kasutajastsenaariumid

### 1. Administraator
- Organisatsiooni haldus (hinnastamise sammude seadistamine)
- Kategooriate ja toodete haldus
- Baarijaamade ja kasutajate haldus
- Müügianalüütika vaatamine

### 2. Kasutaja (baarmen)
- Ligipääs määratud baarijaamadele
- Toodete müük POS-i kaudu
- Laoseisu ja hindade vaatamine

### 3. Inventari haldur
- Laoseisu haldus (lisamine, eemaldamine, korrigeerimine)
- Tehingute ajaloo vaatamine
- Uute toodete ja kategooriate loomine

---

## Tehnilised eripärad

- **Tehingulisus**: kõik inventariga seotud operatsioonid toimuvad `@Transactional` all
- **Audit**: kõik muutused fikseeritakse `InventoryTransaction` kirjetena
- **Jõudlus**: statistika jaoks kasutatakse grupeerimist; seotud üksused on tihti eager-loaditud
- **Turvalisus**: ligipääsukontroll toimub teenuste ja kontrollerite tasemel

---

See kirjeldus kajastab rakenduse praegust äriloogikat, tuginedes koodibaasi analüüsile.

