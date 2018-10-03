//Fonction d'attente
  function sleep(time) {
    return new Promise(solve => setTimeout(solve, time))
  }

//Borne une valeur entre une valeur min et une valeur max
  Math.clamp = function(min, value, max) {
    return Math.max(min, Math.min(value, max))
  }

//Clone les données d'un tableau
  function clone(data) {
    return JSON.parse(JSON.stringify(data))
  }

//Indique si deux nombre flottants sont égaux
  function equals(a, b) {
    return Math.abs(a - b) <= Number.EPSILON
  }

//Mélange un tableau
  function shuffle(array) {
    for (let i = array.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[array[i], array[j]] = [array[j], array[i]]
    }
    return array
  }

//Alias pour les threads
  var Thread = Worker

//Constantes
  const $ = {
    //Agent
      AGENT:{
        //Signaux de gestion du thread d'exploration
          THREAD_INIT:"INIT",
          EXPLORATION_START:"EXPLORATION_START",
          EXPLORATION_DONE:"EXPLORATION_DONE",
          EXPLORATION_TIMEOUT:"TIMEOUT",
          EXPLORATION_FAILED:"FAILED",
          EXPLORATION_INFO_DEPTH:10,
          EXPLORATION_INFO_CLOSED:11,
          INFORMED:1,
          UNINFORMED:0,
          MIN_TIMEOUT:0.5,
          MAX_TIMEOUT:5,
      },

    //Actions
      ACTION:{
        NONE:0,
        ASPIRE:0b001,
        PICKUP:0b010,
        MOVE:0b100,
        UP:0b100,
        DOWN:0b101,
        LEFT:0b110,
        RIGHT:0b111,
        DONE:null,
        PENDING:true
      },

    //Cellules
      CELL:{
        //Environement
          CLEAN:0,
          DUST:1 << 0,
          JEWEL:1 << 1,
        //Rendu
          SIZE:32,
      },

    //Evénèments utilisés pour gérer l'historique de l'application
      EVENT:{
        DUST_ASPIRED:1,
        DUST_GENERATED:2,
        JEWEL_PICKED:3,
        JEWEL_ASPIRED:4,
        JEWEL_GENERATED:5,
        ENERGY_USED:6,
      },

    //Fonctions sérialisées (utilisés en cas de thread file://)
      FUNCTIONS:[clone.toString()]
  }

//Thread de l'agent
  $.AGENT.THREAD = {
    //Code source pour l'exploration
      get SOURCE() { return URL.createObjectURL(new Blob([`(${AgentExplore.toString()})()`], {type: "text/javascript"})) },
    //Context
      CONTEXT:JSON.stringify($),
  }

//Map retournant l'effecteur à activer pour effectuer une certaine action
  $.EFFECTOR = {
    [$.ACTION.NONE]:function (agent) { return agent.effectors.wait.enable() },
    [$.ACTION.ASPIRE]:function (agent) { return agent.effectors.aspire.enable() },
    [$.ACTION.PICKUP]:function (agent) { return agent.effectors.pickup.enable() },
    [$.ACTION.UP]:function (agent) { return agent.effectors.move.up() },
    [$.ACTION.DOWN]:function (agent) { return agent.effectors.move.down() },
    [$.ACTION.LEFT]:function (agent) { return agent.effectors.move.left() },
    [$.ACTION.RIGHT]:function (agent) { return agent.effectors.move.right() },
  }

//Messages de debug
  $.DEBUG = {
    ENABLED:true,
    [$.ACTION.NONE]:"Attente…",
    [$.ACTION.ASPIRE]:"Aspiration…",
    [$.ACTION.PICKUP]:"Ramassage…",
    [$.ACTION.UP]:"Haut",
    [$.ACTION.DOWN]:"Bas",
    [$.ACTION.LEFT]:"Gauche",
    [$.ACTION.RIGHT]:"Droite",
    EXPLORATION_TIMEOUT:"Exploration - Temps écoulé",
    EXPLORATION_FAILED:"Exploration - Échec",
    EXPLORATION_STARTED:"Exploration - Démarée",
    EXPLORATION_DONE:"Exploration - Terminée",
    ASPIRE_DUST:"Poussière aspirée",
    ASPIRE_JEWEL:"Bijou aspiré",
    ASPIRE_NOTHING:"Rien à aspirer",
    PICKUP_JEWEL:"Bijou ramassé",
    PICKUP_NOTHING:"Rien à ramasser",
    DUST_GENERATED:" - Nouvelle poussière",
    JEWEL_GENERATED:" - Nouveau bijou",
    PAUSED:"Mis en pause",
    UNPAUSED:"Reprise",
    INFORMED:"Mode d'exploration - Informée",
    UNINFORMED:"Mode d'exploration - Non informée"
  }
