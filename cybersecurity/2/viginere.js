//Dépendances
  const readlineSync = require("readline-sync")
  const fs = require("fs")
  console.clear()

//Lecture du fichier
  console.log("\nTexte encrypté :")
  const x = fs.readFileSync("input.txt").toString()
  x.match(/.{1,50}/g).map(v => v.match(/.{1,5}/g).join(" ")).forEach(v => process.stdout.write(`    ${v}\n`))

//Fréquence des lettres en français (c.f. https://fr.wikipedia.org/wiki/Fr%C3%A9quence_d%27apparition_des_lettres_en_fran%C3%A7ais)
//(Tableau doublé pour plus de facilité lors de la phase analyse fréquentielle)
  let analysis = [7.45, 1.14, 3.24, 3.67, 14.43, 1.11, 1.94, 1.11, 6.62, 0.34, 0.29, 4.96, 2.62, 6.39, 5.06, 2.49, 0.65, 6.07, 6.51, 5.92, 4.53, 1.11, 0.17, 0.38, 0.46, 0.15, 7.45, 1.14, 3.24, 3.67, 14.43, 1.11, 1.94, 1.11, 6.62, 0.34, 0.29, 4.96, 2.62, 6.39, 5.06, 2.49, 0.65, 6.07, 6.51, 5.92, 4.53, 1.11, 0.17, 0.38, 0.46, 0.15]
  let alphabet = "abcdefghijklmnopqrstuvwxyz".split("")

//=====================================================
//Recherche des occurence par n-grams
  const max_n = 6
  console.log(`\nRecherche des n-grams (n ≤ ${max_n}) :`)
  let lengthes = new Map()
  for (let n = 2; n <= max_n; n++) {
    for (let i = 0; i < x.length - n; i++) {
      //Lecture du nouveau n-gramme
        let gram = x.slice(i, i + n)
        process.stdout.write(`\r    ${gram.padEnd(max_n, " ")}`)
      //Recherches des occurences dans le texte
        let regex = new RegExp(`${gram}`, "g"), matches = [], match
        while (match = regex.exec(x)) matches.push(match.index)
      //Si aucune occurence hormis la première, traitement du prochain n-gramme
        if (matches.length <= 1) continue
      //Sinon calcul de la distance entre chaque n-gramme (et suppression de la première occurrence)
        for (let j = matches.length-1; j > 0; j--) matches[j] -= matches[j-1]
        matches.shift()
        if (!matches.length) continue
      //Recherches des facteurs de distances
        let factors = []
        matches.forEach(v => { factors.push(v) ; for (let k = 2; k < v**0.5; k++) (v%k === 0) ? factors.push(k) : null })
      //Enregistrement des facteurs de distances dans la liste globale
        factors.forEach(v => lengthes.set(v, (lengthes.get(v)||0) + 1))
        process.stdout.write(` | ${(1+matches.length).toString().padStart(2, " ")} fois | ${[...new Set(factors)].sort((a, b) => a - b).join(", ")}\n`)
    }
  }

//=====================================================
//Recherche de la longueur de la clé
  console.log(`\r${" ".repeat(32)}\nLongueur de la clé :`)

//Recherche des facteurs de distances les plus fréquents
  const total = [...lengthes.values()].reduce((a, b) => a + b, 0)
  lengthes.forEach((v, k) => lengthes.set(k, v/total))
  lengthes = [...lengthes.entries()].sort((a, b) => b[1] - a[1]).slice(0, 10)
  for (let r of lengthes) process.stdout.write(`\r    ${r[0].toString().padStart(max_n, " ")} | ${(r[1]*100).toFixed(2).padStart(5, " ")} %\n`)

//Choix de la longueur de clé
  process.stdout.write(`\nChoix de la longueur de clé : `)
  const key_length = readlineSync.question("", {limit:/\d+/, defaultInput:lengthes[0]})

//=====================================================
//Découpage en tranches du texte initial
  console.log("\nAnalyse fréquentielle (utiliser Q et D pour naviguer, [Espace] pour valider): ")
  let chunks = x.match(new RegExp(`.{${key_length}}`, "g")),  key = ""

//Parcours des tranches en fonction de la longueur de la clé
  for (let i = 0; i < key_length; i++) {
    //Analyse fréquentielle des lettres de chaque tranche
      let frequencies = new Map()
      chunks.forEach(y => frequencies.set(y[i], (frequencies.get(y[i]) || 0) + 1))

    //Génération de la table de fréquence et affichage
      let r1 = "", r2 = "", min = Math.min(...[...frequencies.values()]), max = Math.max(...[...frequencies.values()])
      for (let c of alphabet) {
        let v = frequencies.get(c)||0
        r1 += c.padStart(4, " ")
        r2 += `\x1b[${v === max ? 32 : v === min ? 33 : v === 0 ? 31 : 37}m${v.toString().padStart(4, " ")}\x1b[0m`
      }
      process.stdout.write(`${r1}\n`)
      process.stdout.write(`${r2}\n`)

    //Estimation de la fréquence pour chaque lettre à partir des occurences dans la langue française
      function estimate(offset = 0) {
        //Calcul et affichage
          let r3 = "", estimated = alphabet.map(c => Math.round(analysis[(alphabet.indexOf(c) + alphabet.length - offset) % alphabet.length]/100 * chunks.length))
          let min = Math.min(...estimated), max = Math.max(...estimated)
          for (let c of alphabet) {
            let v = estimated[alphabet.indexOf(c)]
            r3 += `\x1b[${v === max ? 32 : v === min ? 33 : v === 0 ? 31 : 37}m${v.toString().padStart(4, " ")}\x1b[0m`
          }
          process.stdout.write(`\r${r3} | ${alphabet[offset]} (+ ${offset})`)

        //Entrée utilisateur
          let key = readlineSync.keyIn("", {hideEchoBack:true, mask:"", limit:"qd ", keepWhitespace:true, defaultInput:" "})
          if (key === "q") return estimate((offset + alphabet.length - 1) % alphabet.length)
          if (key === "d") return estimate((offset + 1) % alphabet.length)
        return alphabet[offset]
      }

    //Ajout de la letrre choisie dans la clé
      key += estimate()
      process.stdout.write("\n\n")
  }

//=====================================================
//Décryptage du texte
  console.log("\nClé supposée : ")
  process.stdout.write(`    ${key}\n`)

//Décryptage du texte (soustraction de la clé au texte encrypté)
  console.log("\nTexte décrypté : ")
  let output = "", rkey = key.split("").map(c => alphabet.indexOf(c))
  for (let i = 0; i < x.length; i++)
    output += alphabet[(alphabet.indexOf(x[i]) - rkey[i % key.length] + alphabet.length) % alphabet.length]

//Affichage du texte
  output.match(/.{1,50}/g).map(v => v.match(/.{1,5}/g).join(" ")).forEach(v => process.stdout.write(`    ${v}\n`))

//=====================================================
//Fin du programme
  console.log("\n\nAppuyez sur n'importe quelle touche pour quitter")
  process.stdin.setRawMode(true)
  process.stdin.resume()
  process.stdin.on("data", process.exit.bind(process, 0))
