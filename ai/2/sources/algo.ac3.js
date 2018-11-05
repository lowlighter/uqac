/**
 * Arc Consistency #3 (AC3).
 * 
 * Met à jour les différents domaines de valeurs des variables en vérifiant leur consistance avec l'assignement.
 * Si une variable non assignée possède un domaine vide, la fonction indique que l'assignement ne pourra pas être complet.
 */
  function AC3(csp, assignment) {
    //Liste des arcs (copie des contraintes)
      let arcs = new Array(...csp.constraints)

    //Parcours de la liste des arcs
      while (arcs.length) {
        //Récupération de l'arc et de la variable à traiter
          let arc = arcs.shift(), variable = arc.a

        //Suppression des valeurs inconsistantes du domaine de 'a' selon les valeurs du domaine de 'b'
          if (AC3.RemoveInconsistentValues(arc, assignment)) {
            //Propagation du test de consistance et test du domaine
              csp.neighbors(variable).forEach(neighbor => arcs.push({a:neighbor, b:variable, op:arc.op}))
              if (assignment.domains.get(variable).size === 0) return {deadend:true}
          }
      }

    return {deadend:false}
  }

/** 
 * Fonction qui met à jour les domaines de valeurs d'une variable. 
 */
  AC3.RemoveInconsistentValues = function (arc, assignment) {
    //Initialisation
      let removed = false, {a, b} = arc

    //Parcours des valeurs de 'a' et 'b' afin de trouver 'vb' satisfaisant 'arc' pour (a = va, b = vb)
      a_values: for (let va of assignment.domains.get(a)) {
        b_values: for (let vb of assignment.domains.get(b)) 
          if (assignment.test(arc, {variable:a, value:va}, {variable:b, value:vb})) continue a_values
        
        //Aucune valeur de 'b' ne peut satisfaire 'arc' pour (a = va, b = vb)
          assignment.domains.delete(va)
          removed = true
      }
  
    return removed
  }