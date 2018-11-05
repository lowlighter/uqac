/** 
 * Least Constraining Value.
 * 
 * Trie les valeurs des variables non assignées selon les moins contraignantes.
 */
  function lcv(assignment, variable) {
    //Initialisation
      let map = {}
      assignment.domains.get(variable).forEach(value => map[value] = 0)

    //Parcours des valeurs du domaine de la variable
      for (let value of assignment.domains.get(variable)) {
        //Ajout temporaire de {variable, value}
          assignment.variables.set(variable, value)
          assignment.unassigned.delete(variable)
        //Comptage du nombre de valeurs légales sur les autres variables non assignées
          for (let other of assignment.unassigned)
            map[value] += assignment.legal(other)
        //Suppression de {variable, value}
          assignment.variables.set(variable, NaN)
          assignment.unassigned.add(variable)
      }

    //Mise à jour du domaine de la variable en les triant par ceux qui autorisent le plus de mouvements légaux
      assignment.domains.set(variable, sortByMappedValueReversed(map))
  }

