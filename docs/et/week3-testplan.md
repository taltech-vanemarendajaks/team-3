# Testiplaan: Borsibaar Projekt

**Projekt:** Borsibaar Rakendus
**Tiim:** Grupp 3
**Kuupäev:** 03.02.2026
**Versioon:** 2.0 (Lõplik)

## 1. Testimise eesmärgid (Testing Objectives)

Testimise peamine eesmärk on tagada "Borsibaar" rakenduse töökindlus läbi automatiseeritud _End-to-End_ (E2E) testide, kasutades uudseid AI-põhiseid töövooge ja Playwrighti edasijõudnud lahendusi.

- **Äriloogika kontroll:** Veenduda, et kasutajad saavad teha tehinguid (osta/müüa) ja näevad portfelli seisu korrektselt.
- **Tehnilise arhitektuuri valideerimine:** Tõestada, et spetsiaalne API-põhine sisselogimise _bypass_ ja abifunktsioonid (_helpers_) töötavad stabiilselt.
- **AI töövoo demonstreerimine:** Näidata Playwright AI agentide (_Planner, Generator, Healer_) efektiivsust testide loomise kiirendamisel.

## 2. Testimise tasemed (Testing Levels)

- **Süsteemitestimine (System Testing):** Automatiseeritud E2E testid _Black-box_ meetodil Playwrighti raamistikus.
- **Vastuvõtutestimine (Acceptance Testing):** Kontrollimine, kas rakendus vastab funktsionaalsetele nõuetele lõppkasutaja vaatest.
- _(Märkus: Ühiktestimine (Unit testing) ja Valge kasti (White-box) testimine on kaetud arendajate poolt koodi kirjutamise ja Pull Requestide ülevaatuse käigus)._

## 3. Testimise ulatus (Test Scope)

### Testitav osa (In Scope)

1.  **Autentimise voog:** Google'i UI vahelejätmine (_bypass_) läbi API, et tagada kiire ja stabiilne sisselogitud olek.
2.  **Põhifunktsionaalsus:**
    - Aktsiate ostmine (kontrollitakse kasutajaliidese tagasisidet ja portfelli uuemist).
    - Aktsiate müümine.
    - Portfelli ja Edetabeli (_Leaderboard_) kuvamine.
3.  **AI genereeritud stsenaariumid:** Testjuhtumid, mis on loodud `playwright-test-planner` agendi poolt baasseisu (_seed_) põhjal.

### Mitte-testitav osa (Out of Scope)

- Google'i väline sisselogimise kasutajaliides (see on _bypass_'itud).
- Mitte-funktsionaalne testimine (Koormustestid, Stresstestid).
- Visuaalne regressioon (piksel-täpsus), v.a juhul kui see on kaetud AI _snapshot_'idega.

## 4. Testimisstrateegia ja lähenemine (Test Approach)

Projekt kasutab **Hübriidset AI-juhitud automatiseerimise strateegiat**.

### 4.1. AI-avustatud töövoog (AI-Assisted Workflow)

Me kasutame VS Code'is **Playwright AI Agente** testide loomise automatiseerimiseks:

1.  **Seadistamine (`seed.spec.ts`):** Anname AI-le ette baasfaili, mis tegeleb navigeerimise ja autentimise olekuga.
2.  **Planeerimine (`playwright-test-planner`):** Agent analüüsib rakenduse olekut ja genereerib testiplaan (`test.plan.md`).
3.  **Genereerimine (`playwright-test-generator`):** Plaani põhjal kirjutab agent käivitatava Playwrighti koodi.
4.  **Parandamine (`playwright-test-healer`):** Kui testid ebaõnnestuvad selektorite muutumise tõttu, analüüsib _Healer_ agent viga ja pakub parandusi.
5.  **Manuaalne viimistlus:** Tauri vaatab AI koodi üle, optimeerib loetavust ja lisab kommentaarid.

### 4.2. Tehniline arhitektuur (Fixtures & Helpers)

Koodi puhtuse ja korduste vältimiseks kasutame kohandatud raamistiku struktuuri:

