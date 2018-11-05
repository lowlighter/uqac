/** 
 * Assignement.
 * 
 * Pour un côté plus pratique de l'implémentation, les instances d'assignements sont liées à un CSP.
 */ 
  class Assignment {

    /** 
     * Constructeur. 
     */
      constructor(csp, options = {}) {
        //Référence au CSP
          this.csp = csp

        //Variables de l'assignement (copie)
          this.variables = new Map(this.csp.variables)

        //Domaines de valeurs de chaque variable (copie)
          this.domains = new Map(this.csp.domains)

        //Variables non assignées (i.e. dont le domaine de valeurs n'est initialement pas vide)
          this.unassigned = new Set()
          this.variables.forEach((_, variable) => this.domains.get(variable).size ? this.unassigned.add(variable) : null)
        
        //Options de résolution
          this.options = options

        //Si le problème de base n'est pas consistant, on empêche toute tentative de résolution en vidant les domaines de variables
          if (!this.consistant()) this.domains.forEach(domain => domain.clear())
      }

    /**
     * Retourne la prochaine variable non assignée à utiliser, à partir des heuristiques actives.
     * MRV, Degree heuristic et LCV sont exécutés à ce moment.
     * 
     * Cette méthode s'appele de la façon suivante : 
     * let variable = assignment.select.unassigned
     */
      get select() {
        //Variables non assignées
          let unassigned = this.unassigned

        //Heuristiques (variables)
          if (this.options.basic) unassigned = basic(this, unassigned)
          if (this.options.mrv) unassigned = mrv(this, unassigned)
          if (this.options.dh) unassigned = dh(this, unassigned)
          let selected = [...unassigned][0]

        //Heuristiques (valeurs)
          if (this.options.lcv) lcv(this, selected)

        return {unassigned:selected}
      }

    /** 
     * Indique si aucune contrainte du CSP n'est violée.
     * Si un couple {variable, valeur} est précisé, teste si l'éventuel ajout de ce couple laisse le CSP consistant.
     */
      consistant({variable, value} = {}) {
        //Parcours des contraintes du CSP et test de contrainte
          let constraints = variable ? this.csp.constraints_map.get(variable) : this.csp.constraints
          for (let constraint of constraints)
            if (!this.test(constraint, {variable, value})) return false
          
        //Aucune contrainte violée donc consistant
          return true
      }

    /** 
     * Test si la contrainte 'constraint' est respectée. 
     * Il est possible de passer jusqu'a deux affectations {variable, valeur} pour tester la contrainte avec des valeurs spécifiques.
     */
      test(constraint, A = {}, B = {}) {
        //Récupération des valeurs des variables associées à la contrainte
          let a = this.variables.get(constraint.a)
          let b = this.variables.get(constraint.b)

        //Valeurs spécifiques (si précisées)
          if (A.variable === constraint.a) a = A.value
          if (A.variable === constraint.b) b = A.value
          if (B.variable === constraint.a) a = B.value
          if (B.variable === constraint.b) b = B.value

        //Vérification de la contrainte
          return !((constraint.op === "!=")&&(a === b))
      }

    /** 
     * Compte le nombre de valeur légales d'une variable. 
     */
      legal(variable) {
        //Parcours des valeurs du domaine de la variable et comptage des valeurs gardant la consistance de l'assignement
          let legal = 0
          for (let value of this.domains.get(variable)) 
            legal += this.consistant({variable, value})
        return legal
      }

    /** 
     * Ajoute l'affectation {variable, valeur}. 
     */
      add({variable, value}) {
        //Mise à jour de la variable
          this.variables.set(variable, value)
          this.unassigned.delete(variable)
      }

    /** 
     * Retire l'affectation {variable, valeur}. 
     */
      delete({variable, value}) {
        //Mise à jour de la variable
          this.variables.set(variable, NaN)
          this.unassigned.add(variable)
      }

    /** 
     * Assignement complet (i.e. il ne reste plus aucune variable non assignée). 
     */
      get complete() {
        return this.unassigned.size === 0
      }

  }