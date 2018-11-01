# Création d’un agent aspirateur

* [Démonstration](https://lowlighter.github.io/access/5005d70afd47a09d181c918c16ca0c4346c3b70c/uqac/ai/1/)

## Projet

### Instructions
Le projet est une application web, ce qui signifie qu’il peut être lancé directement dans le navigateur sans aucun autre prérequis.

Il accessible en ligne mais il est possible également de faire fonctionner l’application localement. 
Pour cela il faut :
1. Télécharger les ressources du projet
2. Ouvrir le fichier index.html dans un navigateur

Notez tout de même que le programme nécessite un navigateur récent et le fonctionnement des n’est pas garanti sur les versions désuètes.

Les paramètres de l’application (fréquences d’exécution, temps alloué à l’exploration, paramètres d’apprentissage, dimensions de la carte, probabilités des poussières et bijoux, fenêtre temporelle d’évaluation de la performance, etc.) peuvent être modifiés dans le fichier app.js. 
Il est conseillé de désactiver le cache du navigateur si ces valeurs sont modifiées.

### Organisation des fichiers
* /source
  * app.js : Paramètres de l’application
  * agent.js : Description générale de l’agent (initialisation, cycle de vie, etc.)
  * agent.sensors.js : Définition des capteurs de l’agent 
  * agent.effectors.js : Définition des effecteurs de l’agent
  * agent.explore.js : Définition des méthodes d’explorations de l’agent
  * environment.js : Description générale de l'environnement
  * environment.scores.js : Définition des mesures de performances de l’environnement
* /misc : Fichiers annexes contenant entre autres le moteur de rendu, les constantes et les fichiers de debug.
* /resources 
  * Textures et styles de l’interface graphique
* index.html : Point d’entrée de l’application

### Interface

![Interface](https://github.com/lowlighter/uqac/tree/master/ai/1/resources/interface.png)

1. Evénements de l’agent
2. Evénements de l’environnement
3. Statistiques sur l’environnement : Poussières aspirées, bijoux ramassés, bijoux aspirés, énergie consommée, poussières et bijoux générés par l’environnement, mesure de performance.
4. Rendu graphique de l’environnement
5. Rendu graphique de l’agent
6. Paramètres d’apprentissage
7. Type d’exploration
8. Plan d’action actuel

## Environnement

### Description
L'environnement dans lequel notre nouveau robot aspirateur T-0.1 évolue est notre luxueux manoir composé de 100 pièces. 
Celui-ci se salit très rapidement et nécessite donc une vigilance permanente de la part de notre robot, d’autant plus que les propriétaires des lieux ont tendance à égarer leurs précieux bijoux. 

### Propriétés
L’environnement possède les propriétés suivantes :
* Complètement observable (l’agent a accès à l’intégralité de la carte en tout temps)
* Stochastique (le prochain état de l’environnement n’est pas entièrement déterminé par l’état actuel ou par les actions de l’agent)
* Épisodique (chaque épisode est indépendant et les actions passées ou futures des autres épisodes n’ont pas d’influence sur l’épisode actuel)
* Dynamique (l’environnement est amené à changer pendant pendant le cycle de vie de l’agent)
* Continu (l'événement est mis à jour en temps réel)
* Un agent (l’aspirateur T-0.1 est le seul agent de l’environnement) 

### Modélisation
Les données de l’environnement sont stockées dans un tableau de 10 cases par 10 cases (soit 100 au total). 
La fréquence à laquelle l’environnement évolue est 20Hz. 

Il est modélisé de façon à ce que l’agent ne puisse interagir avec lui uniquement que par l’intermédiaire de ses capteurs et de ses effecteurs.

##### Cycle de vie() [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/environment.js#L48-L66)]
```
1. Tant que vrai 
2.	C vaut une case aléatoire sélectionnée de la carte
3.	Si l’environnement n’est pas mis en pause, faire :
4.		Probabilité de générer une poussière sur C
5.		Probabilité de générer un bijou sur C
6. 	Mettre à jour le rendu
```

L’environnement peut être contrôlé manuellement pour tester les réactions de l’agent : 
* **Échap** : Mise en pause/reprendre l’exécution du cycle de vie de l’environnement
* **Clic gauche** : Place de la poussière sur une case
* **Clic droit** : Place un bijou sur une case

L’environnement est responsable de son propre rendu. 

### Génération aléatoire des éléments
La génération de poussières et de bijoux est réalisée, par défaut, selon une probabilité 20 fois par seconde (fréquence de 20Hz). A chaque itération la probabilité de génération de la poussière est réglée à 1/20 (soit en théorie une poussière par seconde avec une fréquence réglée à 20Hz), celle pour la génération de bijoux est à une probabilité de 1/100. 

La fréquence de génération ainsi que les probabilités sont évidemment ajustables afin de faire évoluer l’environnement de manière différente. La génération de poussières ne peut avoir lieu sur une case contenant déjà de la poussière (et inversement pour les bijoux). Par contre une case possédant de la poussière peut y voir apparaître un bijou (et inversement). L’agent devra alors ramasser le bijou avant d’y aspirer la poussière.

## Agent

### Type d’agent
L’agent aspirateur est un agent basé sur les buts. Nous le formulons par un problème à simple état :
* Etat initial : L’aspirateur est placé en haut à gauche
* Opérateurs : Aspirer, Ramasser bijou, Haut, Bas, Gauche, Droite
* Fonctions de succession {Action, Etat} : {Aspirer, La poussière est supprimée de la case} {Ramasser, Le bijou disparaît de la case} {Haut, l’aspirateur se déplace d’une case en haut} {Bas, l’aspirateur se déplace d’une case en bas} {Gauche, l’aspirateur se déplace d’une case à gauche} {Droite, l’aspirateur se déplace d’une case à droite}
* Le test de but : ramasser un bijou ou aspirer une poussière
* Coût du chemin : chaque action vaut 1

### Modélisation de l’action
Nous modélisons les actions dans l’environnement en utilisant le format “STRIP”. 
Les actions seront  réalisées via les effecteurs de l’agent.

#### Action “ASPIRER” (ASPIRE) :
* Prémisses :
  * Case contenant une poussière
  * Aspirateur positionné sur cette case
* Conséquences :
  * Poussière aspirée (même le bijou si il y en a un)
  * Case vidée
  * Aspirateur reste sur cette case

#### Action “RAMASSER BIJOU” (PICKUP) :
* Prémisses :
  * Case contenant un bijou
  * Aspirateur positionné sur cette case
* Conséquences :
  * Bijou ramassé
  * Aspirateur reste sur cette case

#### Action “SE DÉPLACER” (UP, DOWN, LEFT, RIGHT) :
* Prémisses :
  * Aspirateur positionné sur une case
* Conséquences :
  * Aspirateur positionné sur la case voisine (si elle existe)

### Modélisation de la perception
L’agent perçoit l’environnement par l’intermédiaire de ses capteurs. 
Celui-ci peut accéder au contenu de chaque pièce du manoir (vide, poussiéreuse, bijou) ainsi qu’à sa position actuelle.

### Fonction d’agent
Le cycle de vie de notre agent basé sur le but est le suivant : 

#### Cycle de vie() [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.js#L49-L75)]
```
1. Tant que l’agent est en vie, faire :
2.	Si aucune intention, faire :
3.		Mettre à jour les croyances sur l’environnement
4.		Mettre à jour désirs
5.		Mettre à jour les intentions via une exploration
6.	Si le plan d’action n’est pas vide, faire :
7.		Exécuter la première action 
```

### Etat mental
L’agent suit le modèle Belief Desire Intention : 
* **Beliefs** : Les croyances de l’agent sont les informations qu’il dispose sur son environnement. Elles contiennent l’état actuel du manoir (les cases vides, poussiéreuses, ou avec un bijou) et la position de l’agent à un instant donné.
* **Desires** : Les désirs représentent contiennent les buts de l’agent. Dans notre cas, il s’agit d’un tableau contenant une description partielle des états minimaux à atteindre. Par exemple, [[DUST, ASPIRE], [JEWEL, PICKUP]], indique que l’agent cherche à effectuer l’action “aspirer” s’il est sur une case poussiéreuse et ramasser s’il se trouve sur un bijou.
* **Intentions** : Les intentions de l’agent constituent son plan d’action afin de satisfaire ses désirs. Elles contiennent donc les prochaines actions de l’agent, et sont déterminées lorsque l’agent réalise son exploration.

### Exploration
Deux méthodes d’exploration ont été implémentées :
1. Non informée
2. Informée

Pour chacune de ces méthodes, les algorithmes de parcours sont différents, toutefois les méthodes suivantes sont communes : 
* **Test de but** : Vérifie si le noeud courant accomplit un but. Ici, nous avons considéré qu’un noeud répondait à un but s’il satisfaisait au moins un des désirs de l’agent
* **Expansion** : Génère les noeuds enfants du noeud passé en paramètre. Ici, les six actions possibles de l’agent définies précédemment sont prises en compte.
* **Reconstruction du plan d’action** : Génère le plan d’action à partir du résultat de l’exploration. Cela consiste à récupérer les actions à faire en remontant jusqu’au noeud racine.
[[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.explore.js#L205-L301)]

Les noeuds gardent en mémoire leur parent, l’action associée (i.e. la branche permettant d’arriver au noeud) ainsi que des croyances altérées (données). 
Une fois un but trouvé, le plan d’action est reconstruit.

Nos algorithmes d’exploration implémentent un système de temps imparti (timeout) qui détermine le temps maximal d’exécution. 
Si celui-ci est dépassé, une exception est levée et l’agent rate son exploration et un plan d’action vide est retourné. 
Cela permet d’éviter que l’agent ne soit inactif durant une durée trop importante.

#### Exploration non-informée [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.explore.js#L26-L88)]

Nous avons choisi la recherche itérative en profondeur (Iterative Deepening Search) pour l’exploration non-informée de notre agent. 
Celle-ci possède l’avantage d’être optimale comme le parcours en largeur (Breadth First Search) pour les coûts de transition uniforme (ce qui est le cas ici, étant donné que chaque action coûte un unique point d’énergie) tout en ayant l’avantage d’une complexité mémoire réduite à l’instar du parcours en profondeur (Depth First Search).

Il a été implémenté de façon récursive avec le pseudo-code suivant :

##### Recherche itérative en profondeur (croyances) [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.explore.js#L41-L62)]
```
1. Le noeud racine est initialisé avec les croyances actuelles
2. Pour une profondeur de 0 à l’infinie faire :
3.	Parcours en profondeur limitée (racine, profondeur)
```

##### Parcours en profondeur limitée (noeud, limite) [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.explore.js#L64-L88)]
```
1. Si le noeud est un but, retourner le noeud
2. Si la profondeur limite est atteinte, retourner NULL
3. Pour chacun des enfants de ce noeud, faire :
4. 	N vaut Parcours en profondeur limitée (noeud, limite-1)
5.	Si N est défini, retourner N
6. Retourner NULL
```

Actuellement la profondeur maximale que nous ayons réussi à atteindre en 10 secondes avec cet algorithme est de 13. 
Avec des règles d’élagages (e.g. considérer que les actions “aspirer” et “ramasser” sont obligatoirement des noeuds feuilles), il aurait été possible d’augmenter cette valeur.

#### Exploration informée [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.explore.js#L90-L202)]

Nous avons choisi l’A* pour l’exploration informée de notre agent et la distance de Manhattan comme heuristique. 
Celui-ci a l’avantage d’être optimal avec l’heuristique choisie, ce qui nous permettra d’éviter de gaspiller de l’énergie inutilement dans notre cas.

Il a été implémenté avec le pseudo-code suivant :
##### Algorithme A* (croyances) [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.explore.js#L105-L173)]
```
1. Le noeud racine est initialisé avec les croyances actuelles
2. Initialisation de la frontière, des scores, de la liste ouverte, de la liste fermée ainsi que de la fonction d’heuristique
3. Tant qu’il reste un noeud dans la liste ouverte, faire :
4.	Retirer le noeud le plus désirable de la liste ouverte
5.	L’ajouter à la liste fermée
6.	Pour chacun des enfants de ce noeud, faire :
7.		Rien si l’enfant est dans la liste fermée
8.		Calculer son score
9.		S’il n’est pas dans la liste ouverte
10.			L’ajouter dans la liste ouverte
11.			Calculer sa désirabilité avec l'heuristique
12.		Mettre à jour son score s’il est meilleur
13.	Si le noeud est un but, retourner le noeud
14. Retourner NULL
```

L’exploration informée n’a jamais rencontrée d’échec d’exploration (temps imparti dépassé) avec les paramètres de base du projet.

#### Apprentissage [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.js#L115-L170)]
Par défaut, l’agent exécute son plan d’action dans son entièreté (i.e. 100%), cependant il est capable de réaliser un apprentissage basique afin de déterminer quel pourcentage d’actions à réaliser avant de réévaluer son plan d’action offre la meilleure performance.

La performance est mesurée par l’environnement de la façon suivante :
* +10 points pour chaque poussière aspirée
* +15 points pour chaque bijou ramassé
* -1 point pour chaque unité d'énergie consommée
* -3 points pour chaque poussière générée par l’environnement
* -50 points par bijou aspiré

Lorsque l’apprentissage est actif, à chaque fois qu’un nouveau plan d’action est défini, la méthode suivante est appelée :

##### Démarrer Apprentissage () [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.js#L123-L134)]
```
1. Ignorer si le plan d’action contient de moins de 3 actions
2. Enregistrer le score actuel 
3. Signaler qu’un nouvel épisode d’apprentissage a démarré
```


On ignore les plans d’actions de moins de 3 actions afin d’éviter les situations où l’agent boucle en continu, comme par exemple `[GAUCHE, DROITE]` ou `[HAUT, BAS]`.

L’apprentissage introduit les notions suivantes : 
* **R** : le ratio d’action effectué/planifié
* **RL** : ratio d’action effectué/planifié défini par la méthode d’apprentissage
* **D** : la variation de score au cours d’un épisode
* **DRL** : la variation de RL qui permet de définir le nouveau RL
* **IDRL** : l’impact de DRL sur RL

##### Continuer Apprentissage () [[code](https://github.com/lowlighter/uqac/blob/master/ai/1/sources/agent.js#L136-L169)]
```
1. Quitter si aucun épisode d’apprentissage n’est en cours
2. R vaut le ratio d’action effectué/planifié
3. Si le R est supérieur à RL, faire : 
4. 	D vaut la variation du score depuis le début de l’épisode
5.	Ajouter la paire RL, D dans l’historique d’apprentissage
6. 	Calculer la valeur moyenne de D pour chaque RL
7. 	Récupérer le RL le plus prometteur jusqu’à maintenant
8.	RL vaut RL + DRL (la valeur est borné entre 0% et 100%)
9	IDRL vaut 0.98 * IDRL
10. 	Le plan d’action actuel est supprimé, forçant l’exploration
```

La valeur DRL est calculée de la  façon suivante :
```
DRL = IDRL * ((1 - RL) * rand() - RL * rand()) 
```

Comme il est possible de le remarquer, la variation DRL est aléatoire. 
Plus RL est proche de 0%, plus DRL  a de chance d’être augmenté, et réciproquement, plus RL est proche de 100%  plus DRL a de chance d’être diminué. 
DRL est ensuite multipliée par IDRL.

IDRL diminue au cours du nombre d’épisode terminés (ligne 9 du pseudo code). 
La variation s’affaiblit donc au cours du temps. 
De cette manière, au début de l’apprentissage l’agent testera des valeurs plus ou moins exotiques au voisinage du meilleur RL qu’il ait trouvé afin d’en trouver un meilleur, puis finira éventuellement par converger vers un RL satisfaisant.

Voici par exemple les résultats que nous avons obtenu au cours de 40 épisodes :
* Exploration non informée : 76%
* Exploration informée : 81%
