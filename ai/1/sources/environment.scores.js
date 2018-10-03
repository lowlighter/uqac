/**
 * Gestionnaires de scores pour les modifications de l'environement.
 * Cette classe enregistre l'historique des actions réalisés par les effecteurs de l'agent.
 * Elle peut retourner l'historique total ou bien les plus récentes secondes.
 */
  class Scores {

    /**
     * Constructeur.
     * @param {Object} options - Options (voir ci-dessous)
     * @param {Number} options.timelapse - Laps de temps durant lequel les entrées de l'historique des scores est considéré comme récent
     */
      constructor({timelapse} = options) {
        //Propriétés
          this.timelapse = timelapse * 1000
          this.history = []
          this.total = {dust:{aspired:0, generated:0}, jewel:{aspired:0, picked:0, generated:0}, energy:{used:0}}
      }

    /** Modificateur de poussière. */
      get dust() { return {aspired:() => this.push($.EVENT.DUST_ASPIRED), generated:() => this.push($.EVENT.DUST_GENERATED)} }

    /** Modificateur de bijoux. */
      get jewel() { return {aspired:() => this.push($.EVENT.JEWEL_ASPIRED), picked:() => this.push($.EVENT.JEWEL_PICKED), generated:() => this.push($.EVENT.JEWEL_GENERATED)} }

    /** Modificateur d'énergie. */
      get energy() { return {used:() => this.push($.EVENT.ENERGY_USED)} }

    /** Scores récents. */
      get recent() {
        let history = (this.history = this.history.filter(p => p.time > (performance.now() - this.timelapse)))
        return {
          dust:{aspired:history.filter(p => p.event === $.EVENT.DUST_ASPIRED).length, generated:history.filter(p => p.event === $.EVENT.DUST_GENERATED).length},
          jewel:{aspired:history.filter(p => p.event === $.EVENT.JEWEL_ASPIRED).length, picked:history.filter(p => p.event === $.EVENT.JEWEL_PICKED).length, generated:history.filter(p => p.event === $.EVENT.JEWEL_GENERATED).length},
          energy:{used:history.filter(p => p.event === $.EVENT.ENERGY_USED).length}
        }
      }

    /** Ajoute une entrée dans l'historique. */
      push(event) {
        //Ajout dans l'historique
          this.history.push({event, time:performance.now()})

        //Ajout dans le compteur général
          switch (event) {
            case $.EVENT.DUST_ASPIRED: return this.total.dust.aspired++
            case $.EVENT.DUST_GENERATED: return this.total.dust.generated++
            case $.EVENT.JEWEL_PICKED: return this.total.jewel.picked++
            case $.EVENT.JEWEL_ASPIRED: return this.total.jewel.aspired++
            case $.EVENT.JEWEL_GENERATED: return this.total.jewel.generated++
            case $.EVENT.ENERGY_USED: return this.total.energy.used++
          }
      }

  }
