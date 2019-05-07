const cheerio = require('cheerio');
const request = require('request-promise')
const fs = require('fs')
const stemmer = require('./porter.stemmer.min.js')


// Pour chacune des bestiaires on récupère les urls des monstres

URL_BESTIAIRE_1 = 'http://legacy.aonprd.com/bestiary/monsterIndex.html'
URL_BESTIAIRE_2 = 'http://legacy.aonprd.com/bestiary2/additionalMonsterIndex.html'
URL_BESTIAIRE_3 = 'http://legacy.aonprd.com/bestiary3/monsterIndex.html'
URL_BESTIAIRE_4 = 'http://legacy.aonprd.com/bestiary4/monsterIndex.html'
URL_BESTIAIRE_5 = 'http://legacy.aonprd.com/bestiary5/index.html'
URLS = [URL_BESTIAIRE_1, URL_BESTIAIRE_2, URL_BESTIAIRE_3, URL_BESTIAIRE_4, URL_BESTIAIRE_5]

// On construit un set contenant tout les monstres
monsters = new Set()
// On construit une liste contenant toutes les spells
let spells = [];

console.log('\t\t\x1b[34m%s\x1b[0m','+---------------------------+');
console.log('\t\t\x1b[35m%s\x1b[0m','| Welcome to PROLOCRAWL 7.0 |');
console.log('\t\t\x1b[34m%s\x1b[0m','+---------------------------+\n');


// Crawl monstre/spells
build_db(URLS)


/**
 * Crawl les monstres de http://legacy.aonprd.com/ et les spells de http://www.dxcontent.com/.
 * Afin d'obtenir la liste des monstres -> spells et des spells.
 * Stock ces listes au format JSON.
 * @param {Array} URLS Liens des bestiaires que l'ont veut crawler.
 */
async function build_db(URLS){

    // Crawl les spells
    await crawl_spells();

    // Crawl la liste des monstres -> spells
    for(let i=0; i< URLS.length; i++){
        await get_monsters_url(URLS[i]).then(data => push_bestiaire(data));
    }
    console.log('Writing into monster.json...')
    fs.writeFileSync("../resources/JSON/monsters.json", JSON.stringify([...monsters]))
    console.log('Done')
}

/**
 * Pour chacun des monstres contenu dans data,
 * on crée un objet contenant son nom et sa liste des spells
 * @param {Array} data URLS des monstres à crawler
 */
async function push_bestiaire(data) {
    for(let i=0; i<data.length; i++){
        let f = await scrap_monster(data[i]);
        console.log(f);
        if(f !== null)
            monsters.add(f);
    }
}

/**
 * Extrait à partir d'une URL,
 * le nom du monstre ainsi que sa liste de spells.
 * @param {String} url Url du monstre à crawler
 */
async function scrap_monster(url){
    return request(url).then(function(html){
        return new Promise(function (resolve) {
            let $ = cheerio.load(html);
            let name = hsv_to_text(url.split('#')[1]);
            let spells= new Set()
            $('[id="'+url.split('#')[1]+'"]').nextUntil('h1').find('a').each((i,elem) =>{
                if(/\.\.\/coreRulebook\/spells\//.test(elem.attribs.href)){
                    let spellname = elem.attribs.href.split('#')[1]
                    spellname = spellname === undefined ? camel_case_to_text(elem.attribs.href.split('/')[elem.attribs.href.split('/').length-1].split('.')[0]): 
                    hsv_to_text(spellname);
                    spells.add(spellname);
                }
            });
            resolve({name: name.trim(), spells:[...spells], url:url})
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error while crawling monster from " + url);
        });
    }).catch(e => console.error(e));
}

/**
 * Extrait à partir d'une URL d'un bestiaire,
 * les urls des monstres à crawler.
 * @param {String} url url contenant une liste d'urls de monstres 
 */
