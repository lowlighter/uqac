/** 
 * Minimum Remaining Value.
 * 
 * Filtre les variables non assignées selon leur nombre de valeurs légales restantes.
 * e.g. : [0:0 (2)]
 */
  function mrv(assignment, unassigned) {
    //Initialisation
      let map = {}, min = Infinity
      unassigned.forEach(variable => map[variable] = 0)
    
    //Parcours des variables non assignées et comptage des valeurs légales
      for (let variable of unassigned) {
        let legal = assignment.legal(variable) 
        min = Math.min(legal, min)
        map[variable] = legal
      }
      
    //Filtre les variables possédant le minimum de valeurs légales restantes
      return filterByMappedValue(map, min)
  }
