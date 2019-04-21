class Country {

    //Constructor
      constructor(world, variables) {
        this.world = world
        this.variables = variables
      }
  
    //Power
      power = NaN
  
    //Cost (based on objective function)
      get cost() {
        return objective(this)
      }
  
    //Influency (of empire)
      influency = NaN
  
    //Imperialist allegiance
      allegiance = null
  
    //Tell if imperialist (i.e. allegiance to self)
      get imperialist() {
        return this.allegiance === this
      }
  
    //List of colonies (if imperialist, else it will be empty)
      get colonies() {
        return [...this.world.countries].filter(country => (country.allegiance === this) && (country !== this))
      }
  
    //Compute the distance to another country
      distance(country) {
        return this.variables.map((variable, index) => country.variables[index] - variable)
      }
  
    //Correct variables values
      clamp() {
        let limits = this.world.limits
        for (let i = 0; i < this.variables.length; i++) 
          this.variables[i] = Math.max(limits[i][0], Math.min(limits[i][1], this.variables[i]))
      }
  
    //Color
      get color() {
        return this.world.color(this)
      }
  
  }