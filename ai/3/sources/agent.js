/**
 * Agent.
 */
class Agent {

  /**
   * Constructeur.
   * Génère les capteurs et les effecteurs associés à l'agent.
   */
    constructor() {
      //Capteurs
      this.sensors = {
        environment: new EnvironmentSensor(),
        position: new PositionSensor()
      }

      //Effecteurs
      this.effectors = {
        move: new MoveEffector(),
        shoot: new ShootEffector(),
        exit: new ExitEffector()
      }

      //Base de règles et de connaissances
      this.knowledge_base = new KnowledgeBase();
      this.rule_base = new RuleBase(this.knowledge_base, this);


      // BDI
      this.beliefs = { knowledge_base: this.knowledge_base };
      this.desires = { 'main goal': $.CELL.PORTAL };
      this.intentions = [];
    }

  /** 
   * Routine principale de l'agent. 
   */
    async routine() {

      //Mise à jour de l'état interne de l'agent
      this.update_state();

      // Selection du mouvement et execution de celui-ci
      this.intentions.push(this.select_move());
      let move = this.intentions.shift();
      await move(this);

    }


  /**
   * Met à jour l'état interne de l'agent.
   */
    update_state() {

      // Mise à jours ses croyances
      this.beliefs['position'] = this.sensors.position.data();
      this.beliefs['environment'] = this.sensors.environment.data();

      // Mise à jours de la base de connaissances à partir de la base de règles
      this.rule_base.rules.forEach(function (rule) {
        if (rule.premises()) {
          rule.conclusion();
        }
      });

    }


  /**
   * Selectionne la meilleure action parmis les actions possibles.
   * @returns {function} Retourne une action
   */
    select_move() {
      let actions = this.compute_valid_action();
      return this.compute_best_move(actions);
    }


  /**
   * Calcul tout les actions valide à partir de la position courante de l'agent.
   * @returns {Object} Retourne un objet contenant tout les mouvements valide pour l'agent
   */
    compute_valid_action() {

      let valid_actions = {};
      valid_actions['move'] = [];

      //Liste des actions possibles
      if (this.beliefs.position.x > 0) { valid_actions['move'].push($.ACTION.LEFT, $.ACTION.SHOOT_LEFT) }
      if (this.beliefs.position.y > 0) { valid_actions['move'].push($.ACTION.UP, $.ACTION.SHOOT_UP) }
      if (this.beliefs.position.y + 1 < this.sensors.environment.environment.height) { valid_actions['move'].push($.ACTION.DOWN, $.ACTION.SHOOT_DOWN) }
      if (this.beliefs.position.x + 1 < this.sensors.environment.environment.width) { valid_actions['move'].push($.ACTION.RIGHT, $.ACTION.SHOOT_RIGHT) }
      if (this.knowledge_base.facts['exit']) { valid_actions['exit'] = $.ACTION.EXIT; }

      return valid_actions;
    }

  /**
   * Calcule la meilleure action parmis les actions possibles.
   * @param valid_actions Mouvements valide pour l'agent
   * @returns {function} Retourne une action
   */
    compute_best_move(valid_actions) {

      // Si il y a un portail, on sort du niveau
      if (valid_actions['exit']) { return $.EFFECTOR[valid_actions['exit']] }

      // Sinon on calcul le meilleur mouvement possible à partir de notre base de connaissance
      let scores = valid_actions['move'].map((val) => {

        //Utilité des déplacements
        if ($.ACTION.MOVE & val) {
          return [...this.knowledge_base.facts[[this.x + $.TRANSLATION[val][0], this.y + $.TRANSLATION[val][1]]]].reduce(function (acc, curr) {
            return acc + $.VALUE[curr];
          }, 0);
        }

        //Utilité des tirs
        if ($.ACTION.SHOOT & val) {
          return [...this.knowledge_base.facts[[this.x + $.TRANSLATION[val][0], this.y + $.TRANSLATION[val][1]]]].reduce(function (acc, curr) {
            return acc + $.VALUE_SHOOT[curr];
          }, $.VALUE_SHOOT.BASE);
        }

      });

      
      let max = Math.max(...scores)
      let best_moves = valid_actions['move'].filter(function (val, index) {
        return scores[index] == max;
      });

      // Si il y a deux choix équivalent, on tire au hasard
      return $.EFFECTOR[best_moves[Math.floor(Math.random() * best_moves.length)]];
    }

}