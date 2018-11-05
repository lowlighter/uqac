/** 
 * Least Constraining Value.
 * 
 * Trie le domaine de valeur d'une variable afin selon celles qui autorisent le plus de mouvements légaux par la suite.
 * e.g. : [7 (24 mouvements légaux restant pour les voisins), ..., 1 (1 mouvement légal restant pour les voisins)]
 *
 * Note : Cette fonction modifie directement le domaine de variable et n'a donc pas de valeur de retour
 */
  function lcv(assignment, variable, values) {
    //Initialisation
      let map = {}
      values.forEach(value => map[value] = 0)

    //Parcours des valeurs du domaine de la variable
      for (let value of values) {
        //Ajout temporaire de {variable, value}
          assignment.add({variable, value})
        //Comptage du nombre de valeurs légales sur les autres variables non assignées
          for (let other of assignment.unassigned)
            map[value] += assignment.legal(other)
        //Suppression de l'ajout temporaire de {variable, value}
          assignment.delete({variable, value})
      }

    //Mise à jour du domaine de la variable en les triant par ceux qui autorisent le plus de mouvements légaux
      let sorted = [...sortByMappedValueReversed(map)]
      values.sort((a, b) => sorted.indexOf(a) - sorted.indexOf(b))
  }

