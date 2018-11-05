/**
 * Backtracking.
 * Implémente le retour sur trace de façon récursive.
 * 
 * L'algorithme est légèrement modifié afin que la récursion soit arrêtée prématurément si AC3 est activée, 
 *  et qu'elle indique que l'assignement ne pourra être complet.
 */
  async function backtracking(csp, assignment) {
    //Affichage graphique
      await rendering(rendering.step)

    //Solution si l'assignement est complet
      if (assignment.complete) return assignment

    //Sinon, récupération d'une variable non assignée
      let variable = assignment.select.unassigned()
      let domain = assignment.select.values(variable)
      
    //Parcours des valeurs du domaine de chaque variable
    //Note : La boucle "for (let value of domain)" a été substituée par l'implémentation ci-dessous (boucle "while")
    //  en raison de l'heuristique LCV qui perturbait l'itérateur à cause du changement sur l'ordre des valeurs du domaine. 
      while (domain.length) {
        //Sélection d'une valeur
          let value = assignment.select.value(variable, domain)

        //Test de consistance
          if (assignment.consistant({variable, value})) {
            //Ajout de {variable, valeur}
              assignment.add({variable, value})

            //Récursion (si AC3 est activé et qu'elle indique que l'assignement actuel ne pourra être complété)
              if ((assignment.options.ac3)&&(AC3(csp, assignment).deadend)) {
                assignment.delete({variable, value})
                return null
              }
            //Récursion
              else {
                let result = await backtracking(csp, assignment)
                if (result) return result
                assignment.delete({variable, value})
              }
          }
      }

    //Sans issue
      return null
  }