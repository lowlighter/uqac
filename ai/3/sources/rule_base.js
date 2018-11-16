/**
 * Base de regles
 */

class RuleBase {
    
    /**
     * Constructeur.
     */
        constructor(knowledge_base, agent) {
            this.knowledge_base = knowledge_base;
            this.agent = agent;
            this.rules = [];
            
            /**
             * Liste des règles
             */
            this.rules.push(
                // Si la case contient un excrement, on l'ajoute dans notre base de connaissances
                new Rule(this, this.has_excrement, this.current_position_has_excrement),
                // Si la case est venteuse, on l'ajoute dans notre base de connaissances
                new Rule(this, this.is_windy, this.current_position_is_windy),
                // Si la case possède un monstre mort, on l'ajoute dans notre base de connaissances
                new Rule(this, this.has_dead_monster, this.current_position_has_dead_monster),
                // Si il y a un portail, il a une sortie
                new Rule(this, this.has_portal, this.exit_found),

                // Si la case n'était pas visité, alors on la marque comme visité et sans danger
                new Rule(this, this.first_visit, this.visited),

                // Si la case est venteuse, il y a un risque qu'il y est une crevasse
                new Rule(this, this.is_windy, this.hole_risk),
                // Si la case contient un excrement, il y a un risque qu'il y est un monstre
                new Rule(this, this.has_excrement, this.monster_risk),

                // Si la case ne contient pas d'excrement, alors les cases autours ne contienne pas de monstre
                new Rule(this, this.is_not_windy, this.no_hole),
                // Si la case n'est pas venteuse, alors les cases autours ne contienne pas de trou
                new Rule(this, this.has_not_excrement, this.no_monster),

                //S'il y a un monstre, il ne peut y avoir de crevasse
                new Rule(this, this.has_monster, this.current_position_has_no_hole),
                //S'il y a un monstre (mort), il ne peut y avoir de crevasse
                new Rule(this, this.has_dead_monster, this.current_position_has_no_hole),
                //S'il y a une crevasse, il ne peut y avoir de monstre
                new Rule(this, this.has_hole, this.current_position_has_no_monster),
                //Si l'agent a tiré sur cette case, il ne peut y avoir de monstre
                new Rule(this, this.has_been_shot, this.current_position_has_no_monster),
            )
            
        }


    /**
     * Premisses
     */

    // Verifie si c'est la première visite d'une case
    first_visit() {
        return !this.rule_base.agent.beliefs.knowledge_base.facts[[this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y]].has($.CELL.EXPLORED);
    }

    // Verifie si la case où l'agent se trouve est venteuse
    is_windy() {
        return this.rule_base.agent.beliefs.environment.has($.CELL.WIND);
    }

    // Verifie si la case où l'agent se trouve n'est pas venteuse
    is_not_windy() {
        return !this.rule_base.agent.beliefs.environment.has($.CELL.WIND);
    }

    // Verifie si la case où l'agent se trouve contient un excrement
    has_excrement() {
        return this.rule_base.agent.beliefs.environment.has($.CELL.EXCREMENT);
    }

    // Verifie si la case où l'agent se trouve ne contient pas d'excrement
    has_not_excrement() {
        return !this.rule_base.agent.beliefs.environment.has($.CELL.EXCREMENT);
    }

    // Verifie si la case où l'agent se trouve contient un portail
    has_portal() {
        return this.rule_base.agent.beliefs.environment.has($.CELL.PORTAL);
    }

    // Vérifie si la case où l'agent se trouve contient un cadavre de monstre
    has_dead_monster() {
        return this.rule_base.agent.beliefs.environment.has($.CELL.MONSTER_DEAD)
    }

    // Vérifie à partir des connaissances si la case a été tirée
    has_been_shot() {
        return this.rule_base.agent.beliefs.knowledge_base.facts[[this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y]].has($.CELL.SHOT)
    }

    // Vérifie à partir des connaissances s'il y a un monstre sur la case
    has_monster() {
        return this.rule_base.agent.beliefs.knowledge_base.facts[[this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y]].has($.CELL.MONSTER)
    }

    // Vérifie à partir des connaissances s'il y a un trou sur la case
    has_hole() {
        return this.rule_base.agent.beliefs.knowledge_base.facts[[this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y]].has($.CELL.HOLE)
    }



    /**
     * Conclusions
     */

    // Ajoute dans la base de connaissance un potentiel risque de crevasse pour les cases voisines de l'agent
    hole_risk() {
        for(let neighbor of this.rule_base.get_neighbors({x:this.rule_base.agent.beliefs.position.x, y: this.rule_base.agent.beliefs.position.y})){
            if(!this.rule_base.agent.beliefs.knowledge_base.facts[neighbor].has($.CELL.EXPLORED)) {
                this.rule_base.add_or_update_risk_level(neighbor, $.CELL.WIND);
            }
        }
    }

    // Retire de la base de connaissance un potentiel risque de crevasse pour les cases voisines de l'agent
    no_hole() {
        for(let neighbor of this.rule_base.get_neighbors({x:this.rule_base.agent.beliefs.position.x, y: this.rule_base.agent.beliefs.position.y})){
            this.rule_base.remove_risk_from_pos(neighbor, $.CELL.WIND);
        }
    }

