const express = require('express')
const app = express()
const sharp = require('sharp');


const fs = require("fs")
const path = require("path")

if (false && fs.existsSync("raw2")) {
  fs.readdirSync("raw2").forEach(chapter => {
    let source = path.join(process.cwd(), "raw2", chapter), dest = path.join(process.cwd(), "raw", chapter.padStart(4, "0"))
    fs.readdirSync(source).forEach(file => /\.jpg$/.test(file) ? fs.copyFileSync(path.join(source, file), `${dest}_${file.padStart(8, "0")}`) : null)
  })
}

app.get("/files", (req, res) => {
  res.setHeader("Content-Type", "application/json")
  res.end(JSON.stringify(fs.readdirSync("raw")))
})

app.get("/save", (req, res) => {
  let {x, y, size, src} = req.query
  x = Number(x); y = Number(y); size = Number(size)
  sharp(`raw/${src}.jpg`)
    .extract({left:x, top:y, width:size, height:size})
    .toFile(`data/${src}-${uuid()}.jpg`)
  res.send("ok")
})

app.use("/", express.static(process.cwd()))

app.listen(3000, function () {
  console.log('Example app listening on port 3000!')
})


function uuid() {
  return "xxxxxxxx".replace(/[xy]/g, function(c) {
    let r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  });
}
