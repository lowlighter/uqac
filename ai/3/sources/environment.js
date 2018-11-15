/**
 * Environment.
 * Cette classe permet de gérer les données de l'environment.
 */
  class Environment {
    /**
     * Constructeur. 
     */
      constructor({width, height, agent} = {}) {
        //Propriétés de l'environment
          this.width = width
          this.height = height
          this.agent = agent
          this.previous_score = 0
          this.score = 0

        //Initialisation de la carte
          this.map = []
          this.init()

        //Initialisation des capteurs et des effecteurs de l'agent
          for (let i in this.agent.sensors) this.agent.sensors[i].init(this)
          for (let i in this.agent.effectors) this.agent.effectors[i].init(this)
      }

    /**
     * Réinitialise l'environement.
     */
      clear() {
        for (let x = 0; x < this.width; x++) {
          this.map[x] = []
          for (let y = 0; y < this.height; y++)
            this.map[x][y] = new Set()
        }
      }

    /**
     * Initialise l'environnement.
     */
      init() {
        //Génération de la carte (plusieurs essais avant d'être sur d'avoir une situation qui peut être résolue)
          for (let k = 0; k < 10; k++) {
            //Réinitialisation de la carte
              this.clear()
              this.previous_score = this.score
            
            //Placement de l'agent
              this.agent.x = this.agent.y = 0;
              this.agent.old_position = [0,0];
              this.map[this.agent.x][this.agent.y] = new Set([$.CELL.AGENT])
            
            //Générations aléatoires des éléments
              let positions = Positions(this)
              let elements = [$.CELL.PORTAL]
              for (let i = 0; i < Math.floor(this.width*1.5-2); i++) elements.push(i%2 ? $.CELL.HOLE : $.CELL.MONSTER)

            //Placement des éléments
              elements.forEach((element) => {
                //Ajout sur la carte
                  let {x, y} = positions.shift()
                  this.map[x][y].add(element)  
              })

            //Ajout des signes distinctifs 
              for (let x = 0; x < this.width; x++)
                for (let y = 0; y < this.height; y++) {
                  if (this.map[x][y].has($.CELL.MONSTER)||this.map[x][y].has($.CELL.HOLE)) {
                    let element = this.map[x][y].has($.CELL.MONSTER) ? $.CELL.MONSTER : $.CELL.HOLE
                    let mark = $.MARK[element]
                    //Ajout des marques sur les cases voisines
                      if (mark) 
                        [{dx:-1, dy:0}, {dx:+1, dy:0}, {dx:0, dy:-1}, {dx:0, dy:+1}].forEach(({dx, dy}) => {
                          let mx = x + dx, my = y + dy
                          if ((mx >= 0)&&(mx < this.width)&&(my >= 0)&&(my < this.height)) {
                            let cell = this.map[mx][my]
                            if ((!cell.has($.CELL.MONSTER))&&(!cell.has($.CELL.HOLE))&&(!cell.has($.CELL.PORTAL)))
                              cell.add(mark)
                          }
                      })
                  }
                }

            //S'il existe une solution, on quitte la génération
              if (connectivity(this.map, {from:$.CELL.AGENT, to:$.CELL.PORTAL, walls:$.CELL.HOLE})) break
          }
      }

    /**
     * Applique l'action de l'agent lancée par ses effecteurs.
     * @param {Effector} action - Action effectuée
     * @param {Object} data - Données additionnelles
     */
      apply(action, data) {
        //Debug
          debug.agent.log2($.DEBUG[action])

        //Déplacement
          if (action & $.ACTION.MOVE) {
              this.agent.knowledge_base.remove_fact_from_pos(this.agent.old_position, $.CELL.PREVIOUS_POSITION);         
              this.agent.old_position = [this.agent.x, this.agent.y];
              this.agent.knowledge_base.add_fact_for_pos(this.agent.old_position, $.CELL.PREVIOUS_POSITION);
            //Déplacement de l'agent
              this.score += $.SCORE.MOVE
              this.map[this.agent.x][this.agent.y].delete($.CELL.AGENT)
              this.agent.x = Math.min(Math.max(0, this.agent.x + data.dx), this.width - 1)
              this.agent.y = Math.min(Math.max(0, this.agent.y + data.dy), this.height - 1)
            //Vérification des monstres et des crevasses
              if (this.map[this.agent.x][this.agent.y].has($.CELL.MONSTER)||this.map[this.agent.x][this.agent.y].has($.CELL.HOLE)) {
                this.on_agent_death();
              }
            //Placement de l'agent
              this.map[this.agent.x][this.agent.y].add($.CELL.AGENT)
          }
        
        //Tir
          if (action & $.ACTION.SHOOT) {
            //Vérification de la case
              this.score += $.SCORE.SHOOT
              let {x, y} = {x:this.agent.x+data.dx, y:this.agent.y+data.dy}
              let valid = (x >= 0)&&(x < this.width)&&(y >= 0)&&(y < this.height)
              debug.agent.log($.DEBUG.SHOOT_TO(data))
              if (valid) this.on_agent_shoot({x, y})
            //Exécute le monstre à l'espace indiquée (s'il existe)
              if ((valid)&&(this.map[x][y].has($.CELL.MONSTER))) {
                debug.agent.success($.DEBUG.SHOOT_SUCCESS)
                this.map[x][y].delete($.CELL.MONSTER)
                this.map[x][y].add($.CELL.MONSTER_DEAD)
              } 
            //Aucune cible
              else { debug.agent.error($.DEBUG.SHOOT_MISS) }
          }

        //Sortie
          if (action === $.ACTION.EXIT) {
            //Passage au niveau suivant s'il y a un portail
              if (this.map[this.agent.x][this.agent.y].has($.CELL.PORTAL)) {
                this.score += $.SCORE.EXIT * this.width * this.height
                debug.agent.success($.DEBUG.EXIT_SUCCESS)
                debug.agent.success($.DEBUG.SCORE(this.score, this.previous_score))
                this.next()
              } 
            //Echec de sortie
              else { debug.agent.error($.DEBUG.EXIT_FAILED) }
          }
      }

    /** 
     * Passe au niveau suivant.
     * Aggrandie la carte et regénère l'environnement.
     */
      next() {
        this.width++
        this.height++
        this.agent.knowledge_base.size++
        this.agent.knowledge_base.init();
        this.init();
        debug.agent.hide(".info")
      }

    /**
     * Se déclenche lorsque l'agent effectue un tir.
     */
      on_agent_shoot({x, y}) {
        //Ajout de la connaissance de la position du tir (l'agent sait où il a tiré)
        this.map[x][y].add($.CELL.SHOT)
        this.agent.knowledge_base.add_fact_for_pos([x, y], $.CELL.SHOT);
        this.agent.rule_base.remove_risk_from_pos([x, y], $.CELL.EXCREMENT);
        this.agent.knowledge_base.remove_fact_from_pos([x, y], $.CELL.MONSTER);
      }

    /**
     * Se déclenche lorsque l'agent décède.
     */
      on_agent_death(){
        //Ajout de la connaissance du meurtrier
        if(this.map[this.agent.x][this.agent.y].has($.CELL.MONSTER)){
          this.agent.knowledge_base.add_fact_for_pos([this.agent.x, this.agent.y],$.CELL.MONSTER);
        } else {
          this.agent.knowledge_base.add_fact_for_pos([this.agent.x, this.agent.y],$.CELL.HOLE);
        }
        this.agent.rule_base.remove_risk_from_pos([this.agent.x, this.agent.y], $.CELL.EXCREMENT);
        this.agent.rule_base.remove_risk_from_pos([this.agent.x, this.agent.y], $.CELL.WIND);
        this.score += $.SCORE.DEATH * this.width * this.height;
        this.agent.knowledge_base.remove_fact_from_pos(this.agent.old_position, $.CELL.PREVIOUS_POSITION);   
        this.agent.x = this.agent.y = 0;
        this.agent.knowledge_base.add_fact_for_pos([0,0], $.CELL.PREVIOUS_POSITION);
        debug.agent.error($.DEBUG.DEATH)
      }
  }