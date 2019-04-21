async function* ica(parameters) {
    //Initialisation
      let {world, nb_countries, nb_imperialists, assimilation_deviation, assimilation_direction, colonies_power, revolution_scale, revolution_rate, influency_epoch} = parameters
      let best = {variables:[], cost:Infinity}
      let iteration = 0
      
    //1. Generate countries
      let countries = []
      for (let i = 0; i < nb_countries; i++)
        countries.push(world.create_country())
  
    //2. Select imperialists among the best countries
      countries.sort((a, b) => a.cost - b.cost)
      let imperialists = countries.slice(0, nb_imperialists)
      let colonies = countries.slice(nb_imperialists)
      imperialists.forEach(country => country.allegiance = country)
      world.colors()
  
    //3. Compute imperialists initial power
      let cost_imperialists = imperialists.map(imperialist => imperialist.cost)
      let max_cost_imperialists = Math.max(...cost_imperialists)
      let power_imperialists = cost_imperialists.map(cost => max_cost_imperialists - cost)
      let sum_power_imperialists = power_imperialists.reduce((a, b) => a + b, 0)
      imperialists.forEach((imperialist, i) => imperialist.power = Math.abs(power_imperialists[i]/sum_power_imperialists))
      
    //4. Form empire by granting colonies to imperialists
      for (let imperialist of imperialists) {
        let nb_colonies = Math.round(imperialist.power * (nb_countries - nb_imperialists))
  
        //Attribution of colonies
          for (let i = 0; (i < nb_colonies)&&(colonies.length); i++)
            colonies.splice(Math.floor(random()*colonies.length), 1)[0].allegiance = imperialist
            
      }
  
    //Main loop
      await world.create_or_update_graph(true)
      let update = () => { data.iteration = iteration ; data.countries = countries ; data.empires = imperialists.map(v => { return {x:v.colonies.length, color:v.color} }).sort((a, b) => a.color - b.color) ; data.best = best }
      while (++iteration) {
  
        //Update colonies and imperialist lists
          countries = [...world.countries]
          colonies = world.colonies
          imperialists = world.imperialists
          update()
          await world.create_or_update_graph()
  
        //Update colonies with assimilation and revolutions
          for (let colony of colonies) {
            //5. Assimilation
              if (colony.allegiance) {
                //Direction
                  let d = colony.distance(colony.allegiance)
                  let x = d.map(distance => random() * distance * assimilation_direction)
                //Deviation
                  let t = Math.cos(-assimilation_deviation + 2 * random() * assimilation_deviation)
                //Affectation
                  colony.variables = colony.variables.map((v, i) => v + x[i] * t)
              }
  
            //6. Revolutions
                if (random() < revolution_rate)
                  colony.variables = colony.variables.map(v => v + revolution_scale * (random() - random()))
                
            //Adjust variables values (must be inside world's limit)
              colony.clamp()   
          }
  
          await world.just_update_graph()

        //7. Overthrow of imperialists
          for (let imperialist of imperialists) {
            
            //Search of a better colony inside the empire
              let colonies_cost = imperialist.colonies.map(colony => colony.cost)
              let colonies_min = Math.min(...colonies_cost)
  
            //If better, the colony overthrow the current imperialist
              if (colonies_min < imperialist.cost) {
                let new_imperialist = imperialist.colonies[colonies_cost.indexOf(colonies_min)]
                imperialist.colonies.forEach(colony => colony.allegiance = new_imperialist)
                imperialist.allegiance = new_imperialist
                world.update_colors(imperialist, new_imperialist)
              }
  
          }
  
        //Update colonies and imperialists (because of the possible overthrow)
          colonies = world.colonies
          imperialists = world.imperialists
          update()
  
        //8. Competition
          //Compute the influency of each imperialist
            let total_cost_imperialists = imperialists.map(imperialist => imperialist.cost + colonies_power * (imperialist.colonies.map(colony => colony.cost).reduce((a, b) => a + b, 0)/(imperialist.colonies.length||1)))
            let max_total_cost_imperialists = Math.max(...total_cost_imperialists)
            let sum_total_cost_imperialists = total_cost_imperialists.reduce((a, b) => a + b, 0)
            let influencies = total_cost_imperialists.map((total_cost_imperialist, i) => imperialists[i].influency = Math.abs((total_cost_imperialist - max_total_cost_imperialists)/sum_total_cost_imperialists))
  
          //Weakest colony
            let less_influent = imperialists[influencies.indexOf(Math.min(...influencies))]
            let less_influent_colonies = less_influent.colonies.concat(world.city_states)
            let colonies_cost = less_influent_colonies.map(colony => colony.cost)
            let weakest = less_influent_colonies[colonies_cost.indexOf(Math.max(...colonies_cost))]
  
          //Influencies war
            for (let j = 0; j < influency_epoch; j++)
              influencies = influencies.map(influency => influency - random())
            let winner = imperialists[influencies.indexOf(Math.max(...influencies))]
  
          //Change the weakest colony allegiance
            if(weakest) weakest.allegiance = winner
  
        //9. Collapse the weakest empire
          for (let imperialist of imperialists)
            if (imperialist.colonies.length === 0) {
              imperialist.allegiance = null
              data.debug.warn(`An empire collapsed (iteration ${iteration})`)
            }
              
        //Update best solution
          let countries_cost = countries.map(country => country.cost)
          let best_country = countries[countries_cost.indexOf(Math.min(...countries_cost))]
          if (best_country.cost < best.cost) {
            data.debug.success(`Previous best : ${best.cost.toFixed(2)} --> New best : ${best_country.cost.toFixed(2)}`)
            best = {variables:best_country.variables.slice(), cost:best_country.cost}
          }
  
          yield best
      }
  }