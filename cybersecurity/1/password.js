//Dépendances
  const fs = require("fs")
  const crypto = require('crypto')
  console.clear()

//Q1
console.log("\nQ1 ================")
{
  //Lecture du dictionnaire
    let data = fs.readFileSync("dict.txt").toString()
    let dict = data.split("\r\n")
  //Création de la lookup table
    var lookup = {}
    dict.forEach(w => lookup[crypto.createHash("sha256").update(w).digest("hex").toString("hex")] = w)
  //Inversion du hash
    process.stdout.write(`${"UTILISATEUR".padEnd(16, " ")} ${"MOT DE PASSE"}\n`)
    fs.readFileSync("hashedPasswordFile.txt").toString().replace(/([a-z]+)\s+([a-f0-9]+)/gm, (m, u, p) => {
      process.stdout.write(`${u.padEnd(16, " ")} ${lookup[p]}\n`)
    })
}

//Q2
console.log("\nQ2 ================")
{
  //Calcul du sel et du nouveau hash
    process.stdout.write(`${"UTILISATEUR".padEnd(16, " ")} ${"SEL".padEnd(64, " ")} ${"HASH h(p + s)".padEnd(64, " ")}\n`)
    fs.readFileSync("hashedPasswordFile.txt").toString().replace(/([a-z]+)\s+([a-f0-9]+)/gm, (m, u, p) => {
      let salt = crypto.createHash("sha256").update(u).digest("hex").toString("hex")
      let hash = crypto.createHash("sha256").update(lookup[p] + salt).digest("hex").toString("hex")
      process.stdout.write(`${u.padEnd(16, " ")} ${salt} ${hash}\n`)
    })
}

//Q3
console.log("\nQ3 ================")
{
  //Lecture du dictionnaire
    let data = fs.readFileSync("dict.txt").toString()
    let dict = data.split("\r\n")
    process.stdout.write(`${"UTILISATEUR".padEnd(16, " ")} ${"MOT DE PASSE"}\n`)
  //1 mot du dictionnaire + 1 nombre de 00 à 99
    search1:{
      let p = "2a2fa6d7a8a83ca291b298a373fe602a2c2879c841a3bd8ef818d3cfaa9ecd40"
      for (let i = 0; i < dict.length; i++) {
        for (let j = 0; j < 100; j++) {
          let hash = crypto.createHash("sha256").update(dict[i] + j).digest("hex").toString("hex")
          process.stdout.write(`\r${"tyrion".padEnd(16, " ")} ${i*100+j}/${dict.length*100}`)
          if (hash === p) {
            process.stdout.write(`\r${" ".repeat(64)}`)
            process.stdout.write(`\r${"tyrion".padEnd(16, " ")} ${dict[i] + j}\n`)
            break search1
          }
        }
      }
    }
  //Méthode XKCD 2
    search2:{
      let p = "f06f9c14c073e5b51503b6a4ecaccdee594420f60382b7c755d92ed4bdb6ee04"
      for (let i = 0; i < dict.length; i++) {
        for (let j = 0; j < dict.length; j++) {
          let hash = crypto.createHash("sha256").update(dict[i] + dict[j]).digest("hex").toString("hex")
          process.stdout.write(`\r${"littlefinger".padEnd(16, " ")} ${i*dict.length+j}/${dict.length**2}`)
          if (hash === p) {
            process.stdout.write(`\r${" ".repeat(64)}`)
            process.stdout.write(`\r${"littlefinger".padEnd(16, " ")} ${dict[i] + dict[j]}\n`)
            break search2
          }
        }
      }
    }
}

//Fin du programme
  console.log("\n\nAppuyez sur n'importe quelle touche pour quitter")
  process.stdin.setRawMode(true)
  process.stdin.resume()
  process.stdin.on("data", process.exit.bind(process, 0))
