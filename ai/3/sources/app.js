//Application
  let agent = new Agent()
  let environment = new Environment({width:3, height:3, agent})
  let vue = new Vue({el:"#app", data:{environment, debug, $}})