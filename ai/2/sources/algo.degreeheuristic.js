/** 
 * MRV + Degree heuristic.
 * 
 * Trie les variables non assignées par nombre de contraintes sur ses voisins non assignées.
 */      
  function dh(assignment, unassigned) {
    //Initialisation
      let map = {}
      unassigned.forEach(variable => map[variable] = 0)

    //Parcours des variables non assignées et comptage des contraintes de chaque variable sur ses voisins non assignés
      for (let variable of unassigned)
        map[variable] = [...assignment.csp.constraints_map.get(variable)].filter(({a, b}) => assignment.unassigned.has(a)&&assignment.unassigned.has(b)).length
        
    return sortByMappedValueReversed(map)
  }