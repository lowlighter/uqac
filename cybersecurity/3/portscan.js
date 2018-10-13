//Dépendances
  const { spawn } = require("child_process")
  const fs = require("fs")
  console.clear()

//Seuil d'alerte
  let n = 100

//Extraction du pcap via Tshark (Wireshark en ligne de commande)
  let extracted = "" ; debug(0, n)
  const tshark = spawn("Wireshark\\Wireshark\\tshark", [
    "-r", "portscan.pcap", //Lecture depuis le fichier pcap
    "-T", "fields", "-E", "separator=;", //Format pour traiter les données
    "-e", "ip.src", //Récupération de l'IP source
    "-e", "ip.dst", //Récupération de l'IP de destination
    "-e", "tcp.port", //Récupération des ports TCP (si définis)
    "-e", "udp.port", //Récupération des ports UDP (si définis)
  ])

//Récupération de l'extraction
  tshark.stdout.on("data", chunk => ((extracted += chunk.toString()), debug(1)))
//Fin de l'extraction
  tshark.on("close", code => (debug(2), analysis()))

//Analyse de l'extraction
  function analysis() {
    //Map qui va stocker les scans
      let scans = {} ; debug(3)

    //Parcours de l'extraction
      extracted.split("\n").forEach(line => {
        //Lecture des données
          let [src, dst, tcp, udp] = line.trim().split(";")
          let port = NaN
        //Récupération du port cible
          if (tcp) port = tcp.split(",")[1]
          if (udp) port = udp.split(",")[1]

        //Si toutes les données sont présentes,
          if ((src)&&(dst)&&(port)) {
            //Ajoute le port ciblé dans les ports scannés
              let key = `${src}->${dst}`
              ;(scans[key]||(scans[key] = [])).push(port)
            //Debug
              debug(4, `${key}:${port}`)
          }
      })
      debug(2)

    //Affichage des résultats
      console.log(`\n\nRésultats\n${"Source -> Destination".padEnd(36, " ")} | ${"Paquets envoyés".padEnd(16, " ")} | ${"Ports scannés".padEnd(16, " ")} | ${"Alerte".padEnd(8, " ")}\n${"=".repeat(36+3+16+3+16+3+8)}`)
      for (let i in scans) {
        //Nombre de paquets envoyés, nombre de ports utilisés et alerte si supérieur au seuil
          let paquets = scans[i].length
          let ports = new Set(scans[i]).size
          let alert = ports > n
        //Affichage
          console.log(`\x1b[${alert ? 33 : 37}m${i.padStart(36, " ")} | ${paquets.toString().padStart(16, " ")} | ${ports.toString().padStart(16, " ")} | ${alert ? "Alerte" : ""}\x1b[0m`)
      }

  }

//Message de debug
  function debug(x, d = "") {
    if (x === 0) console.log(`Seuil d'alerte : > ${d}\n\nExtraction Wireshark...`)
    if (x === 1) process.stdout.write(`\r    Lignes : ${extracted.split(/\n/).length}`)
    if (x === 2) console.log("\n    Terminé")
    if (x === 3) console.log("\nAnalyse de l'extraction...")
    if (x === 4) process.stdout.write(`\r    Requête : ${d}${" ".repeat(32)}`)
  }
