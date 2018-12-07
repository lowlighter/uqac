//Dépendances
  const { spawn } = require("child_process")
  const fs = require("fs")
  const path = require("path")
  const {chunksToLinesAsync, chomp} = require("@rauschma/stringio")
  
//Récupération des fichiers de tests
  const tests = {}, asserts = {}
  fs
    .readdirSync(path.join(`${process.cwd()}`, "tests"))
    .filter(file => /.txt$/.test(file))
    .forEach(file => {
      test = fs.readFileSync(path.join(`${process.cwd()}`, "tests", file)).toString()
      tests[file] = (test.match(/>>> .*/g)||[]).map(v => v.substr(4))
      asserts[file] = test.substr(test.indexOf("# ASSERT")).split(/\r\n|\n/g).slice(1)
    })

//Démarrage de l'engine
  const engine = spawn("run.cmd")
  let booted = false, wready = false, ready = false, gentest = null
  ;(async function () {
    for await (const cline of chunksToLinesAsync(engine.stdout)) {
      let line = chomp(cline)
      if (/Program running.../.test(line)) booted = true
      if ((booted)&&(!ready)&&(!wready)) engine.stdin.write("isready\n"), wready = true
      if (/^readyok$/.test(line)) ready = true
      if ((ready)&&(!gentest)) gentest = StartTests()
      if (gentest) gentest.next(line.trim())
    }
  })()

//Tests unitaires
  function* StartTests() {
    console.clear()
    console.log("\nUNIT TESTS : ")
    let passed = 0, success = 0
    for (let test in tests) {
      //Nom du test
        console.log(`  ${test.replace(/.txt$/, "")} : `)
        passed++

      //Envoie des entrée de tests
        for (let input of tests[test])
          engine.stdin.write(`${input}\n`)
        let line = yield false

      //Vérification de la sortie
        let ok = true
        while (asserts[test].length) {
          console.log(`    \x1b[90m${line}\x1b[0m`)
          if ((!/ignore/.test(line))&&(line.length)) {
            let expected = asserts[test][0].trim()
            if (expected !== line) { 
              console.log(`\x1b[33m${`    Expected <${expected}> but got <${line}>`}\x1b[0m`)
              ok = false ; 
              break 
            }
            asserts[test].shift()
          }
          line = yield false
        }

      //Résultat
        ok ? console.log(`\x1b[32m${"    > OK"}\x1b[0m`) : console.log(`\x1b[31m${"    > KO"}\x1b[0m`)
        success += ok
    }

    //Résultat final
    console.log(`\n${success} / ${passed} passed`)
    process.exit(success === passed ? 0 : 1)
  }

  