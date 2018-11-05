/** 
 * Problème à satisfaction de contraintes (CSP).
 * @abstract
 */
  class ConstraintSatisfactionProblem {

    /** 
     * Constructeur. 
     */
      constructor() {
        //Variables 
          this.variables = new Map()

        //Domaine de valeurs des variables
          this.domains = new Map()

        //Voisinage de chaque variable
          this.neighborhoods = new Map()

        //Contraintes
          this.constraints = []
          this.constraints_map = new Map() 

        //Utilisé pour gérer l'état de la résolution du problème (pour l'interface utilisateur)
          this.solving = new ConstraintSatisfactionProblem.SolvingState()
      }

    /** 
     * Cherche une solution au CSP avec l'algorithme passé en paramètre.
     * Note : Seul la fonction 'backtracking' peut être utilisée en tant que premier paramètre
     * 
     * Le chronomètre est également démarré.
     */
      async solve(algorithm, options) {
        //Initialisation
          let solution = null
          this.solving.start()

        //Résolution selon l'algorithme spécifié en paramètre
          try { 
            solution = await algorithm(this, this.solver = new Assignment(this, options)) 
            this.solving.end(this.chrono, solution)
            if (!solution) this.solver = null
            await rendering()
          } 
        //Arrêt utilisateur
          catch (e) { 
            this.solving.cancel(this.chrono)
            rendering.render()
          }
          
        return solution
      }

    /** 
     * Génère la map des contraintes.
     * Celle-ci regroupe les contraintes d'une même variable. 
     * 
     * Le contenu est équivalent à csp.constraints, mais permet de les filtrer par variable pour économiser du temps de calcul
     */
      compute_constraints_map() {
        //Parcours des variables
          for (let [variable] of this.variables) { 
            //Initialisation
              let constraints = new Set()
              this.constraints_map.set(variable, constraints)
            //Parcours des contraintes à la recherche des contraintes liées à la variable
              for (let constraint of this.constraints)
                if ((constraint.a === variable)||(constraint.b === variable)) constraints.add(constraint)
          }
      }

    /** 
     * Calcule le voisinage de chaque variable.
     * i.e. les variables avec lesquelles une varible possède au moins une contrainte.
     */
      compute_neighborhoods() {
        //Parcours des variables
          for (let [variable] of this.variables) { 
            //Initialisation
              let neighborhood = new Set()
              this.neighborhoods.set(variable, neighborhood)
            //Ajout du voisinage
              this.constraints_map.get(variable).forEach(constraint => (neighborhood.add(constraint.a), neighborhood.add(constraint.b)))
              neighborhood.delete(variable)
          }
      }
      

    /** 
     * Temps écoulé depuis le début de la résolution.
     */
      get chrono() {
        return Number.isFinite(this.solving.started) ? performance.now() - this.solving.started : 0
      }
  }

//Classe permettant de gérer l'état de résolution d'un CSP
  ConstraintSatisfactionProblem.SolvingState = ConstraintSatisfactionProblemSolvingState