- **API Abifunktsioonid (`helpers/api.ts`):** Eraldi kiht serveriga suhtlemiseks (nt `getProduct`, `bypassLogin`). See võimaldab testandmeid luua ilma aeglast UI-d kasutamata.
- **Kohandatud Fixtures (`helpers/fixtures.ts`):** Laiendame Playwrighti baasobjekti `test`. See süstib (_injects_) meie API helperid automaatselt igasse testi, kaotades vajaduse manuaalsete importide järele igas failis.
- **Auth Bypass:** Google'i sisselogimise UI automatiseerimise asemel pärime sessiooni läbi turvatud API otspunktii (kasutades `process.env.TEST_SECRET`) ja süstime saadud `storageState` otse brauseri konteksti.

## 5. Testimiskeskkond (Test Environment)

- **Tööriistad:** Playwright (TypeScript), VS Code AI Agents.
- **Brauserid:** Google Chrome / Chromium (Headless & Headed režiimid).
- **Konfiguratsioon:**
  - `Borsibaar .env`: Peab sisaldama korrektseid API saladusi (_secrets_).
  - `Playwright .env`: Peab klappima _backend_-i saladustega, et võimaldada sisselogimise _bypass_.
  - Teste saab jooksutada nii _Localhost_ kui _Staging_ keskkonnas.

## 6. Sisenemis- ja väljumiskriteeriumid (Entry & Exit Criteria)

### Sisenemiskriteeriumid (Entry - millal alustame)

- `bypass-google-login` API otspunkt töötab ja vastab päringutele.
- Playwright agendid (`init-agents`) on installitud ja konfigureeritud (`--loop=vscode`).
- `seed.spec.ts` fail on loodud ja kontrollitud (suudab luua sessiooni).

### Väljumiskriteeriumid (Exit - millal loeme valmis)

- AI loodud testiplaan (`test.plan.md`) on edukalt konverteeritud töötavaks koodiks.
- Kõik kriitilise teekonna testid (Ost/Müük/Auth) on läbinud ("Passed").
- On genereeritud Playwrighti HTML raport, mis näitab 100% õnnestumist valitud testide osas.

## 7. Rollid ja vastutused (Roles and Responsibilities)

| Roll                         | Liige     | Vastutusala                                                                                                            |
| :--------------------------- | :-------- | :--------------------------------------------------------------------------------------------------------------------- |
| **QA Automaatika Juht**      | **Tauri** | AI genereeritud koodi viimistlemine, koodi stiil ja korrastamine (_refactoring_), manuaalne käivitamine ja lõppraport. |
| **Tehniline Testimise Juht** | **Tormi** | Auth Bypass loogika arendus, AI agentide seadistamine, API Helpers & Fixtures arhitektuuri loomine.                    |
| **Arendaja**                 | Anton     | Vigade parandus, uute funktsioonide arendus, koodi ülevaatus (_Code Review_).                                          |
| **Arendaja**                 | Artjom    | Valge kasti testimine (koodi struktuur), _Pull Request_'ide ülevaatus, arendus.                                        |
| **Arendaja**                 | Henri     | Ühiktestide (_Unit tests_) katvus, arhitektuuri stabiilsus, vigade parandus.                                           |

## 8. Riskid ja eeldused (Risks and Assumptions)

### Riskid

- **AI hallutsinatsioonid:** Agent `test-generator` võib luua teste, mis töötavad juhuslikult või kasutavad ebakindlaid selektoreid. **Maandamine:** Tauri vaatab kogu koodi manuaalselt üle.
- **Saladuste ebakõla:** Kui lokaalsed `.env` failid pole sünkroonis, ebaõnnestub API _bypass_ ja testid ei käivitu.
- **Andmete stabiilsus:** Dünaamilised aktsiahinnad võivad põhjustada testides ebakõlasid (nt rahakoti saldo kontrollimisel).

### Eeldused

- Google Login _bypass_ API püsib turvaline ja töökorras.
- Chrome'i brauseris testimine loetakse antud faasis piisavaks katvuseks.

## 9. Testimise tulemid (Deliverables)

1.  **Testiplaan** (Käesolev dokument).
2.  **Testkoodi hoidla (Repo):** GitHubi failid (`tests/`, `helpers/api.ts`, `helpers/fixtures.ts`).
3.  **Dokumentatsioon:** `AIAbilTestidSelgitus.md` (Töövoo juhend) ja `TestHelpersSelgitus.md` (Arhitektuuri selgitus).
4.  **Raport:** Playwrighti HTML raport, mis tõestab testide edukat läbimist.
