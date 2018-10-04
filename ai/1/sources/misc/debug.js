//Debugger
  class Debugger {
    //Constructeur
      constructor(logconsole) { this.console = document.querySelector(logconsole) }

    //Ligne dans le journal
      log(text) { if ($.DEBUG.ENABLED) this.console.innerHTML = `<div>${this.time} │ ${text}</div>` + this.console.innerHTML }
      log2(text) { if ($.DEBUG.ENABLED) this.console.innerHTML = `<div class="log2">${this.time} │ ${text}</div>` + this.console.innerHTML }
      info(text) { if ($.DEBUG.ENABLED) this.console.innerHTML = `<div class="info">${this.time} │ ${text}</div>` + this.console.innerHTML }
      error(text) { if ($.DEBUG.ENABLED) this.console.innerHTML = `<div class="error">${this.time} │ ${text}</div>` + this.console.innerHTML }
      success(text) { if ($.DEBUG.ENABLED) this.console.innerHTML = `<div class="success">${this.time} │ ${text}</div>` + this.console.innerHTML }

    //Temps
      get time() { return (performance.now()/1000).toFixed(1).padStart(6, " ") }
  }

//Debugger (agent)
  class AgentDebugger extends Debugger {
    //Constructeur
      constructor(logconsole) {
        super(logconsole)
        this.iconsole = document.querySelector(".intentions .list")
      }

    //Affichage des intentions
      intentions(actions) {
        if (actions === null) {
          debug.agent.log($.DEBUG.EXPLORATION_STARTED)
          this.iconsole.innerHTML = `<div class="computing">Exploration en cours...<div class="info"></div><div class="bar"><div class="inner" style="animation-duration: ${agent.exploration.timeout}s;"></div></div></div>`
          this.cursor = 0
        } else {
          let text = ""
          actions.forEach(action => text += `<div>${$.DEBUG[action]}</div>`)
          this.iconsole.innerHTML = text
        }
      }

    //Met à jour les informations sur l'exploration
      iexplore(type, value) {
        let text = ""
        if ($.AGENT.EXPLORATION_INFO_CLOSED === type) text = `Noeuds explorés ${value}`
        if ($.AGENT.EXPLORATION_INFO_DEPTH === type) text = `Profondeur ${value}`
        ;(document.querySelector(".intentions .info")||{}).innerHTML = text
      }

    //Mise à jour de l'affichages des intentions
      action() {
        debug.agent.iconsole.children[0].remove()
      }

    //Affiche le mode d'exploration de l'agent
      mode() {
        if (agent._mode_changed) debug.agent.info(agent.mode === $.AGENT.INFORMED ? $.DEBUG.INFORMED : $.DEBUG.UNINFORMED)
        agent._mode_changed = false
      }

  }

//Debuggers liés aux agent et à l'environment
  const debug = {
    environment:new Debugger(".environment"),
    agent:new AgentDebugger(".agent"),
  }

//Initialiseur
  window.onload = function () {
    //Génération manuelle de l'environnement
      let p = (event) => {
        return {
          x:Math.floor((event.clientX - renderer.canvas.offsetLeft) * (renderer.canvas.width / renderer.canvas.clientWidth) / $.CELL.SIZE),
          y:Math.floor((event.clientY - renderer.canvas.offsetTop) * (renderer.canvas.height / renderer.canvas.clientHeight) / $.CELL.SIZE)
        }
      }
      renderer.canvas.onclick = function (event) {
        let {x, y} = p(event)
        if (event.button === 0) environment.create_dust({x, y}, true)
        environment.render()
        return false
      }
      renderer.canvas.oncontextmenu = function (event) {
        let {x, y} = p(event)
        environment.create_jewel({x, y}, true)
        environment.render()
        return false
      }

    //Customisation de l'agent
      document.querySelector("[name=exploration]").onchange = function (event) {
        let target = event.target
        agent.mode = +target.options[target.selectedIndex].value
      }
      document.querySelector("[name=learning]").onchange = function (event) {
        let target = event.target
        agent.learning.enabled = target.checked
        document.querySelector(".learning .details").classList[agent.learning.enabled ? "remove" : "add"]("hide")
      }

    //Pause
      window.onkeyup = function (event) {
        if (event.key === "Escape") environment.paused = !environment.paused
        if (event.key === "z") agent.effectors.move.up()
        if (event.key === "q") agent.effectors.move.left()
        if (event.key === "s") agent.effectors.move.down()
        if (event.key === "d") agent.effectors.move.right()
        if (event.key === "a") agent.effectors.aspire.enable()
        if (event.key === "e") agent.effectors.pickup.enable()
      }
  }
