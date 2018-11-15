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
 * Effecteur de déplacement.
 */
  class MoveEffector extends Effector {
    /**
     * Déplacement vers le haut.
     */
      async up() {
        this.environment.apply($.ACTION.UP, {dx:0, dy:-1})
      }

    /**
     * Déplacement vers le bas.
     */
      async down() {
        this.environment.apply($.ACTION.DOWN, {dx:0, dy:+1})
      }

    /**
     * Déplacement vers la gauche.
     */
      async left() {
        this.environment.apply($.ACTION.LEFT, {dx:-1, dy:0})
      }

    /**
     * Déplacement vers la droite.
     */
      async right() {
        this.environment.apply($.ACTION.RIGHT, {dx:+1, dy:0})
      }

  }

/**
 * Effecteur de tir.
 */
  class ShootEffector extends Effector {

    /**
     * Tir un rocher sur la case indiquée.
     */
      async shoot({dx, dy}) {
        this.environment.apply($.ACTION.SHOOT, {dx, dy})
      }

    /**
     * Tir un rocher sur la case à gauche.
     */
      async shoot_left() {
        return this.shoot({dx:-1, dy:0})
      }

    /**
     * Tir un rocher sur la case à droite.
     */
      async shoot_right() {
        return this.shoot({dx:+1, dy:0})
      }

    /**
     * Tir un rocher sur la case en haut.
     */
      async shoot_up() {
        return this.shoot({dx:0, dy:-1})
      }

    /**
     * Tir un rocher sur la case en bas.
     */
      async shoot_down() {
        return this.shoot({dx:0, dy:+1})
      }

  }

/**
 * Effecteur de sortie.
 */
class ExitEffector extends Effector {
    
    /**
     * Quitte le niveau si l'agent se trouve sur le portail.
     */
      async exit() {
        this.environment.apply($.ACTION.EXIT)
      }

  }