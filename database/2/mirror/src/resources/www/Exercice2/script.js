(async function () {


    async function battle(n) {
        let response = await axios.get(`battles/battle${n}`)
        let turns = response.data.split("\n").filter(line => line.length).map(line => JSON.parse(line))


        let vue = new Vue({el:"#app", data:{monsters:[]}})

        function* gen() {
            for (let turn of turns) {
                vue.$data.monsters = turn
                yield
            }
        }

        return gen()

    }

    window.b = await battle(1)

    //Websocket
    let ws = null
    function reconnect() {
        ws =  new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
        ws.onmessage = message => {
            let parsed = JSON.parse(message.data)
            console.log(parsed, Date.now())
        }
        ws.onopen = () => { ws.send("{}") }
        ws.onclose = () => {
            if (reconnect.attempts++ < 5) reconnect()
        }
    }
    reconnect.attempts = 0
    reconnect()


})()
