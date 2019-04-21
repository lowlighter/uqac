class World {
  
    //Limits of the world
      limits = data.objective.limit
  
    //List of countries
      countries = new Set()
  
    // Map data
      map = []
  
    // Graph
      graph = null
  
      constructor() {
        this.init_map();
      }
  
    //Create a new country and initialize it randomly
      create_country() {
        const country = new Country(this, this.limits.map(v => v[0] + random()*(v[1]-v[0])))
        this.countries.add(country)
        return country
      }
  
    //List of imperialists
      get imperialists() {
        return [...this.countries].filter(country => country.imperialist)
      }
  
    //List of colonies
      get colonies() {
        return [...this.countries].filter(country => !country.imperialist)
      }
    
    //List of city states 
      get city_states() {
        return [...this.countries].filter(country => country.allegiance === null)
      }
  
    //Update colors after an overthrow
      update_colors(prev, next) {
        let color = this._colors.get(prev)
        this._colors.delete(prev)
        this._colors.set(next, color)
      }
  
    //Colors initializer
      colors() {
        //let colors = ["#FFFFFF", "#FF0000", "#800000", "#FFFF00", "#808000", "#00FF00", "#008000", "#00FFFF", "#008080", "#0000FF", "#000080", "#FF00FF", "#800080"]
        let colors = ["rgb(255, 255, 255)", "rgb(255, 0, 0)", "rgb(0, 255, 0)", "rgb(0, 0, 255)", "rgb(255, 255, 0)", "rgb(0, 255, 255)", "rgb(255, 0, 255)", "rgb(128, 0, 0)", "rgb(0, 128, 0)",  "rgb(0, 0, 128)", "rgb(128, 128, 0)", "rgb(0, 128, 128)",  "rgb(128, 0, 128)"]
        for (let i = colors.length; i < 100; i++) 
          colors.push(`rgb(${Math.floor(Math.random()*255)}, ${Math.floor(Math.random()*255)}, ${Math.floor(Math.random()*255)})`)
        this._colors = new Map()
        for (let i = 0; i < this.imperialists.length; i++)
          this._colors.set(this.imperialists[i], colors[i])
      }
  
    //Color giver
      color(country) {
        return this._colors.get(country.allegiance)||"rgb(0, 0, 0)"//||"#000000"
      }
  
    // Map init
  
      init_map() {
        let x_coord = []
        let y_coord = []
        let z_coord = []
        for(let i=this.limits[0][0];i<=this.limits[0][1];i++){
          let line = []
          for(let j=this.limits[1][0]; j<=this.limits[1][1]; j++){
          x_coord.push(i)
          y_coord.push(j)
          z_coord.push(data.objective.func(i,j))
          }
        }
        this.map.push({
             x: x_coord,
             y: y_coord,
             z: z_coord,
             type: 'heatmap',
             colorscale:'Blues'
        });
      }
  
      compute_traces() {
        let traces = []
        for (let imperialist of this.imperialists) {
          let trace = {
            mode: 'markers',
            marker: {
              symbol: "diamond",
              size: 12,
              line: {
              //color: 'rgba(0, 255, 0, 1)',
              width: 1},
              opacity: 0.8},
            type: 'scatter2d'
          };
  
            // Setting parameter for the imperialist trace
            trace.x = [imperialist.variables[0]]
            trace.y = [imperialist.variables[1]]
            //trace.z = [imperialist.cost]
            trace.marker.color = imperialist.color
            trace.marker.symbol = "diamond"
            trace.marker.size = 18
            traces.push(trace)
  
            // Setting parameter for the colony trace
            let ctrace = {
              mode: 'markers',
              marker: {
                symbol: "circle",
                color: imperialist.color,
                size: 10,
                line: {width: 1},
                opacity: 0.8},
              type: 'scatter2d'
            };
            ctrace.x = []
            ctrace.y = []
            ctrace.z = []
  
            for(let colony of imperialist.colonies) {
              ctrace.x.push(colony.variables[0])
              ctrace.y.push(colony.variables[1])
              //ctrace.z.push(colony.cost)
  
            }
            traces.push(ctrace)
            
        }
        

        let ct_trace = {
          mode: 'markers',
          x:[],
          y:[],
          z:[],
          marker: {
            symbol: "pentagon",
            size: 12,
            line: {width: 1},
            color: 'rgb(0,0,0)',
            opacity: 0.8},
          type: 'scatter2d'
        };

        for(let city of this.city_states){
          ct_trace.x.push(city.variables[0])
          ct_trace.y.push(city.variables[1])
        }
        traces.push(ct_trace)

        return traces
      }

    // Update graph markers
      async create_or_update_graph(heatmap = false){
          let traces = this.compute_traces()
          if (heatmap) await Plotly.react('graph-heatmap', this.map, layout)
          await Plotly.react('graph', traces, layout)
      }

    //
      just_update_graph() {
        //this.create_or_update_graph()
        //return null
        let data = this.compute_traces()
        return Plotly.animate('graph', {
          data,
        }, {
          transition: {
            duration: 250,
            easing: 'cubic-in-out'
          },
          frame: {
            duration: 250
          }
        })
      }
  }