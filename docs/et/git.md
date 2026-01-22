Kõigepealt kloonida repository local arvutisse ja avada CMD root kaustas.

Siis teha uus branch nii: git switch -c branchiNimi

Saad kontrollida mis branchis oled commandiga: git branch

Mine VS code sisse ja tee mingi muudatus (Lisa READNE.md #Börsibaar pealkirja juurde nt: läbu branchist1)

Sellega vaatad, mis muudatused on tehtud: git status (Peaks ütlema punasega, et on muudetud faile mida pole stageitud)

Lisa siis kõik failid staging režiimi sellega: git add .

Ning commit sõnumiga: git commit -m "Kirjuta mida muutsid"

Kui commit sõnum läks valesti ja pole veel commit-i pushinud github-i, saab amend-ida sõnumit commandiga: git commit --amend (Asendad ülemise oranži sõnumi sellega mis tahad ja siis vajutad Esc, ning kirjutad :wq ja siis vajuta Enter)

Siis switchi main branchi (sama nagu checkout), tee: git switch main

Tee siis uus branch et luua conflict: git switch -c branchiNimi

Mine VS code sisse ja tee mingi muudatus (tee mingi muudatus samas failis kus eelmises branchis, et oleks conflict 2 faili vahel)

Siis commit: git add . ja siis: git commit -m "Kirjuta mida muutsid"

Siis merge uus branch vana branchiga, mis sa tekitasid (tee kindlaks et oled praegu uues branchis): git merge vanaBranchiNimiMisTekitasid

Nüüd peaks ütlema, et automatic merge failed kuna on mingis failis conflict

Tee: git status (Vaata kus ütleb punasega both modifed, et mis fail on conflictis)

Siis conflicti lahendamiseks kas jätad alles praeguse branchi muudatuse:
git checkout --ours -- path\to\file\that\is\conflicted
Või teise branchi muudatuse (See branch kus praegu sa pole kui teed command: git branch):
git checkout --theirs -- path\to\file\that\is\conflicted

Ja lisa fail mis tahad alles jätta (paneb, et conflict resolved):
git add path\to\file\that\is\conflicted

Siis kui enam conflicti pole, lõpeta merge (commit): git commit -m "text"

Kui tuleb välja, et mergisid kokku valed branchid, siis saad merge tagasi võtta kui commit tehtud sellise käsuga: git reset --merge ORIG_HEAD

Siis kustuta branch ära mis pole kasutusel (lokaalselt), kuna mergisid ära:
git branch -d branchName

Siis branchis, mis on alles, kustuta ära läbu mis sa tekitasid et teha conflict.

Siis tee commit, näiteks "Puhastasin README.md faili" (Eeldan, et oskad nüüd teha git add ja commit)

Siis võid branchi ära kustutada, mis tekitasid. (Pead selleks liikuma teise branchi sisse)

Teine:

Kõigepealt kloonida repository local arvutisse. Mine mingi kataloogi sisse, kus tahad et repository ilmuks, ava seal kataloogi sees cmd (Shift + right click, vali open in terminal)
Siis pane command: git clone https://github.com/taltech-vanemarendajaks/team-3

Siis tuleb avada manual update branch nii: git switch manual-update

Siis mine VS code sisse ja loo uus fail docs/et kataloogis: git.md

Ja pane sinna sisse mingi tekst nagu: Läbu tormi poolt

Nüüd tee command: git status (Kontroll, et lisasid git.md faili)

Siis add to staging ja commit fail: git add .
Ja: git commit -m "Lisasin git.md faili"

Ja pushi siis faili origin github-i: git push origin manual-update