async function get_monsters_url(url){
    return request(url).then(function(html){
        return new Promise(function (resolve) {
            let $ = cheerio.load(html);
            let monsters_url = [];
            monsters_li = $('.body-content .body #monster-index-wrapper li a').each((i,elem)=>{
                if(/\.html\#/.test(elem.attribs.href)){monsters_url.push(url.substring(0,url.lastIndexOf('/')+1)+ elem.attribs.href)}
            });
            resolve(monsters_url);
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error while crawling monsterlist from " + url);
        });
    });
}

/**
 * Transforme un texte en camel case, en texte normal lowercase.
 * @param {String} str Texte en camel case.
 */
function camel_case_to_text(str){
    return str.replace( /([A-Z])/g, " $1" ).toLowerCase().trim()
}

/**
 * Transforme un texte au format hsv, en texte normal lowercase.
 * @param {String} str Texte au format hsv.
 */
function hsv_to_text(str){
    return str.toLowerCase().replace(/-/g, ' ').trim()
}


/**
 * Crawl la liste des spells.
 * La stock sous format JSON.
 */
async function crawl_spells(){
    console.log('\n\x1b[5m%s\x1b[0m', 'Starting to scrap the spells...');

    for(let id=1; id<=1973; id++) {
        let url = `http://www.dxcontent.com/SDB_SpellBlock.asp?SDBID=${id}`;
        await scrap_spell(url,id);
    }

    console.log("Removing duplicates...")
    // Remove duplicated spells (shitty website)
    let size = spells.length
    spells = spells.filter((spell, index, self) =>
        index === self.findIndex((s) => (
            s.name === spell.name
        ))
    )
    console.log(`Succesfully removed duplicates ${size - spells.length}`)

    console.log('\n\x1b[32m%s\x1b[0m', 'Success !');
    console.log('\n\x1b[5m%s\x1b[0m', `Writing ${spells.length} spells to resources/JSON/spells.json...`);
    if(!fs.existsSync('src/resources/JSON')){
        fs.mkdirSync('src/resources/JSON');
    }

    await fs.writeFile('src/resources/JSON/spells.json', JSON.stringify(spells), function(err) {
        if (err) {
            console.log('\n\x1b[31m%s\x1b[0m', err);
        } else {
            console.log('\n\x1b[32m%s\x1b[0m', 'Spells succesfully saved !');
        }
    });
    
}

/**
 * Scrap une spell à partir d'une URL
 * @param {String} url url du spell à crawl
 * @param {number} i indice de l'url
 */
async function scrap_spell(url,i) {
    return request(url).then(function(html){
        return new Promise(function (resolve,reject) {
            let $ = cheerio.load(html);
            if($('.SpellDiv .heading').text() === "") {
                console.log('\n\x1b[33m%s\x1b[0m', `INFO SGBD ${i}: Empty URL ` + url);
                resolve();
            }

            let spell = {}
            spell.name = format_spell_name($('.SpellDiv .heading').children()['0'].children[0].data)
            spell.components = get_components($('.SpellDiv .SPDet').children()['3'].next.data)
            spell.description = $('.SPDesc').text()
            spell.keywords = keywords_parser(spell.description)
            spell.url = url

            $('b').each(function(i, elem) {
                let attr = $(elem).text().replace(/\'/,'')
                if(attr !== "Components"){
                    if(elem.next){
                        spell[attr] = elem.next.data.replace(/;/g,'').trim()
                    }
                    if((attr === "Level")&&/[A-Z]/g.test(elem.next.data)){
                        spell[attr] = elem.next.data.replace(/[A-Z]/g, '')
                    }
                    if((attr === "School")&&/see/g.test(elem.next.data)){
                        spell[attr] = 'universal'
                    }
                }
              });

   
            let color = (i % 2 == 0) ? '\n\x1b[94m%s\x1b[0m' : '\n\x1b[1m%s\x1b[0m';
            console.log(color, `Name: ${(spell.name+" ".repeat(30)).substr(0, 30)} | level: ${(spell['Level']+" ".repeat(28)).substr(0, 28)} | Components: ${(spell.components+" ".repeat(20)).substr(0, 10)} |`);
            resolve(spells.push(spell));
        }, function(){
            console.log('\n\x1b[31m%s\x1b[0m', "Error for url " + url);
        });
    });
    
}

/**
 * Format le nom d'une spell
 * @param {String} str nom de la spell
 */
function format_spell_name(str){
    return str.toLowerCase().replace(/[\'\/]/g,' ').replace(/,/g, '').trim()
}

/**
 * Transforme une string contenant les components en array.
 * @param {String} str_compo String components
 */
function get_components(str_compo) {
    const regex_compo = /([A-Z]+\/[A-Z]+|[A-Z]+)/;
    let raw_components = str_compo.replace(/\(.+\)/gm, '').split(',')
    let components = [];
    raw_components.forEach(function(item){
        let matchs = item.match(regex_compo);
        if(matchs !== null){
            components.push(matchs[0].replace(/\//gm,"_").replace(/DF_F/gm,"F_DF").replace(/DF_M/gm, "M_DF"))
        }
    });
    return components;
}


function keywords_parser(input) {
    //Création du regex (si inexistant)
    if (!keywords_parser.regex) {
        let stopwords = ['did', "doesn't", 'she', 'mightn', 'with', 'for', 'off', "should've", 'o', "couldn't", 'because', 'in', 'and', 'some', 'i', 'not', 'then', 'should', 'ours', 'having', "didn't", "haven't", 'more', "hadn't", 'that', 'mustn', "needn't", "mightn't", 'we', 'those', 'is', "isn't", 'do', 'd', "don't", 'a', 'by', 'out', 'y', 'whom', 'into', 'too', 'during', 'now', 'herself', 'its', 'ourselves', 'the', 'no', 'll', "won't", 'had', 'if', "aren't", "that'll", "hasn't", 'only', 'all', "she's", 'you', 'on', 'couldn', 'from', 'shan', 'while', 'yours', 'them', 'most', 'wasn', 'are', 'hadn', 'have', 'but', 'there', 'myself', 'up', 'where', 'hers', 'these', 'about', 'just', 'it', 'your', 'over', 'how', 'will', "shan't", 'does', 'weren', 'as', "weren't", 'so', 'has', 'needn', "you're", 'didn', 'being', 'isn', 'm', 'he', 'nor', 'few', 'won', 'at', 'itself', 'each', 'until', 'when', "you've", 'be', 've', 'than', 'what', 'themselves', 'under', 'once', 'ain', 'which', 'don', 'below', "you'll", 'wouldn', 'hasn', "wasn't", 'doing', 'above', 'or', "wouldn't", 'down', 'other', 'yourself', 'theirs', 'they', 'this', 'our', 'ma', 'of', 'through', 'me', 'to', 'again', 'haven', 'an', 'between', 'before', 'their', 'her', 's', 't', "you'd", 'very', 'both', 're', 'yourselves', 'here', 'same', 'can', 'aren', 'further', 'any', 'shouldn', 'himself', 'am', 'him', 'doesn', "it's", 'were', 'against', "mustn't", 'was', 'his', 'why', 'who', 'such', 'my', 'own', "shouldn't", 'after', 'been']
        keywords_parser.regex = new RegExp(`\\b(${stopwords.join("|")})\\b`, "gi")
    }

    //Suppression des caractères spéciaux et des stopwords
    input = input.toLocaleLowerCase().replace(/[^a-z'\s]/g, ' ')
    input = input.replace(keywords_parser.regex, '')
    input = input.replace(/[^a-z\s]/g, '')

    //Récupération des mots-clés
    let keywords = [...new Set(input.split(" ").map(word => stemmer.stemmer(word)).filter(word => word.length > 2))]
    return keywords
}