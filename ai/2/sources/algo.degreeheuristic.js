/** 
 * Degree heuristic.
 * 
 * Trie les variables non assignées par rapport au nombre de contraintes sur ses voisins non assignées.
 * Il peut être utilisé en tant que briseur d'égalité pour MRV.
 * e.g. [0:0 (32 contraintes), 2:3 (12 contraintes), ..., 7:2 (1 contrainte)]
 */      
  function dh(assignment, unassigned) {
    //Initialisation
      let map = {}
      unassigned.forEach(variable => map[variable] = 0)

    //Parcours des variables non assignées et comptage des contraintes de chaque variable sur ses voisins non assignés
      for (let variable of unassigned)
        map[variable] = [...assignment.csp.constraints_map.get(variable)].filter(({a, b}) => assignment.unassigned.has(a)&&assignment.unassigned.has(b)).length
        
    //Classe les variables selon le nombre de contraintes (en ordre décroissant)
      return sortByMappedValueReversed(map)
  }