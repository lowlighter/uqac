/** Force le rendu du sudoku. */
  async function rendering(step = 0) {
    if (sudoku.solving.kill) { sudoku.solving.reset(); throw "KILLED" }
    rendering.render()
    await new Promise(solve => setTimeout(solve, step))
  }
  rendering.render = function () { if ((rendering.renderer)&&(rendering.renderer.$forceUpdate)) rendering.renderer.$forceUpdate() }
  rendering.step = 0

/** Retourne un set trié par le nom de ses valeurs. */
  function sortByName(set) {
    return new Set([...set].sort())
  }

/** Retourne un set trié selon une map. */
  function sortByMappedValue(map) {
    return new Set(Object.keys(map).sort((a, b) => map[a] - map[b]))
  }

/** Retourne un set trié selon une map (sens inverse). */
  function sortByMappedValueReversed(map) {
    return new Set(Object.keys(map).sort((a, b) => map[b] - map[a]))
  }

/** Retourne un set filtré selon une unique valeur. */
  function filterByMappedValue(map, value) {
    return new Set(Object.keys(map).filter(k => map[k] === value))
  }

/** Modification manuelle du sudoku. */
  function maker(event) {
    let value = event.key
    //Suppression
      if ((value === "Backspace")||(value === "Delete")) 
        maker.set.set(event.target.name, NaN)
    //Ajout
      else if (/[1-9]/.test(value))
        maker.set.set(event.target.name, Number(value))
    rendering()
  }
  maker.set = new Map()

/** Générateur aléatoire de sudoku. */
  maker.random = function () {
    //Arrêt de la résolution
      if (sudoku.solving) stop()

    //Création aléatoire d'un sudoku
      let array = sudokugenerator.sudoku.generate("very-hard").split("").map(Number), data = {}
      for (let x = 0; x < 9; x++)
        for (let y = 0; y < 9; y++)
          data[`${x}:${y}`] = Number.isFinite(array[x+y*9]) ? array[x+y*9] : NaN
    
    //Mise à jour du sudoku
      sudoku.init(data)
      sudoku.solver = null
      maker.set.clear()
      rendering.render()
  }

/** Nettoie le sudoku */
  maker.clear = function () {
    sudoku.init({})
    sudoku.solver = null
    maker.set.clear()
    rendering.render()
  }

/** Résolution de sudoku */
  async function solve() {
    //Ne rien faire si en cours de résolution
      if (sudoku.solving.started) return 
      
    //Création de sudoku à partir des entrées utilisateurs
      let data = {}
      document.querySelectorAll(".square.fixed input").forEach(input => {
        let {value, name} = input
        value = /\d+/.test(value) ? Number(value) : NaN
        if (Number.isFinite(value)) data[name] = value
      })
      sudoku.init(data)
      maker.set.clear()
      //rendering.step = Number(document.querySelector('[name="rendering_step"]').value) * 1000

    //Résolution
      await sudoku.solve(backtracking, {
        ac3:document.querySelector('[name="ac3"]').checked, 
        lcv:document.querySelector('[name="lcv"]').checked,
        mrv:document.querySelector('[name="mrv"]').checked,
        dh:document.querySelector('[name="dh"]').checked,
        basic:!(document.querySelector('[name="mrv"]').checked||document.querySelector('[name="dh"]').checked)
      }) 
  }

/** Force l'arrêt de la résolution. */
  function stop() {
    sudoku.solving.cancel()
  }

/** Initialise le sudoku */
  async function sudoku_init() {
    sudoku = new Sudoku()
    maker.random()
    rendering.renderer = new Vue({el:"#app", data:{sudoku}})
    try { await rendering() } catch (e) {}
  }

/**  
 * Classe permettant de gérer l'état de résolution d'un CSP.
 */
  class ConstraintSatisfactionProblemSolvingState {
    /** 
     * Constructor. 
     */
      constructor() {
        this.started = null
        this.ended = null
        this.time = null
        this.solvable = false
        this.kill = false
      }

    /** 
     * Démarre le chrono et indique qu'une résolution est en cours.
     */
      start() {
        this.started = performance.now()
        this.ended = false
        this.time = null
        this.solvable = false
        this.kill = false
      }

    /**
     * Arrête le chrono et indique qu'une résolution s'est terminée.
     */
      end(t, solvable) {
        this.started = null
        this.ended = true
        this.time = t
        this.solvable = !!solvable
      }

    /** 
     * Arrête le chrono et indique qu'une résolution s'est arrêtée.
     */
      cancel(t) {
        this.started = null
        this.ended = true
        this.time = t
        this.solvable = null
        this.kill = true
      }

    /** 
     * Réinitialise l'état. 
     */
      reset() {
        this.kill = false
      }

  }