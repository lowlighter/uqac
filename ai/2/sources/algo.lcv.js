/** 
 * Least Constraining Value.
 * 
 * Trie le domaine de valeur d'une variable afin selon celles qui autorisent le plus de mouvements légaux par la suite.
 * e.g. : [7 (24 mouvements légaux restant pour les voisins), ..., 1 (1 mouvement légal restant pour les voisins)]
 *
 * Note : Cette fonction modifie directement le domaine de variable et n'a donc pas de valeur de retour
 */
  function lcv(assignment, variable) {
    //Initialisation
      let map = {}
      assignment.domains.get(variable).forEach(value => map[value] = 0)

    //Parcours des valeurs du domaine de la variable
      for (let value of assignment.domains.get(variable)) {
        //Ajout temporaire de {variable, value}
          assignment.add({variable, value})
        //Comptage du nombre de valeurs légales sur les autres variables non assignées
          for (let other of assignment.unassigned)
            map[value] += assignment.legal(other)
        //Suppression de l'ajout temporaire de {variable, value}
          assignment.delete({variable, value})
      }

    //Mise à jour du domaine de la variable en les triant par ceux qui autorisent le plus de mouvements légaux
      assignment.domains.set(variable, sortByMappedValueReversed(map))
  }

