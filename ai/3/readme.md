# Agent logique

* [Démonstration](https://lowlighter.github.io/access/5005d70afd47a09d181c918c16ca0c4346c3b70c/uqac/ai/3/)

### Interface

![Interface](https://github.com/lowlighter/uqac/blob/master/ai/3/resources/interface.png)

#### 1. Evénements et scores de l’agent
#### 2. Environnement
  * 🤷 : Indique la position de l'agent
  * 💩 : Indique la présence d'un monstre sur une case adjacente
  * 🦑 : Indique la présence d'un monstre
  * 💥 : Indique une case où l'agent a tiré dessus
  * 🍣 : Indique la présence d'un monstre mort
  * 💨 : Indique la présence d'une crevasse sur une case adjacente
  * 🕳 : Indique la présence d'une crevasse
  * 🌌 : Indique la présence d'un portail
#### 3. Base de connaissance de l'agent
  * Tous les signes utilisée dans la section *2. Environnement* ainsi que les suivants : 
  * 🦑❗ : Indique une case avec 1 💩 adjacent
  * 🦑❗❗ : Indique une case avec 2 💩 adjacents
  * 🦑❗❗❗ : Indique une case avec 3 💩 adjacents
  * 🦑❗❗❗❗ : Indique une case avec 4 💩 adjacents
  * 🕳❗ : Indique une case avec 1 💨 adjacent
  * 🕳❗❗ : Indique une case avec 2 💨 adjacents
  * 🕳❗❗❗ : Indique une case avec 3 💨 adjacents
  * 🕳❗❗❗❗ : Indique une case avec 4 💨 adjacents
  * 👁 : Indique que l'agent a exploré la case
  * *Previous position* : Indique la case précédente où se situer l'agent
  
#### Couleur des celulles
Respectivement de gauche à droite, les marqueurs de celulles non explorées de type : Ø < ❗ < ❗❗ < ❗❗❗ < ❗❗❗❗.

![1](https://github.com/lowlighter/ai3/blob/master/resources/cell.unexplored.png)
![2](https://github.com/lowlighter/ai3/blob/master/resources/cell.risky.low.png)
![3](https://github.com/lowlighter/ai3/blob/master/resources/cell.risky.med.png)
![4](https://github.com/lowlighter/ai3/blob/master/resources/cell.risky.high.png)
![5](https://github.com/lowlighter/ai3/blob/master/resources/cell.risky.max.png)

  
## Projet

### Instructions
Le projet est une application web, ce qui signifie qu’il peut être lancé directement dans le navigateur sans aucun autre prérequis.

Il accessible en ligne mais il est possible également de faire fonctionner l’application localement. 
Pour cela il faut :
1. Télécharger les ressources du projet
2. Ouvrir le fichier index.html dans un navigateur

Notez tout de même que le programme nécessite un navigateur récent et le fonctionnement n’est pas garanti sur les versions désuètes.

### Organisation des fichiers
* /sources
  * app.js : Paramètres de l’application
  * agent.js : Définition de l'agent
  * agent.effectors.js : Effecteurs de l'agent
  * agent.sensors.js : Capteurs de l'agent
  * environment.js : Définition de l'environnement
  * knowledge_base.js : Base de connaissances
  * rule.js : Règles
  * rule_base.js : Base de règles
* /resources 
  * Fichiers annexes contenant entre autres le moteur de rendu et les fonctions utilitaires.
* index.html : Point d’entrée de l’application


