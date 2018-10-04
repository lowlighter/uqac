//Moteur de rendu
  const renderer = new Renderer()
//Agent
  const agent = new Agent({frequency:6, exploration:{timeout:10}, learning:{factor:0.35, history:100}})
//Environment
  const environment = new Environment({width:10, height:10, dust:0.05, jewel:0.02, frequency:20, scores:{timelapse:15}, agent, renderer})
