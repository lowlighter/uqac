/**
 * Backtracking.
 * Implémente le retour sur trace de façon récursive.
 * 
 * L'algorithme est légèrement modifié afin que la récursion dépende de l'attribut 'deadend' de l'assignement.
 * Celui-ci arrête prématurément la récursion si AC3 est activée et indique que l'assignement ne pourra être complet.
 */
  async function backtracking(csp, assignment) {
    //Affichage graphique
      await rendering(rendering.step)

    //Solution si l'assignement est complet
      if (assignment.complete) return assignment

    //Sinon, récupération d'une variable non assignée
      let variable = assignment.select.unassigned

    //Parcours des valeurs du domaine de chaque variable
      for (let value of assignment.domains.get(variable)) {
        //Test de consistance
          if (assignment.consistant({variable, value})) {
            //Ajout de {variable, valeur}
              assignment.add({variable, value})

            //Récursion (si AC3 est activé)
              if ((assignment.options.ac3)&&(AC3(csp, assignment).deadend)) {
                assignment.delete({variable, value})
                return null
              }
            //Récursion (classique)
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