/**
 * Moteur de rendu.
 * Cette classe permet de gérer la surface de dessin afin d'afficher l'état interne de l'environment.
 */
  class Renderer {

    /**
     * Constructeur.
     */
      constructor() {
        //Surface de rendu
          this.canvas = document.querySelector("canvas")
          this.context = this.canvas.getContext("2d")

        //Textures
          this.textures = {
            cell:document.querySelector("[alt=cell]"),
            clean:document.querySelector("[alt=clean]"),
            dust:document.querySelector("[alt=dust]"),
            jewel:document.querySelector("[alt=jewel]"),
            both:document.querySelector("[alt=both]"),
            agent:document.querySelector("[alt=agent]"),
            action:document.querySelector("[alt=action]"),
          }
      }

    /**
     * Initialise la surface de rendu.
     * @param {Object} options - Options (voir ci-dessous)
     * @param {Number} options.width - Largeur de la surface de rendu (en nombre de celulles)
     * @param {Number} options.height - Hauteur de la surface de rendu (en nombre de celulles)
     */
      init({width, height} = options) {
        this.canvas.width = width * $.CELL.SIZE
        this.canvas.height = height * $.CELL.SIZE
      }

    /**
     * Met à jour le rendu d'une instance d'environment.
     * @param {Environment} environment - Instance de l'environment
     */
      render(environment) {
        //Nettoyage de la surface de rendu
          this.context.clearRect(0, 0, this.canvas.width, this.canvas.height)

        //Rendu des pièces de l'environment
          let inmap = {dust:0, jewel:0}
          for (let x = 0; x < environment.width; x++) {
            for (let y = 0; y < environment.height; y++) {
              //Récupération des données de la celulle
                let cell = environment.map[x][y]
              //Affichage de la texture
                this.context.drawImage(this.textures.cell, x * $.CELL.SIZE, y * $.CELL.SIZE)
                this.context.drawImage(this.textures[["clean", "dust", "jewel", "both"][cell]], x * $.CELL.SIZE, y * $.CELL.SIZE)
              //Comptage des poussières et bijoux sur la carte
                inmap.dust += (cell & $.CELL.DUST) > 0
                inmap.jewel += (cell & $.CELL.JEWEL) > 0
            }
          }

        //Rendu de l'agent
          this.context.drawImage(environment.agent.action ? this.textures.action : this.textures.agent, environment.agent.x * $.CELL.SIZE, environment.agent.y * $.CELL.SIZE)

        //Rendu des scores
          document.querySelector(".timelapse").innerHTML = Math.round(environment.scores.timelapse/1000)
          ;["recent", "total"].forEach(type => {
            document.querySelector(`.${type} .dust`).innerHTML = environment.scores[type].dust.aspired
            document.querySelector(`.${type} .jewel-picked`).innerHTML = environment.scores[type].jewel.picked
            document.querySelector(`.${type} .jewel-aspired`).innerHTML = environment.scores[type].jewel.aspired
            document.querySelector(`.${type} .energy`).innerHTML = environment.scores[type].energy.used
            document.querySelector(`.${type} .dust-generated`).innerHTML = environment.scores[type].dust.generated
            document.querySelector(`.${type} .jewel-generated`).innerHTML = environment.scores[type].jewel.generated
          })
          document.querySelector(`.inmap .dust-generated`).innerHTML = inmap.dust
          document.querySelector(`.inmap .jewel-generated`).innerHTML = inmap.jewel
          document.querySelector(`.recent .measure`).innerHTML = environment.measure
          document.querySelector(`.learning-ratio`).innerHTML = `${(100*agent.learning.ratio).toFixed(2)} %`
      }

  }
