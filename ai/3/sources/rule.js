
/**
 * Regle
 */

class Rule {

    /**
     * Constructeur.
     */
        constructor(rule_base, premises, conclusion) {
            this.rule_base = rule_base;
            this.premises = premises;
            this.conclusion = conclusion;
        }

    /**
     * Référence à l'agent.
     */
        get agent() { return this.rule_base.agent }

    /**
     * Référence à la base de connaissance.
     */
        get knowledge_base() { return this.rule_base.knowledge_base }
    
}