/**
 * Agent.
 */
  class Agent {

    /**
     * Constructeur.
     * Génère les capteurs et les effecteurs associés à l'agent.
     * @param {Object} options - Options (voir ci-dessous)
     * @param {Number} options.frequency - Fréquence d'itération (en Hertz)
     * @param {Object} options.exploration - Options de l'exploration
     * @param {Number} options.exploration.timeout - Temps maximal alloué à l'exploration
     * @param {Object} options.learning - Options de l'aprrentissage
     * @param {Object} options.learning.factor - Facteur d'apprentissage
     * @param {Object} options.learning.history - Nombre d'entrées conservées dans l'historique de l'apprentissage
     */
      constructor({frequency, exploration, learning, boot} = options) {
        //Propriétés de l'agent
          this.frequency = frequency
          this.exploration = exploration
          this.alive = true
          this._mode = $.AGENT.INFORMED
          this.learning = {enabled:false, ratio:1.00, factor:learning.factor, history:learning.history, scores:[], episode:{plan:[], measure:0}}

        //Capteurs
          this.sensors = {
            environment:new EnvironmentSensor(),
            position:new PositionSensor()
          }

        //Effecteurs
          this.effectors = {
            aspire:new AspireEffector(),
            pickup:new PickupEffector(),
            move:new MoveEffector(),
            wait:new WaitEffector()
          }

        //Action
          this.beliefs = null
          this.desires = []
          this.intentions = []

        //Thread reservé à l'exploration de l'agent
          this.thread = new Thread($.AGENT.THREAD.SOURCE)
          this.thread.postMessage([$.AGENT.THREAD_INIT, $.AGENT.THREAD.CONTEXT])
      }

    /**
     * Boucle principale de l'agent.
     * La fréquence de mise à jour (en Hertz) détermine la vitesse à laquelle cette méthode est appelée.
     * La boucle principale de l'agent sera démarée après que les capteurs et les effecteurs soient initialisés avec l'environement.
     */
      async loop() {
        while (this.alive) {
          //Si aucun plan d'action, exploration afin de déterminer un nouveau plan d'action
            if (!this.intentions.length) {
              //Mise à jour des croyances et des désirs
                this.beliefs = {map:this.sensors.environment.data(), position:this.sensors.position.data()}
                this.desires = [[$.CELL.JEWEL, $.ACTION.PICKUP], [$.CELL.DUST, $.ACTION.ASPIRE]]
              //Exploration
                await this.explore()
            }

          //Exécution de la prochaine action planfiée
            let action = this.intentions.shift()
            if (action) await action()

          //Apprentissage (si activé)
            if (this.learning.enabled) await this.learn()

          //Prochaine itération
            await sleep(1000/this.frequency)
        }
      }

    /**
     * Fonction d'exploration.
     * L'exploration est défini dans le fichier agent.explore.js.
     * La fonction ci-dessous sert à encapsuler les résultats de l'exploration.
     */
      explore() {
        //Exploration
          this.thread.postMessage([$.AGENT.EXPLORATION_START, this.beliefs, this.desires, this.mode, this.exploration.timeout])
          debug.agent.intentions(null)
          return new Promise(solve => { this.thread.onmessage = (message) => {
            //Exploration terminée
              if ($.AGENT.EXPLORATION_DONE === message.data[0]) {
                //Mise à jour des intentions
                  this.intentions = message.data[1].map(action => $.EFFECTOR[action].bind(null, this))
                //Notifie la fonction d'apprentissage
                  this.learn($.AGENT.START_LEARNING)
                //Debug
                  if (this.intentions.length) debug.agent.success($.DEBUG.EXPLORATION_DONE)
                  debug.agent.intentions(message.data[1])
                  debug.agent.mode()
                solve()
              }

            //Info sur l'exploration
              if (($.AGENT.EXPLORATION_INFO_DEPTH === message.data[0])||($.AGENT.EXPLORATION_INFO_CLOSED === message.data[0])) debug.agent.iexplore(message.data[0], message.data[1])
            //Exploration échouée (impossible de trouver une solution dans le temps imparti)
              if ($.AGENT.EXPLORATION_TIMEOUT === message.data[0]) (debug.agent.error($.DEBUG.EXPLORATION_TIMEOUT), solve())
            //Exploration échouée (impossible de trouver une solution)
              if ($.AGENT.EXPLORATION_FAILED === message.data[0]) (debug.agent.error($.DEBUG.EXPLORATION_FAILED), solve())
          }})
      }

    /**
     * Propriété qui permet de gérer le mode d'exploration de l'agent.
     */
      get mode() { return this._mode }
      set mode(mode) { this._mode = mode ; this._mode_changed = true }

    /**
     * Fonction d'apprentissage.
     * A chaque début d'épisode, cette fonction choisit un ratio de nombre d'action à effectuer par rapport au plan d'action global.
     * Celui-ci dépend du facteur d'apprentissage (qui est réduit au cours du temps) ainsi que des résultats précédents.
     * Le ratio le plus prometteur est récupéré puis une légère variation aléatoire est appliquée.
     * La variation de score entre le début et la fin de l'épisode est mesurée, puis enregistrée dans la liste des résultats, et ainsi de suite.
     */
      async learn(action = $.AGENT.CONTINUE_LEARNING) {
        //Nouvel épisode d'apprentissage
          if ($.AGENT.START_LEARNING === action) {
            //Arrêt prématuré s'il y a moins de trois actions dans le plan d'action (évite les boucles infinies)
              if (this.intentions.length < 3) return
            //Copie du plan d'action initial (celui-ci étant modifié à chaque itération du cycle de vie)
              this.learning.episode.plan = Array.from(this.intentions)
            //Mesure initial du score
              this.learning.episode.measure = agent.sensors.environment.environment.measure
            //Nouvel épisode d'apprentissage
              this.learning.episode.started = true
              debug.agent.log($.DEBUG.LEARNING_EPISODE_START+this.learning.ratio.toFixed(2))
          }

        //Continuité de l'épisode d'apprentissage actuel
          if (($.AGENT.CONTINUE_LEARNING === action)&&(this.learning.episode.started)) {
            //Nombre d'actions planifiées et nombre d'action exécutées
              let planned = this.learning.episode.plan.length
              let executed = planned - this.intentions.length
              let ratio = executed/planned

            //Si l'épisode d'apprentissage est terminé (i.e. le ratio est supérieur ou égale à celui que l'on voulait tester)
              if (ratio >= this.learning.ratio) {
                //Score (mesure actuel moins mesure initiale)
                  ratio = ratio.toFixed(2)
                  let score = agent.sensors.environment.environment.measure - this.learning.episode.measure
                  this.learning.scores.push({ratio, score})
                  if (this.learning.scores.length > this.learning.history) this.learning.scores.shift()
                  this.learning.episode.started = false
                  debug.agent.success($.DEBUG.LEARNING_EPISODE_DONE+`${ratio} => ${score}`)

                //Calcul des scores moyens pour chaque ratio enregistré
                  let scores = new Map()
                  this.learning.scores.forEach(v => scores.has(v.ratio) ? scores.get(v.ratio).push(v.score) : scores.set(v.ratio, [v.score]))
                  scores.forEach((v, k) => scores.set(k, v.reduce((a, b) => a + b, 0)/v.length))

                //Tri des ratio selon leur performances et récupère le premier élément
                  let best = +[...scores].sort((a, b) => a[1] - b[1])[0][0]

                //Calcul d'une variation de la meilleur performance
                  let delta = this.learning.factor * ((1 - best) * Math.random() - best * Math.random())
                  this.learning.factor *= 0.98
                  this.learning.ratio = +Math.clamp(0, best + delta , 1).toFixed(2)

                //Force l'exploration en supprimant le plan d'action actuel
                  this.intentions = []
              }
          }
      }

  }
