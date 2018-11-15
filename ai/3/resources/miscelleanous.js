//Fonction d'attente
  function sleep(time) {
    return new Promise(solve => setTimeout(solve, time))
  }

//Clone les données d'un tableau
  function clone(data) {
    return JSON.parse(JSON.stringify(data))
  }

//Calcule la connexité entre deux cases
  function connectivity(map, {from, to, walls}) {
    //Crée un clone des données et l'initialise avec un identifiant différent
      let graph = [], a, b
      for (let x = 0; x < map.length; x++) {
        graph[x] = []
        for (let y = 0; y < map[x].length; y++) {
          if (map[x][y].has(from)) a = {x, y} 
          if (map[x][y].has(to)) b = {x, y}
          graph[x][y] = !map[x][y].has(walls) ? x*map.length + y : NaN
        }
      }
    
    //Calcule les zones connexes
      for (let i = 0; i < graph.length; i++) {
        for (let x = 0; x < graph.length; x++) {
          for (let y = 0; y < graph[x].length; y++) {
            [{dx:-1, dy:0}, {dx:+1, dy:0}, {dx:0, dy:-1}, {dx:0, dy:+1}].forEach(({dx, dy}) => {
              let nx = x + dx, ny = y + dy
              if ((nx >= 0)&&(nx < graph.length)&&(ny >= 0)&&(ny < graph[x].length)) {
                let n = graph[nx][ny]
                if (!isNaN(n))
                  graph[x][y] = Math.min(graph[x][y], n)
              }
            })
          }
        }
      }

    //Connexe si les deux cases ont la même valeur
      return graph[a.x][a.y] === graph[b.x][b.y]
  }

//Mélange un tableau
  function shuffle(array) {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[array[i], array[j]] = [array[j], array[i]]
    }
    return array
  }

//Génère des couples {x, y} avec 0 <= x < w et 0 <= y < h et les mélanges
  function Positions({width, height}) {
    let array = []
    for (let x = 0; x < width; x++)
      for (let y = 0; y < height; y++)
        if (x||y) array.push({x, y})
    return shuffle(array)
  }

//Détermine les classes de chaque cellule
  function cell_class(x, y) {
    x-- ; y--
    let cell = environment.agent.knowledge_base.facts[`${x},${y}`]
    return {
      "unexplored":!cell.has($.CELL.EXPLORED),
      "risky-low":cell.has($.CELL.MONSTER_LOW_RISK)||cell.has($.CELL.HOLE_LOW_RISK),
      "risky-med":cell.has($.CELL.MONSTER_MED_RISK)||cell.has($.CELL.HOLE_MED_RISK),
      "risky-high":cell.has($.CELL.MONSTER_HIGH_RISK)||cell.has($.CELL.HOLE_HIGH_RISK),
      "risky-max":cell.has($.CELL.MONSTER_MAX_RISK)||cell.has($.CELL.HOLE_MAX_RISK)||cell.has($.CELL.MONSTER)||cell.has($.CELL.HOLE),
    }
  }

  
//Constantes
  const $ = {
    //Cellules
      CELL:{
        AGENT:String.fromCodePoint(129335),
        MONSTER:String.fromCodePoint(129425),
        MONSTER_DEAD:String.fromCodePoint(127843),
        EXCREMENT:String.fromCodePoint(128169),
        WIND:String.fromCodePoint(128168),
        HOLE:String.fromCodePoint(128371),
        PORTAL:String.fromCodePoint(127756),
        MONSTER_LOW_RISK:`${String.fromCodePoint(129425)}${String.fromCodePoint(10071)}`,
        HOLE_LOW_RISK:`${String.fromCodePoint(128371)}${String.fromCodePoint(10071)}`,
        MONSTER_MED_RISK:`${String.fromCodePoint(129425)}${String.fromCodePoint(10071).repeat(2)}`,
        HOLE_MED_RISK:`${String.fromCodePoint(128371)}${String.fromCodePoint(10071).repeat(2)}`,
        MONSTER_HIGH_RISK:`${String.fromCodePoint(129425)}${String.fromCodePoint(10071).repeat(3)}`,
        HOLE_HIGH_RISK:`${String.fromCodePoint(128371)}${String.fromCodePoint(10071).repeat(3)}`,
        MONSTER_MAX_RISK:`${String.fromCodePoint(129425)}${String.fromCodePoint(10071).repeat(4)}`,
        HOLE_MAX_RISK:`${String.fromCodePoint(128371)}${String.fromCodePoint(10071).repeat(4)}`,
        EXPLORED:String.fromCodePoint(128065),
        PREVIOUS_POSITION:"Previous position",
        SHOT:String.fromCodePoint(128165),
        EMPTY:"",
      },
    //Actions
      ACTION:{
        EXIT:0b001,
        MOVE:0b100,
        UP:0b100,
        DOWN:0b101,
        LEFT:0b110,
        RIGHT:0b111,
        SHOOT:0b1000000,
        SHOOT_UP:0b1100000,
        SHOOT_DOWN:0b1010000,
        SHOOT_LEFT:0b1110000,
        SHOOT_RIGHT:0b1001000,
      },
    //Scores
      SCORE:{
        EXIT:10,
        DEATH:-10,
        MOVE:-1,
        SHOOT:-10,
      }
  }
  
