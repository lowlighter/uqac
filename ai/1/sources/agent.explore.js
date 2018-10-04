function AgentExplore() {
  //Croyances
    let $ = {AGENT:{THREAD_INIT:"INIT"}} ; $.WIDTH = 0 ; $.HEIGHT = 0
    let beliefs = null
    let desires = null

  //Récepteur du thread
    onmessage = function (message) {
      //Initialisation du thread
        if ($.AGENT.THREAD_INIT === message.data[0]) init(message)

      //Exploration
        if ($.AGENT.EXPLORATION_START === message.data[0]) {
          //Tentative de recherche de solution
            try {
              beliefs = message.data[1] ; $.WIDTH = beliefs.map.length ; $.HEIGHT = (beliefs.map[0]||[]).length
              desires = message.data[2]
              let plan = (message.data[3] === $.AGENT.INFORMED ? InformedExploration(message.data[4]) : UninformedExploration(message.data[4]))
              postMessage([$.AGENT.EXPLORATION_DONE, plan])
            }
          //Echec
            catch (e) { postMessage([e]) }
        }
    }

//=========================================================================================================
//=========================================================================================================
// EXPLORATION NON INFORMÉE
//=========================================================================================================

  /**
   * Exploration non informée.
   * @param {Number} timeout - Temps accordé (en secondes)
   * @return {Array<$.ACTION>} Plan d'action
   */
    function UninformedExploration(timeout) {
      let node = IterativeDeepeningSearch({cell:beliefs.map[beliefs.position.x][beliefs.position.y], position:beliefs.position}, timeout)
      return ReconstructPlan(node)
    }

  /**
   * Parcours itératif en profondeur.
   * @param {Object} data - Données du noeud
   * @param {Number} timeout - Temps accordé (en secondes) pour parcourir l'arbre
   * @return {Node|null} Un noeud cible si trouvé, ou null le cas échéant
   */
    function IterativeDeepeningSearch(data, timeout) {
      //Racine
        let root = {data, parent:null, action:$.ACTION.NONE}
      //Valeur de timeout
        timeout = timeout*1000 + performance.now()

      //Recherche d'un noeud cible
        for (let depth = 0; depth < Infinity; depth++) {
          postMessage([$.AGENT.EXPLORATION_INFO_DEPTH, depth])
          let node = DepthLimitedSearch(root, depth, timeout)
          if (node) return node
        }

      //Echec
        return null
    }

  /**
   * Parcours en profondeur limité.
   * @param {Node} node - Noeud de départ
   * @param {Number} limit - Profondeur maximale
   * @param {Number} timeout - Temps accordé (en secondes) pour parcourir l'arbre
   * @return {Node|null} Un noeud cible si trouvé, ou null le cas échéant
   */
    function DepthLimitedSearch(node, limit, timeout) {
      //Test de noeud cible
        if (IsGoal(node)) return node

      //Limite atteinte
        if (limit <= 0) return null
        if (timeout < performance.now()) throw $.AGENT.EXPLORATION_TIMEOUT

      //Récursion sur les enfants
        let children = Expand(node)
        for (let child of children) {
          let nnode = DepthLimitedSearch(child, limit-1, timeout)
          if (nnode) return nnode
        }

      //Echec
        return null
    }

//=========================================================================================================
//=========================================================================================================
// EXPLORATION INFORMÉE
//=========================================================================================================

   /**
    * Exploration informée.
    * @param {Number} timeout - Temps accordé (en secondes)
    * @return {Array<$.ACTION>} Plan d'action
    */
     function InformedExploration(timeout) {
       let node = AStar({cell:beliefs.map[beliefs.position.x][beliefs.position.y], position:beliefs.position}, timeout)
       return ReconstructPlan(node)
     }

  /**
   * Algorithme A*.
   * @param {Object} data - Données du noeud
   * @param {Number} timeout - Temps accordé (en secondes) pour parcourir l'arbre
   * @return {Node|null} Un noeud cible si trouvé, ou null le cas échéant
   */
    function AStar(data, timeout) {
      //Racine
        let root = {data, action:$.ACTION.NONE}
      //Valeur de timeout
        timeout = timeout*1000 + performance.now()

      //Fonctions utilitaires (identifieur, reconstructeur, coût, heurisitique, score)
        let {id, rebuild, cost, heuristic, heuristics, score, sort} = AStar.f

      //Frontière (permet d'organiser les noeuds les plus désirables) et scores
        let boundary = new Map([[id(root), 0]])
        let scores = new Map([[id(root), {score:0, from:null}]])


      //Listes ouverte et fermée
        let open = [id(root)]
        let closed = new Set()

      //Contexte des fonctions utilitaires
        heuristic = heuristic.bind(null, heuristics(root))
        rebuild = rebuild.bind(null, scores, id)
        score = score.bind(null, scores, id)
        sort = sort.bind(null, open, boundary)

      //Parcours de la liste ouverte
        while (open.length) {
          //Récupération du noeud le plus désirable et ajout dans la liste fermée
            let node = (sort(), open.shift()) ; closed.add(id(node))
            node = rebuild(node)

          //Limite atteinte
            if (timeout < performance.now()) throw $.AGENT.EXPLORATION_TIMEOUT

          //Parcours des noeuds enfants (i.e. noeuds voisins)
            Expand(node, true).forEach(child => {
              //Traitement si la liste fermée ne contient pas l'élément
                if (!closed.has(id(child))) {
                  //Calcul du nouveau score
                    let nscore = score(node, 1) + cost(node, child)

                  //On met à jour le score si celui-ci est meilleur que celui qui a été précédemmment trouvé
                  //Si le noeud n'a pas encore été decouvert (i.e. il n'est pas dans les scores) on considère son score comme étant infini
                    if (open.has(id(child))) {
                      open.push(id(child))
                      boundary.set(id(child), nscore + heuristic(child))
                    }

                    if (nscore < score(child)) {
                      scores.set(id(child), {score:nscore, from:node})
                    }
                }
            })

          //Test de noeud cible
            if (IsGoal(node)) return node

          //Debug
            postMessage([$.AGENT.EXPLORATION_INFO_CLOSED, closed.size])
        }

      //Echec
        return null
    }

  /**
   * Fonctions utilitaires.
   */
    AStar.f = {
      //Fonction d'identification
        id:(node) => { return typeof node === "object" ? JSON.stringify(node) : node },
      //Fonction de reconstruction
        rebuild:(scores, id, node) => { node = JSON.parse(node) ; node.parent = scores.get(id(node)).from ; return node },
      //Score
        score:(scores, id, node, current) => { return scores.has(id(node)) ? scores.get(id(node)).score : (current ? 0 : Infinity) },
      //Tri de la liste ouverte à partir de la frontière pour obtenir les noeuds les plus désirables en premier
        sort:(open, boundary) => { open.sort((a, b) => boundary.get(a) - boundary.get(b)) },
      //Fonction de coût
        cost:(node, next) => { return 1 },
      //Distance de manhattan
        manhattan:(a, b) => { return Math.abs(b.y - a.y) + Math.abs(b.x - a.x) },
      //Heuristique
        heuristic:(hmap, node) => { return hmap[node.data.position.x][node.data.position.y] },
      //Calcule les heurisitiques
        heuristics:(node) => {
          let hmap = []
          for (let x = 0; x < $.WIDTH; x++) {
            hmap[x] = []
            for (let y = 0; y < $.HEIGHT; y ++) hmap[x][y] = AStar.f.manhattan(node.data.position, {x, y})
          }
          return hmap
        }
    }


//=========================================================================================================
//=========================================================================================================
// UTILITAIRES D'EXPLORATIONS
//=========================================================================================================

   /**
    * Reconstruit le plan à suivre à partir d'un noeud cible.
    * @param {Node} node - Noeud cible
    * @return {Array<$.ACTION>} Liste des identifiants d'actions à réaliser pour atteindre le noeud cible en partant du noeud racine
    */
     function ReconstructPlan(node) {
       //Echec de l'exploration
        if (!node) return [$.ACTION.NONE]

       //Parcours des noeuds jusqu'à atteindre la racine
         let actions = []
         do { actions.unshift(node.action) } while (node = node.parent)

       //Retour du plan (le noeud racine est ignoré, celui-ci ne possédant pas d'action)
         return actions.slice(1)
     }

  /**
   * Indique si le noeud passé en paramètre est un noeud cible.
   * @param {Node} node - Noeud à tester
   * @return {Boolean} Vrai s'il s'agit d'un noeud cible
   */
    function IsGoal(node) {
      //Si l'agent a au moins un désir, le noeud racine est invalidée car il n'effectue aucune action
        if ((desires)&&(desires.length)&&(!node.parent)) return false

      //Parcours des désirs ([état, action])
        for (let desire of desires) {
          if ((node.parent.data.cell & desire[0])&&(node.action === desire[1])) return true
        }

      //Aucun desir n'a pu être accompli via la succession actuelle d'action
        return false
    }

  /**
   * Fonction d'expansion.
   * @param {Node} node - Noeud à étendre
   * @param {Boolean} noparent - Supprime le noeud parent (e.g. A* qui conserve une liste interne des parents)
   * @return {Array<Node>} Liste des enfants du noeud
   */
    function Expand(node, noparent = false) {
      //Initialisation
        let x = node.data.position.x, y = node.data.position.y, children = [], parent = node.parent||{action:NaN}, child = null
        let apply = Expand.apply.bind(null, children, node)

      //Rammasage
        if (node.data.cell & $.CELL.JEWEL) {
          child = apply($.ACTION.PICKUP)
          child.data.cell &= ~$.CELL.JEWEL
        }

      //Aspiration
        if (node.data.cell & $.CELL.DUST) {
          child = apply($.ACTION.ASPIRE)
          child.data.cell = $.CELL.CLEAN
        }

      //Déplacement (les noeuds qui consistent à retourner en arrière (i.e. deux déplacements dans le sens opposé) ne sont pas permis)
        //Haut
          if (y - 1 >= 0) {
            child = apply($.ACTION.UP)
            child.data.position.y--
            child.data.cell = beliefs.map[child.data.position.x][child.data.position.y]
          }

        //Droite
          if (x + 1 < $.WIDTH) {
            child = apply($.ACTION.RIGHT)
            child.data.position.x++
            child.data.cell = beliefs.map[child.data.position.x][child.data.position.y]
          }

        //Bas
          if (y + 1 < $.HEIGHT) {
            child = apply($.ACTION.DOWN)
            child.data.position.y++
            child.data.cell = beliefs.map[child.data.position.x][child.data.position.y]
          }

        //Gauche
          if (x - 1 >= 0) {
            child = apply($.ACTION.LEFT)
            child.data.position.x--
            child.data.cell = beliefs.map[child.data.position.x][child.data.position.y]
          }

        //Suppression des parents si demandé
          if (noparent) children.map(child => delete child.parent)

      return children
    }

  /**
   * Crée un noeud enfant en clonant les données du parent.
   * Incrémente automatiquement la profondeur puis référence le parent et l'action.
   * @param {Array<>} children - (paramètre à lier) Référence vers le tableau d'enfant du noeud éténdu
   * @param {Node} node - (paramètre à lier) Noeud à étendre
   * @param {$.ACTION} action - Action effectuée
   * @return {Node}
   */
    Expand.apply = function (children, node, action) {
      let child = {data:{cell:node.data.cell, position:{x:node.data.position.x, y:node.data.position.y}}, parent:node, action}
      children.push(child)
      return child
    }

  /**
   * Cette fonction permet d'initialiser le thread.
   * Elle permet de contourner les restrictions de certains navigateurs et permet de faire fonctionner le code en loca.
   * @param {Message} message - Message d'initialisation du thread
   */
    function init(message) {
      //Récupération du contexte et des variables globales
        $ = JSON.parse(message.data[1])
      //Récupération des fonctions utilitaires
        for (let f of $.FUNCTIONS) eval(f.replace(/function ([a-z]+)/i, "self.$1 = function $1"))
      //Méthode supplémentaires pour les tableaux
        Array.prototype.add = function (...x) { this.push(...x) }
        Array.prototype.has = function (x) { return this.indexOf(x) === -1 }
    }
}
