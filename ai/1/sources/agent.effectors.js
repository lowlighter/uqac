/**
 * Effecteur.
 */
  class Effector {
    /**
     * Initialise l'environement dans lequel l'effecteur fonctionne.
     * @param {Environment} environment - Environement dans lequel l'effecteur fonctionne.
     */
      init(environment) {
        this.environment = environment
      }

    /** Référence vers l'agent. **/
      get agent() {
        return this.environment.agent
      }
  }

/**
 * Effecteur d'aspiration.
 */
  class AspireEffector extends Effector {
    /**
     * Aspire la poussière et/ou les bijoux de la case où se trouve l'agent.
     */
      async enable() {
        this.agent.action = $.ACTION.PENDING
        await sleep(this.duration)
        this.environment.eval($.ACTION.ASPIRE)
        this.environment.map[this.agent.x][this.agent.y] = $.CELL.CLEAN
        this.agent.action = $.ACTION.DONE
      }

    /** Durée de l'action. */
      get duration() { return 500 }
  }

/**
 * Effecteur de ramassage de bijoux.
 */
  class PickupEffector extends Effector {
    /**
     * Ramasse le bijou de la case où se trouve l'agent.
     */
      async enable() {
        this.agent.action = $.ACTION.PENDING
        await sleep(this.duration)
        this.environment.eval($.ACTION.PICKUP)
        this.environment.map[this.agent.x][this.agent.y] &= ~$.CELL.JEWEL
        this.agent.action = $.ACTION.DONE
      }

    /** Durée de l'action. */
      get duration() { return 800 }
  }

/**
 * Effecteur de d'attente.
 */
  class WaitEffector extends Effector {
    /**
     * Attend un certain laps de temps.
     */
      async enable() {
        await sleep(this.duration)
      }

    /** Durée de l'action. */
      get duration() { return 1500 }
  }

/**
 * Effecteur de déplacement.
 */
  class MoveEffector extends Effector {
    /**
     * Déplacement vers le haut.
     */
      async up() {
        this.agent.action = $.ACTION.PENDING
        this.environment.eval($.ACTION.UP)
        this.agent.y = Math.max(0, this.agent.y - 1)
        this.agent.action = $.ACTION.DONE
      }

    /**
     * Déplacement vers le bas.
     */
      async down() {
        this.agent.action = $.ACTION.PENDING
        this.environment.eval($.ACTION.DOWN)
        this.agent.y = Math.min(this.agent.y + 1, this.environment.height - 1)
        this.agent.action = $.ACTION.DONE
      }

    /**
     * Déplacement vers la gauche.
     */
      async left() {
        this.agent.action = $.ACTION.PENDING
        this.environment.eval($.ACTION.LEFT)
        this.agent.x = Math.max(0, this.agent.x - 1)
        this.agent.action = $.ACTION.DONE
      }

    /**
     * Déplacement vers la droite.
     */
      async right() {
        this.agent.action = $.ACTION.PENDING
        this.environment.eval($.ACTION.RIGHT)
        this.agent.x = Math.min(this.agent.x + 1, this.environment.width - 1)
        this.agent.action = $.ACTION.DONE
      }
  }
