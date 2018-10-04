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
     */
      constructor({frequency, exploration, boot} = options) {
        //Propriétés de l'agent
          this.frequency = frequency
          this.exploration = exploration
          this.alive = true
          this._mode = $.AGENT.INFORMED

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
          //Apprentissage (si activé)
            if (this.learning) await this.learn()

          //Si aucun plan d'action, exploration afin de déterminer un nouveau plan d'action
            if (!this.intentions.length) {
              //Mise à jour des croyances et des désirs
                this.beliefs = {map:this.sensors.environment.data(), position:this.sensors.position.data()}
                this.desires = [[$.CELL.DUST, $.ACTION.ASPIRE], [$.CELL.JEWEL, $.ACTION.PICKUP]]
              //Exploration
                await this.explore()
            }

          //Exécution de la prochaine action planfiée
            let action = this.intentions.shift()
            if (action) await action()

          //Prochaine itération
            await sleep(1000/this.frequency)
        }
      }

    /**
     * Fonction d'exploration.
     */
      explore() {
        //Exploration (voir agent.explore.js, la fonction étant exécutée dans un thread supplémentaire)
          this.thread.postMessage([$.AGENT.EXPLORATION_START, this.beliefs, this.desires, this.mode, this.exploration.timeout])
          debug.agent.intentions(null)
          return new Promise(solve => { this.thread.onmessage = (message) => {
            //Exploration terminée
              if ($.AGENT.EXPLORATION_DONE === message.data[0]) {
                //Mise à jour des intentions
                  this.intentions = message.data[1].map(action => $.EFFECTOR[action].bind(null, this))
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
     * Fonction d'apprentissage
     */
      async learn() {
        //TODO
      }

  }
