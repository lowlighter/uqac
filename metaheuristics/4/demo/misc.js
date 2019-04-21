random = Math.random
noise.seed(random())

layout = {
  autosize: true,
  showlegend: false,
  plot_bgcolor: "rgba(40, 40, 40, 0)",
  paper_bgcolor:"rgba(40, 40, 40, 0)",
  font:{
    color:"white"
  },
  xaxis:{
    range: [-100,100],
    showgrid: false,
    showline: false,
    zeroline:false,
    showticklabels: false,
    ticks: '',
    autotick: true,
    fixedrange: true
  },
  yaxis:{
    range: [-100,100],
    showgrid: false,
    showline: false,
    zeroline:false,
    showticklabels: false,
    ticks: '',
    autotick: true,
    fixedrange: true
  },
  margin: {
    r: 122,
  }
};


layout_heatmap = {
  autosize: true,
  showlegend: false,
  plot_bgcolor: "rgb(40, 40, 40)",
  paper_bgcolor:"rgb(40, 40, 40)",
  font:{
    color:"white"
  },
  xaxis:{
    fixedrange: true
  },
  yaxis:{
    fixedrange: true
  }
};

function start(){
    if (!data.algo) {
      init_data()
      data.algo = ica({world:new World(), ...view.$data.parameters}, data)
    }
    if(!data.inter) {
      data.inter = true
      next()
    }
}

async function next() {
  if (!data.inter) return
  await data.algo.next()
  setTimeout(next)
}

function stop(){
    data.inter = null
}

function step() {
  if (!data.algo)
    start()
  stop()
  data.algo.next()
}

function reset(){
    data.inter = null
    data.algo = null
}

function init_data(){
    layout.xaxis.range = data.objective.limit[0]
    layout.yaxis.range = data.objective.limit[1]
    data.countries = []
    data.empires = []
    data.best = {variables:[], cost: Infinity}
    data.iteration = 0
    data.debug ? data.debug.clear() : data.debug = new Logger()
}

function set_test(){
    stop()
    layout.xaxis.range = data.objective.limit[0]
    layout.yaxis.range = data.objective.limit[1]
    reset()
}

let data = {algo:null, inter:null, test_functions, objective:test_functions[0], parameters:{nb_countries:100, nb_imperialists:10, assimilation_deviation:Math.PI/4, assimilation_direction:0.05, colonies_power:0.01, revolution_scale:2, revolution_rate:0.1, influency_epoch:10}}
init_data()
let view = new Vue({el:"#ui", data, computed:{running() { return !!this.$data.inter }}, mounted(){}})
data.algo = null