    // Ajoute dans la base de connaissance un potentiel risque de monstre pour les cases voisines de l'agent
    monster_risk(){      
        for(let neighbor of this.rule_base.get_neighbors({x:this.rule_base.agent.beliefs.position.x, y: this.rule_base.agent.beliefs.position.y})){
            if(!this.rule_base.agent.beliefs.knowledge_base.facts[neighbor].has($.CELL.EXPLORED) && !this.rule_base.agent.beliefs.knowledge_base.facts[neighbor].has($.CELL.SHOT)) {
                this.rule_base.add_or_update_risk_level(neighbor, $.CELL.EXCREMENT);
            }
        }    
    }

    // Retire de la base de connaissance un potentiel risque de monstre pour les cases voisines de l'agent
    no_monster() {
        for(let neighbor of this.rule_base.get_neighbors({x:this.rule_base.agent.beliefs.position.x, y: this.rule_base.agent.beliefs.position.y})){
            this.rule_base.remove_risk_from_pos(neighbor, $.CELL.EXCREMENT);
        }  
    }

    //Ajoute dans la base de connaissance qu'un monstre est mort
    current_position_has_dead_monster() {
        this.rule_base.agent.beliefs.knowledge_base.add_fact_for_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.MONSTER_DEAD)
        this.rule_base.agent.beliefs.knowledge_base.remove_fact_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.MONSTER)
    }

    // Ajoute dans la base de connaissance de l'agent que une sortie a été trouvée
    exit_found() {
        this.rule_base.agent.beliefs.knowledge_base.facts['exit'] = true;
    }
    
    // Ajoute dans la base de connaissance que l'agent a visité la case
    visited(){
        this.rule_base.agent.beliefs.knowledge_base.add_fact_for_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.EXPLORED);
        this.rule_base.remove_risk_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.EXCREMENT);
        this.rule_base.remove_risk_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.WIND);
    }

    // On marque la case comme venteuse
    current_position_is_windy() {
        this.rule_base.agent.beliefs.knowledge_base.add_fact_for_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.WIND);
    }

    // On marque que la case contient un excrement
    current_position_has_excrement() {
        this.rule_base.agent.beliefs.knowledge_base.add_fact_for_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.EXCREMENT);
    }

    // On marque que la case n'a pas de trou
    current_position_has_no_hole() {
        this.rule_base.agent.beliefs.knowledge_base.remove_fact_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.HOLE)
        this.rule_base.remove_risk_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.WIND)
    }

    // On marque que la case n'a pas de monstre
    current_position_has_no_monster() {
        this.rule_base.agent.beliefs.knowledge_base.remove_fact_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.MONSTER)
        this.rule_base.remove_risk_from_pos([this.rule_base.agent.beliefs.position.x, this.rule_base.agent.beliefs.position.y], $.CELL.EXCREMENT)
    }

    /**
     * Fonctions utiitaires
     */

    /**
     * Ajoute ou met à jour le niveau de risque.
     * @param {*} pos 
     * @param {*} mark 
     */
        add_or_update_risk_level(pos, mark) {
            //Si l'agent sait qu'il y a un monstre ou une crevasse (parce qu'il est mort dessus)
            //Pas besoin d'évaluer les risques
            if (this.agent.beliefs.knowledge_base.facts[pos].has($.CELL.HOLE)|| this.agent.beliefs.knowledge_base.facts[pos].has($.CELL.MONSTER)) return
            
            let risk_indicators = 0;
            let neighbors = this.get_neighbors({x:pos[0], y:pos[1]});
            for(let neighbor of neighbors){
                if (this.agent.beliefs.knowledge_base.facts[neighbor].has($.CELL.HOLE)|| this.agent.beliefs.knowledge_base.facts[neighbor].has($.CELL.MONSTER)|| this.agent.beliefs.knowledge_base.facts[neighbor].has($.CELL.MONSTER_DEAD)) {
                    neighbor.remove = true
                    continue
                }

                if(this.agent.beliefs.knowledge_base.facts[neighbor].has(mark)){
                    risk_indicators++;
                }
            }
            neighbors = neighbors.filter(v => !v.remove)

            this.remove_risk_from_pos(pos, mark);
            this.agent.beliefs.knowledge_base.add_fact_for_pos(pos, $.RISK[mark][risk_indicators  + (3 - neighbors.length)]);
            
        }

     /**
      * Récupère les cellules voisines d'une position.
      * @param {[x, y]} pos 
      * @returns {Array<[x, y]>}
      */
        get_neighbors(pos){
            let neighbors = [];
            if(pos.x + 1 < this.agent.beliefs.knowledge_base.size) {neighbors.push([pos.x + 1, pos.y])}
            if(pos.x - 1 >= 0) {neighbors.push([pos.x - 1, pos.y])}
            if(pos.y + 1 < this.agent.beliefs.knowledge_base.size) {neighbors.push([pos.x, pos.y + 1])}
            if(pos.y - 1 >= 0) {neighbors.push([pos.x, pos.y - 1])}
            return neighbors; 
        }

    /**
     * Supprime les risques liées à une position.
     */
        remove_risk_from_pos(pos, risk_kind){
            for(let risk of $.RISK[risk_kind]) {
                this.agent.beliefs.knowledge_base.remove_fact_from_pos(pos, risk);
            }
        }
}