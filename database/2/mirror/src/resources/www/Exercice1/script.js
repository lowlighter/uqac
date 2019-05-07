(function () {

    //Data
    const data = {
        query:{
            name:"",
            advanced:false,
            levels:[],
            components:[],
            classes:[],
            schools:[],
            misc:"",
            limit:10
        },
        delay:null,
        response:null,
        connected:false,
        init:false,
        pending:{start:0, complete:0},
        components:[], classes:[], schools:[], levels:[]
    }

    let format = data => (data.charAt(0).toLocaleUpperCase() + data.substr(1)).replace(/ \bs\b/g, "'s")

    //Websocket
    let ws = null
    function reconnect() {
        ws =  new WebSocket(location.href.replace(/^http/, "ws").replace(/\/$/, "/ws"));
        ws.onmessage = message => {
            let parsed = JSON.parse(message.data)
            //Check if init
            let first = parsed.results[0]||{}
            if ((first.type||[])[0] === "init") {
                delete first.type
                for (let filters in first)
                    data[filters] = first[filters].sort()
                data.init = true
            }
            //Else accept response
            else {
                let marked = null
                if (data.query.misc) marked = new RegExp(`\\b(${data.query.misc.split(" ").join("|")})[a-z]*\\b`, "gi")
                data.pending.complete = performance.now() - data.pending.start
                parsed.results = parsed.results.map(result => JSON.parse(result))
                parsed.results.forEach(result => {
                    result.monsters = JSON.parse(result.monsters||"[]")
                    result.monsters.forEach(m => m.spells = m.spells.substr(1, m.spells.length-2).split(",").map(s => format(s)).join(", "))
                    result.components = result.components.substr(1, result.components.length-2).split(",").join(", ")
                    if (marked) result.description = result.description.replace(marked, m => `<mark>${m}</mark>`)
                })
                data.response = parsed
            }
        }

        ws.onopen = () => {
            data.connected = true
            reconnect.attempts = 0
            ws.send(JSON.stringify({init:true}))
        }
        ws.onclose = () => {
            console.log("disconnected")
            data.connected = false
            if (reconnect.attempts++ < 5) reconnect()
        }
    }
    reconnect.attempts = 0
    reconnect()


    //View
    const view = new Vue({
        el:"#app",
        data,
        methods:{
            send(message) { 
                ws.send(JSON.stringify(message)) 
                data.pending.start = performance.now()
            },
            q(type, element) {
                data.query[type].includes(element) ? data.query[type].splice(data.query[type].indexOf(element), 1) : data.query[type].push(element)
                this.submit()
            },
            submit() {
                clearTimeout(data.delay)
                let query = {...data.query, name:data.query.name.replace(/[,']/g, " ")}
                if (query.misc) query.misc = keywords_parser(query.misc)
                this.send(query)
            },
            format(type, data) {
                switch (type) {
                    case "text": return format(data)
                    case "spell.name": return format(data)
                    case "spell.components": return data
                        .replace(/\bV\b/g, "Verbal")
                        .replace(/\bS\b/g, "Somatic")
                        .replace(/\bM\b/g, "Material")
                        .replace(/\bF\b/g, "Focus")
                        .replace(/\bDF\b/g, "Divine Focus")
                        .replace(/\bE\b/g, "Emotional")
                        .replace(/\bT\b/g, "Thought")
                        .replace(/\bM_DF\b/g, "Material & Divine Focus")
                        .replace(/\bF_DF\b/g, "Focus & Divine Focus")
                    default: return data
                }
            },
            delayed_submit() {
                clearTimeout(data.delay)
                data.delay = setTimeout(() => this.submit(), 100)
            }
        },
        computed:{
            results() { return (this.$data.response||{results:[]}).results }
        },
        mounted() {
            document.querySelector("input[name='query']").focus()
        }
    })

    function keywords_parser(input) {
        //Création du regex (si inexistant)
        if (!keywords_parser.regex) {
            let stopwords = ['did', "doesn't", 'she', 'mightn', 'with', 'for', 'off', "should've", 'o', "couldn't", 'because', 'in', 'and', 'some', 'i', 'not', 'then', 'should', 'ours', 'having', "didn't", "haven't", 'more', "hadn't", 'that', 'mustn', "needn't", "mightn't", 'we', 'those', 'is', "isn't", 'do', 'd', "don't", 'a', 'by', 'out', 'y', 'whom', 'into', 'too', 'during', 'now', 'herself', 'its', 'ourselves', 'the', 'no', 'll', "won't", 'had', 'if', "aren't", "that'll", "hasn't", 'only', 'all', "she's", 'you', 'on', 'couldn', 'from', 'shan', 'while', 'yours', 'them', 'most', 'wasn', 'are', 'hadn', 'have', 'but', 'there', 'myself', 'up', 'where', 'hers', 'these', 'about', 'just', 'it', 'your', 'over', 'how', 'will', "shan't", 'does', 'weren', 'as', "weren't", 'so', 'has', 'needn', "you're", 'didn', 'being', 'isn', 'm', 'he', 'nor', 'few', 'won', 'at', 'itself', 'each', 'until', 'when', "you've", 'be', 've', 'than', 'what', 'themselves', 'under', 'once', 'ain', 'which', 'don', 'below', "you'll", 'wouldn', 'hasn', "wasn't", 'doing', 'above', 'or', "wouldn't", 'down', 'other', 'yourself', 'theirs', 'they', 'this', 'our', 'ma', 'of', 'through', 'me', 'to', 'again', 'haven', 'an', 'between', 'before', 'their', 'her', 's', 't', "you'd", 'very', 'both', 're', 'yourselves', 'here', 'same', 'can', 'aren', 'further', 'any', 'shouldn', 'himself', 'am', 'him', 'doesn', "it's", 'were', 'against', "mustn't", 'was', 'his', 'why', 'who', 'such', 'my', 'own', "shouldn't", 'after', 'been']
            keywords_parser.regex = new RegExp(`\\b(${stopwords.join("|")})\\b`, "gi")
        }
    
        //Suppression des caractères spéciaux et des stopwords
        input = input.toLocaleLowerCase().replace(/[^a-z'\s]/g, ' ')
        input = input.replace(keywords_parser.regex, '')
    
        //Récupération des mots-clés
        let keywords = [...new Set(input.split(" ").map(word => stemmer(word)).filter(word => word.length > 2))]
        return keywords
    }

})()

