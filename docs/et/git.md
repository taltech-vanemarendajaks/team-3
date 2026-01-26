# Git-i lühijuhend: Muudatused, konfliktid ja haldus

See juhend kirjeldab peamisi Git-i töövõtteid alates kloonimisest kuni konfliktide lahendamiseni.

---

## 1. Alustamine ja uue haru (branch) loomine

1.  **Klooni repositoorium** oma arvutisse ja ava terminal (CMD või PowerShell) projekti juurkaustas.
2.  **Loo uus haru:**
    ```bash
    git switch -c branchiNimi
    ```
3.  **Kontrolli aktiivset haru:**
    ```bash
    git branch
    ```

## 2. Muudatuste tegemine ja committimine

1.  Tee VS Code-is muudatus (näiteks lisa `README.md` faili pealkirja juurde tekst: `#Börsibaar - läbu branchist1`).
2.  **Vaata muudatuste staatust:**
    ```bash
    git status
    ```
    *(Muudetud failid peaksid olema punased).*
3.  **Lisa failid staging-alasse:**
    ```bash
    git add .
    ```
4.  **Tee commit:**
    ```bash
    git commit -m "Kirjuta siia, mida muutsid"
    ```

### Commiti parandamine (amend)
Kui soovid viimast commit-sõnumit muuta (enne pushimist):
1.  Kasuta käsku: `git commit --amend`.
2.  Avaneb tekstiredaktor (Vim). Muuda tekst ära.
3.  Salvestamiseks ja väljumiseks: vajuta `Esc`, trüki `:wq` ja vajuta `Enter`.

---

## 3. Konflikti tekitamine ja lahendamine

### Konflikti loomine
1.  Liigu tagasi peaharru: `git switch main`.
2.  Loo uus haru: `git switch -c teineBranchiNimi`.
3.  Tee muudatus **samas failis ja samas kohas**, kus eelmises branchis.
4.  Salvesta ja commiti:
    ```bash
    git add .
    git commit -m "Teine muudatus konflikti jaoks"
    ```

### Mergemine ja konflikti lahendamine
1.  Proovi harud kokku sulatada (veendu, et oled uues harus):
    ```bash
    git merge vanaBranchiNimi
    ```
    *Git annab teada: "Automatic merge failed; fix conflicts and then commit the result".*
2.  Kontrolli staatust: `git status` (näitab faili juures "both modified").
3.  **Lahenda konflikt valides sobiv versioon:**
    * Säilita praeguse haru muudatus:
        ```bash
        git checkout --ours -- path/to/file
        ```
    * Säilita teise haru muudatus:
        ```bash
        git checkout --theirs -- path/to/file
        ```
4.  Märgi konflikt lahendatuks: `git add path/to/file`.
5.  Lõpeta merge uue commitiga: `git commit -m "Merge ja konflikti lahendus"`.

> **Märkus:** Kui merge läks valesti ja soovid seda tühistada:
> ```bash
> git reset --merge ORIG_HEAD
> ```

---

## 4. Koristustööd (Branchide kustutamine)

1.  Kustuta lokaalne haru, mida pole enam vaja:
    ```bash
    git branch -d branchiNimi
    ```
2.  Puhasta failid üleliigsest "läbust".
3.  Tee lõplik commit:
    ```bash
    git add .
    git commit -m "Puhastasin README.md faili"
    ```

---

## 5. Ülesanne: Manual Update

Järgi neid samme konkreetse haru uuendamiseks:

1.  **Klooni repositoorium:**
    Ava kaust, kuhu soovid projekti, ja käivita terminal (Shift + paremklikk -> Open in Terminal).
    ```bash
    git clone [https://github.com/taltech-vanemarendajaks/team-3](https://github.com/taltech-vanemarendajaks/team-3)
    ```
2.  **Liigu õigesse harru:**
    ```bash
    git switch manual-update
    ```
3.  **Loo uus fail:**
    Loo fail asukohta `docs/et/git.md` ja lisa sinna tekst (nt "Läbu tormi poolt").
4.  **Salvesta ja commiti:**
    ```bash
    git status
    git add .
    git commit -m "Lisasin git.md faili"
    ```
5.  **Pushi muudatused GitHubi:**
    ```bash
    git push origin manual-update
    ```