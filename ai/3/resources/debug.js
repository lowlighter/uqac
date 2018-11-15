//Debugger
  class Debugger {
    //Constructeur
      constructor(logconsole) { this.logs = [] }

    //Ligne dans le journal
      log(text) { if ($.DEBUG.ENABLED) this.logs.unshift({text:`${this.time} | ${text}`, type:""}) }
      log2(text) { if ($.DEBUG.ENABLED) this.logs.unshift({text:`${this.time} | ${text}`, type:"log2"}) }
      info(text) { if ($.DEBUG.ENABLED) this.logs.unshift({text:`${this.time} | ${text}`, type:"info"}) }
      error(text) { if ($.DEBUG.ENABLED) this.logs.unshift({text:`${this.time} | ${text}`, type:"error"}) }
      success(text) { if ($.DEBUG.ENABLED) this.logs.unshift({text:`${this.time} | ${text}`, type:"success"}) }

    //Temps
      get time() { return (performance.now()/1000).toFixed(1).padStart(6, " ") }
  }

//Debuggers liés aux agent et à l'environment
  const debug = {
    agent:new Debugger(".agent"),
    knowledge: new Debugger(".knowledge")
  }

//Initialiseur
  window.onload = function () {
    //Pause
      window.onkeyup = function (event) {
        if (event.key === "z") agent.effectors.move.up()
        if (event.key === "q") agent.effectors.move.left()
        if (event.key === "s") agent.effectors.move.down()
        if (event.key === "d") agent.effectors.move.right()
        if (event.key === "e") agent.effectors.exit.exit()
        if (event.key === "i") agent.routine()
        if (event.key === "a") {
          if ((agent.x-1 >= 0)&&(environment.map[agent.x-1][agent.y].has($.CELL.MONSTER))) return agent.effectors.shoot.shoot({dx:-1, dy:0})
          if ((agent.x+1 < environment.width)&&(environment.map[agent.x+1][agent.y].has($.CELL.MONSTER))) return agent.effectors.shoot.shoot({dx:+1, dy:0})
          if ((agent.y-1 >= 0)&&(environment.map[agent.x][agent.y-1].has($.CELL.MONSTER))) return agent.effectors.shoot.shoot({dx:0, dy:-1})
          if ((agent.y+1 < environment.height)&&(environment.map[agent.x][agent.y+1].has($.CELL.MONSTER))) return agent.effectors.shoot.shoot({dx:0, dy:+1})
          agent.effectors.shoot.shoot({dx:0, dy:0})
        }
      }
  }