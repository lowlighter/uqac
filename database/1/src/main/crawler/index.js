//Dependancies
  const x = require("x-ray")()
  const out = require("fs").createWriteStream("../resources/spells.json")
  const stringify = require("json-stringify-pretty-compact")
  const colors = require("colors")
  x.concurrency(1)
  
//Main
  ;(async function () {
    //Initialization
      console.clear()
      console.log(`  ID │ Lv. │ Spell name${" ".repeat(28)}`.bgWhite.black)
      out.write("[")
      let pass = 0, first = true
    //Retrieve spells
      for (let id = 1; pass < 3; id++) {
        //Scraper
          await x(`http://www.dxcontent.com/SDB_SpellBlock.asp?SDBID=${id}`, ".SpellDiv", {
            name:".heading",
            description:".SPDesc",
            raw:[".SPDet@html"]
          })
        //Post-processing
          ((e, spell) => {
            //Check errors
              if (e) return console.log(`${spell.id.toString().padStart(4, " ")} │ ${e.toString()}`.red)
              if (!spell.name) return console.log(`${id.toString().padStart(4, " ")} │  -  │ (ignored, no data)`.yellow), pass++
              pass = 0
            //Identifier
              spell.id = id
            //Transform raw data to formatted attributes
              let raw = []
              spell.raw.forEach(r => raw.push([...(r.match(/<b>[^<]+<\/b>[^<]+/g)||[])]))
              raw.flat().forEach(r => r.replace(/<b>([^<]+)<\/b>([^<]+)/, (m, g1, g2) => spell[g1.toLocaleLowerCase().replace(/ /g, "_")] = g2.trim().replace(/;$/, "")))
              delete spell.raw
            //Special formatting for attributes
              if ("level" in spell) spell.level = +(spell.level.match(/(?:sorcerer|wizard)\s+(\d+)/)||[,NaN])[1]
              if ("components" in spell) spell.components = spell.components.split(",").map(c => c.trim())
              if ("spell_resistance" in spell) spell.spell_resistance = /yes/.test(spell.spell_resistance); else spell.spell_resistance = false
            //Output
              out.write(Buffer.from(`${!first ? "," : (first = false, "")}\n${stringify(spell)}`, "utf8"))
              console.log(`${spell.id.toString().padStart(4, " ")} │  ${isNaN(spell.level) ? "-" : spell.level}  │ ${spell.name}`[isNaN(spell.level) ? "gray" : "white"])
          })
      }
    //Finalization
      out.write("\n]")
      console.log("\nDone !")
      process.stdin.setRawMode(true)
      process.stdin.resume()
      process.stdin.on("data", process.exit.bind(process, 0))
  })()
  