/**
 * Base de connaissances
 */

class KnowledgeBase {
    
    /**
     * Constructeur.
     */
        constructor(facts) {
            this.facts = {};
            this.size = 3;
            this.init();
        }

    /**
     * Initialise une base de connaissance vierge.
     */
        init() {
            this.clear();
            for(let i=0; i<this.size; i++){
                for(let j=0; j<this.size; j++){
                    this.facts[[i,j]] = new Set();
                }
            }
            this.add_fact_for_pos([0,0], $.CELL.PREVIOUS_POSITION);
        }

    /**
     * Vérifie s'il il y a un fait pour une position dans la base de connaissances.
     * @param {Array} pos - position x et y de l'agent
     * @param {String} fact - fait dont on doit vérifié la présence
     */
        fact_in_position(pos, fact) {
            this.facts[pos].has(fact);
        }

    /**
     * Ajoute un fait à une position dans la base de connaissances.
     * @param {Array} pos - position x et y de l'agent
     * @param {String} fact - fait que l'on doit ajouter
     */
        add_fact_for_pos(pos, fact) {
            this.facts[pos].add(fact);
        }

    /**
     * Supprime un fait à une position dans la base de connaissances.
     * @param {Array} pos - position x et y de l'agent
     * @param {String} fact - fait que l'on doit supprimer
     */
        remove_fact_from_pos(pos, to_remove) {
            this.facts[pos].delete(to_remove);
        }

    /**
     * Supprime toutes les connaissances dans la base de connaissances.
     */
        clear() {
            for (const prop of Object.keys(this.facts)) {
                delete this.facts[prop];
            }
        }

}