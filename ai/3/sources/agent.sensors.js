/**
 * Capteur.
 */
  class Sensor {
    /**
     * Initialise l'environement dans lequel le capteur fonctionne.
     * @param {Environment} environment - Environement dans lequel le capteur fonctionne.
     */
      init(environment) {
        this.environment = environment
      }
  }

/**
* Capteur de données.
*/
  class EnvironmentSensor extends Sensor {
    /**
     * Retourne les données de l'environement dans lequel se trouve l'agent.
     * @return {Set}
     */
      data() {
        return new Set(this.environment.map[this.environment.agent.x][this.environment.agent.y])
      }
  }

/**
* Capteur de position.
*/
  class PositionSensor extends Sensor {
    /**
     * Retourne les données de la position de l'agent dans l'environment.
     * @return {Position}
     */
      data() {
        return {x:this.environment.agent.x, y:this.environment.agent.y}
      }
  }