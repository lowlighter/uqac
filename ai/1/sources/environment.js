/**
 * Environment.
 * Cette classe permet de gérer les données de l'environment.
 */
  class Environment {

    /**
     * Constructeur.
     * @param {Object} options - Options (voir ci-dessous)
     * @param {Number} options.width - Largeur de l'environment
     * @param {Number} options.height - Hauteur de l'environment
     * @param {Number} options.dust - Probabilité de générer de la poussière à chaque itération
     * @param {Number} options.jewel - Probabilité de générer un bijou à chaque itération
     * @param {Number} options.frequency - Fréquence d'itération (en Hertz)
     * @param {Number} options.agent - Référence vers l'agent
     * @param {Number} options.renderer - Référence vers le moteur de rendu
     * @param {Number} options.scores - Options du scoring
     */
      constructor({width, height, dust, jewel, frequency, agent, renderer, scores} = options = {}) {
        //Propriétés de l'environment
          this.width = width
          this.height = height
          this.dust = dust
          this.jewel = jewel
          this.frequency = frequency
          this.agent = agent
          this.renderer = renderer
          this.scores = new Environment.scores(scores)

        //Initialisation des pièces du manoir
          this.map = []
          this.clear()

        //Initialisation de la surface de rendu
          this.renderer.init(this)
        //Initialisation des capteurs et des effecteurs de l'agent
          this.agent.x = this.agent.y = 0
          for (let i in this.agent.sensors) this.agent.sensors[i].init(this)
          for (let i in this.agent.effectors) this.agent.effectors[i].init(this)
        //Démarrage de la boucle principale de l'agent
          this.agent.loop()

        //Démarrage de la boucle principale de l'environment
          this.loop()
      }


    /**
     * Met à jour l'environement selon les probabilités de générer des éléments indiqués lors de la construction.
     * La fréquence de mise à jour (en Hertz) détermine la vitesse à laquelle cette méthode est appelée.
     */
      async loop() {
        while (true) {
          //Tirage aléatoire d'une pièce du manoir
            let x = Math.floor(Math.random() * this.width), y = Math.floor(Math.random() * this.height)

          //Génération aléatoire de poussière dans la pièce tirée aléatoirement
            if ((!this.paused)&&(Math.random() <= this.dust)) this.create_dust({x, y})
          //Génération aléatoire de bijou dans la pièce tirée aléatoirement
            if ((!this.paused)&&(Math.random() <= this.jewel)) this.create_jewel({x, y})

          //Mise à jour du rendu et prochaine itération
            this.render()
            await sleep(1000/this.frequency)
        }
      }

    /**
     * Génère une poussière l'endroit indiqué.
     * @param {Position} position - Coordonnées
     * @param {Boolean} [info=false] - Indique s'il s'agit d'une génération manuelle
     */
      create_dust({x, y} = position, info = false) {
        if (!(this.map[x][y] & $.CELL.DUST)) {
          this.map[x][y] |= $.CELL.DUST
          this.scores.dust.generated()
          debug.environment[info ? "info" : "log"](`${x}:${y}` + $.DEBUG.DUST_GENERATED)
        }
      }

    /**
     * Génère un bijou à l'endroit indiqué.
     * @param {Position} position - Coordonnées
     * @param {Boolean} [info=false] - Indique s'il s'agit d'une génération manuelle
     */
      create_jewel({x, y} = position, info = false) {
        if (!(this.map[x][y] & $.CELL.JEWEL)) {
          this.map[x][y] |= $.CELL.JEWEL
          this.scores.jewel.generated()
          debug.environment[info ? "info" : "log"](`${x}:${y}` + $.DEBUG.JEWEL_GENERATED)
        }
      }

    /**
     * Retourne la mesure de performance de l'environement.
     * @return {Number} Contient la mesure de performance
     */
      get measure() {
        let {dust, jewel, energy} = this.scores.recent
        return dust.aspired * 8 - dust.generated * 2 + jewel.picked * 12 - jewel.aspired * 16 - jewel.generated - energy.used
      }

    /**
     * Evalue l'action d l'agent.
     * @param {Effector} action - Action effectuée
     */
      eval(action) {
        //Pièce dans laquelle se trouve l'agent
          let cell = this.map[this.agent.x][this.agent.y]

        //Prise en compte de l'action de l'agent
          debug.agent.log($.DEBUG[action])
          this.scores.energy.used()

        //Aspiration
          if (action === $.ACTION.ASPIRE) {
            if (cell === $.CELL.CLEAN) debug.environment.error($.DEBUG.ASPIRE_NOTHING)
            if (cell & $.CELL.JEWEL) debug.environment.error($.DEBUG.ASPIRE_JEWEL), this.scores.jewel.aspired()
            if (cell & $.CELL.DUST) debug.environment.success($.DEBUG.ASPIRE_DUST), this.scores.dust.aspired()
          }

        //Rammassage
          if (action === $.ACTION.PICKUP) {
            if (cell & $.CELL.JEWEL) debug.environment.success($.DEBUG.PICKUP_JEWEL), this.scores.jewel.picked()
            else debug.environment.error($.DEBUG.PICKUP_NOTHING)
          }
      }

    /**
     * Réinitialise l'environement.
     */
      clear() {
        for (let x = 0; x < this.width; x++) {
          this.map[x] = []
          for (let y = 0; y < this.height; y++)
            this.map[x][y] = $.CELL.CLEAN
        }
      }

    /**
     * Met à jour le rendu de l'environement.
     */
      render() {
        this.renderer.render(this)
      }

    /**
     * Propriété qui permet de gérer l'état de l'envionnement.
     * Si celui-ci est en pause, il ne générera plus aucun objet.
     */
      get paused() { return this._paused }
      set paused(paused) { this._paused = paused ; debug.environment.info(this.paused ? $.DEBUG.PAUSED : $.DEBUG.UNPAUSED) }

  }

//Alias pour la classe Scores
  Environment.scores = Scores