//Marques 
  $.MARK = {
    [$.CELL.HOLE]:$.CELL.WIND,
    [$.CELL.MONSTER]:$.CELL.EXCREMENT
  }


  $.RISK = {
    [$.CELL.WIND]: [$.CELL.HOLE_LOW_RISK, $.CELL.HOLE_MED_RISK, $.CELL.HOLE_HIGH_RISK, $.CELL.HOLE_MAX_RISK], 
    [$.CELL.EXCREMENT]:[$.CELL.MONSTER_LOW_RISK, $.CELL.MONSTER_MED_RISK, $.CELL.MONSTER_HIGH_RISK, $.CELL.MONSTER_MAX_RISK]
  }

// Map effector

  $.EFFECTOR = {
    [$.ACTION.EXIT]:async function (agent) { await agent.effectors.exit.exit()},
    [$.ACTION.SHOOT]:async function (agent) { await agent.effectors.shoot.shoot()},
    [$.ACTION.SHOOT_UP]:async function (agent) { await agent.effectors.shoot.shoot_up()},
    [$.ACTION.SHOOT_DOWN]:async function (agent) { await agent.effectors.shoot.shoot_down()},
    [$.ACTION.SHOOT_LEFT]:async function (agent) { await agent.effectors.shoot.shoot_left()},
    [$.ACTION.SHOOT_RIGHT]:async function (agent) { await agent.effectors.shoot.shoot_right()},
    [$.ACTION.UP]:async function (agent) { await agent.effectors.move.up()},
    [$.ACTION.DOWN]:async function (agent) { await agent.effectors.move.down()},
    [$.ACTION.LEFT]:async function (agent) { await agent.effectors.move.left()},
    [$.ACTION.RIGHT]:async function (agent) { await agent.effectors.move.right()}
  }

// Valeur permettant d'evaluer la pertinence d'une case de l'environnement

  $.VALUE = {
    [$.CELL.PREVIOUS_POSITION]: -2,
    [$.CELL.EXPLORED]:-11,
    [$.CELL.HOLE_LOW_RISK]: -5,
    [$.CELL.MONSTER_LOW_RISK]:-5,
    [$.CELL.HOLE_MED_RISK]: -12,
    [$.CELL.MONSTER_MED_RISK]:-12,
    [$.CELL.HOLE_HIGH_RISK]: -20,
    [$.CELL.MONSTER_HIGH_RISK]:-20,
    [$.CELL.HOLE_MAX_RISK]: -30,
    [$.CELL.MONSTER_MAX_RISK]:-30,
    [$.CELL.MONSTER]: -50,
    [$.CELL.HOLE]: -50,
    [$.CELL.EXIT]: 30,
    [$.CELL.WIND]: 0,
    [$.CELL.EXCREMENT]:0,
    [$.CELL.MONSTER_DEAD]:0,
    [$.CELL.SHOT]:0
  }

  $.VALUE_SHOOT = {
    BASE:-5,
    [$.CELL.EXPLORED]:-Infinity,
    [$.CELL.HOLE_LOW_RISK]: -Infinity,
    [$.CELL.MONSTER_LOW_RISK]:-30,
    [$.CELL.HOLE_MED_RISK]: -Infinity,
    [$.CELL.MONSTER_MED_RISK]:-25,
    [$.CELL.HOLE_HIGH_RISK]: -Infinity,
    [$.CELL.MONSTER_HIGH_RISK]:-20,
    [$.CELL.HOLE_MAX_RISK]: -Infinity,
    [$.CELL.MONSTER_MAX_RISK]:0,
    [$.CELL.MONSTER]: 10,
    [$.CELL.HOLE]: -Infinity,
    [$.CELL.EXIT]: -Infinity,
    [$.CELL.WIND]: -Infinity,
    [$.CELL.EXCREMENT]:-Infinity,
    [$.CELL.MONSTER_DEAD]:-Infinity,
    [$.CELL.SHOT]:-Infinity,
    [$.CELL.PREVIOUS_POSITION]:-Infinity
  }

// Vecteur de translation pour le mouvement d'un agent

  $.TRANSLATION = {
    [$.ACTION.RIGHT]: [1, 0],
    [$.ACTION.LEFT]:[-1, 0],
    [$.ACTION.DOWN]: [0, 1],
    [$.ACTION.UP]: [0, -1],
    [$.ACTION.SHOOT_RIGHT]: [1, 0],
    [$.ACTION.SHOOT_LEFT]:[-1, 0],
    [$.ACTION.SHOOT_DOWN]: [0, 1],
    [$.ACTION.SHOOT_UP]: [0, -1]
  }
  

//Messages de debug
  $.DEBUG = {
    ENABLED:true,
    [$.ACTION.SHOOT]:"Tirer",
    [$.ACTION.EXIT]:"Sortir",
    [$.ACTION.UP]:"Haut",
    [$.ACTION.DOWN]:"Bas",
    [$.ACTION.LEFT]:"Gauche",
    [$.ACTION.RIGHT]:"Droite",
    EXIT_FAILED:"Aucun portail",
    EXIT_SUCCESS:"Niveau suivant",
    SHOOT_MISS:"Aucune cible touchée",
    SHOOT_SUCCESS:"Un monstre a été abbatu !",
    SHOOT_TO(data) { return `Tirer ${data.dx < 0 ? "à gauche" : data.dx > 0 ? "à droite" : data.dy < 0 ? "en haut" : data.dy > 0 ? "en bas" : ""}` },
    DEATH:"L'agent est mort !",
    SCORE(score, prev) { return `Score : ${score} (${score-prev>=0?"+":""}${score-prev})` }
  }
