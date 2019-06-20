# 🧠 Apprentissage machine - Notes de cours

## Introduction

#### Historique
- 1943: Apparition du neurone artificiel (McCullock & Pitts).
- 1957: Perceptron (Rosenblatt)
- 1986: Backpropagation

#### Pourquoi ?
* Environnements inconnus
* Agent qui s'améliore avec l'expérience

#### Applications
* Classification
* Régression/prédiction
* Clustering d'éléments similaires
* Renforcement
* Réduction de dimensionnalité 

#### Learning element 
* Elément de performance (agent logique, basé sur l'utilité, exploration, ...)
* Composant fonctionnel (fonction d'évaluation, de perception/action, de transitions, ...)
* Représentation (fonction linéaire avec poids, axiomes, réseau neuronal, ...)
* Feedback (victoire/défaite, résultat, ...)

#### Types d'apprentissage
* **Supervisé** : Exemples d'entrées et sorties disponible, l'objectif est d'obtenir une fonction qui associe l'un à l'autre
* **Semi-supervisé** : Les données d'apprentissage n'ont pas toujours une étiquette
* **Non-supervisé** : Extraction des patterns dans les entrées sans direction spécifique sans aucun feedback. Les résultats sont à interpréter.
* **Renforcement** : Apprend en fonction des récompenses/punitions suite à ses actions.

#### Représentation des données
Séquence de valeurs réelles/entères, matrices, vecteurs one-hot, bag-of-words ou tf-idf, ...

#### Flow typique du ML
1. **Preprocessing du dataset** : Feature extraction/scaling/selection, réduction de dimensionnalité, échantillonage, ...
2. **Apprentissage** : Sélection du modèle, cross-validation, ...
3. **Evaluation** : Métriques, optimisation des hyper-paramètres, ...
4. **Prédiction**

## Vocabulaire et concepts

##### Epoch
Un passage complet sur l'ensemble de données.

##### Batch
Un sous-ensemble des données d'entraînement lorsque celui-ci est trop volumineux pour être utilisé entièrement à chaque fois.

Dans ce cas, on ajoute la notion d'*itération* qui est le nombre de batches nécessaire pour compléter une epoch

##### Feature scaling 
Normalise les données suivant une échelle (min-max, z-score, ...) pour éviter les problèmes avec les modèles qui utilisent la notion de distance

##### Data imbalance 
Phénomène qui se produit lorsque la proportion d'exemples de chaque classe est très différente, ce qui tend à favoriser les classes avec un grand nombre d'échantillons

##### Sous-apprentissage et sur-apprentissage
<img src="imgs/overfit.png" width="500">

##### Compromis biais-variance
Un haut biais permet un apprentissage généralement plus rapide et simple, mais procure de moins bonne performances sur les problèmes complexes (e.g. logistic regression, linear regression, ...).

Une haute variance permet d'impacter l'estimation de la fonction cible est d'être plus réceptif aux particularités des données, ce qui est désirable mais dans une certaine mesure afin d'éviter l'overfitting (e.g. arbres de décisions, knn, svm, ...).

C'est donc un compromis à faire. On peut utiliser par exemple la régularisation L2 (qui introduit un nouveau biais qui pénaliser les valeurs extrêmes de poids, à condition d'avoir fait du feature scaling).

##### Minibatch
Stochastic gradient descent avec k > 1.

##### Backpropagation
Algorithme qui permet de calculer les dérivées partielles d'une fonction de coûts complexe (e.g. inégale et non convexe).

On part du vecteur d'erreur de la dernière couche, pour calculer l'erreur de la couche précédente qui est calculé à partir de la dérivée de la fonction d'activation.

<img src="imgs/backpropagation.png" width="300">

##### Paramétrique et non-paramètrique
**Paramétrique** :
Estimation des paramètres à partir des données d'entraînement. Le nombre de paramètres est indépendant du nombre de données (e.g. perceptron, SVM linéaire, logistic regression, ...).

L'avantage des modèles paramètriques est qu'ils sont généralement plus simple, l'apprentissage est rapide. ?éanmoins ils sont souvent limités aux problèmes simples.

**Non-paramétrique** :
Le nombre de paramètres augmente avec la taille des données (e.g. DT, SVM, KNN, ...).

L'avantage des ces modèles est qu'ils sont généralement plus flexibles et puissant, mais ils sont plus lent à entrainer et nécessite plus de données d'entrainement. Le risque de sur-apprentissage est également plus élevé.

Généralement les méthodes non-paramétriques sont utiles lorsque l'on a beaucoup de données et aucune connaissance au préalable, et qu'on veut éviter de choisir les features.

## Classification simple

### Perceptron
<img src="imgs/perceptron_schema.png" width="500">

Le perceptron apprend les poids optimaux à multiplier avec les entrées pour déterminer si le neurone s'active ou non. La fonction d'activation est celle de Heaviside (+1 si positif, -1 si négatif). Il s'agit donc d'un outil de classification binaire.

<img src="imgs/perceptron.png" width="300">

A noter que w<sub>0</sub>x<sub>0</sub> = ϑ est appelé le biais.

Le principe est le suivant : 
1. Les poids sont initalisés à ≈ 0
2. Pour chaque exemple d'entraînement x<sup>(i)</sup> :
  1. On calcule la sortie estimée y<sup>(i)</sup>
  2. On met à jour les poids w<sub>j</sub> += Δ w<sub>j</sub>

<img src="imgs/perceptron_weight.png" width="200">

*η* est un nombre entre 0 et 1 qui constitue le learning rate.

A noter que Δ w<sub>j</sub> est nul si la prédiction est correcte, donc le poids ne changera pas.

#### Remarques
Si le dataset n'est pas séparable linéairement, le perceptron bouclera à l'infini.

Il est possible d'étendre le perceptron pour faire de la classification multi-classe par du *One-vs-All*, qui consiste à créer un classeur spécialisé dans la détection d'une classe en particulier, puis de déterminer quel classeur est actif pour tel entrée.

### Adaptive linear neurons (Adaline)
<img src="imgs/adaline_schema.png" width="500">

Sur le même principe que le perceptron, toutefois la fonction d'activation est linéraire, ce qui permet d'avoir des sorties continues plutôt que binaire. On rajoute parfois un *quantizer* pour la prédiction de classe.

On charche à optimiser la fonction de coûts *Sum of Squared Errors* (SSE) entre les sorties et les vraies classes :

<img src="imgs/sse.png" width="200">

On utilise pour cela l'algorithme du gradient, notamment parce que J(w) est convexe mais aussi parce que c'est très rapide.

<img src="imgs/gradient.png" width="200">

Toutefois, on utilise généralement la *descente de gradient stochastique* (SGD) pour éviter l'utilisation du dataset complet et accélerer le temps de calcul. Dans ce cas, il faut veiller à mélanger le dataset pour obtenir des résultats satisfaisants. Un learning rate adaptatif est particulièrement adapté avec le SGD. 

<img src="imgs/sgd.png" width="200">

#### Remarques
La mise à jour des poids se fait sur le dataset en entier, à l'inverse du perceptron où elle se fait exemple par exemple.

Le feature scaling permet d'accélerer la descente du gradient.

Une valeur de learning rate *η* forte peut empêcher de converger (si le pas du gradient est trop élevé) tandis qu'une valeur faible augmentera considérablement le temps de calcul.

## Classification

### Logistic regression
<img src="imgs/logistic_schema.png" width="500">

La fonction d'activation est une sigmoïde (fonction logistique). Elle se base sur le principe du rapport des chances (odds ratio), c'est-à-dire qu'une échantillon appartienne à une certaine classe étant donné ses attributs.

La fonction de coûts est modifiée pour utilisée les logarithmes (Log-likelyhood) afin d'éviter les underflow, en plus de trasformer les produits en somme, ce qui en soit les calculs plus faciles.

#### Remarques
La logistic regression est plus sensible aux outliers. Il s'agit également d'un modèle simple qui est facile à mettre en place et à jour.

### Support vector machine (SVM)
<img src="imgs/svm_schema.png" width="500">

Le principe est de maximiser les marges (distance entre un hyperplan et les échantillons les plus proches de ce plan, a.k.a support vectors) entre les classes, dans le but d'éviter l'overfitting et permettre une meilleure généralisation.

Un avantage du SVM est de pouvoir faire de la kernalisation. Il s'agit d'utiliser un noyau qui exploite des combinaisons non-linéaires et des attrinuts originaux en les projetant dans un espace à plus haute dimension, dans laquelle ils deviennent potentiellement séparables (e.g. radial basis function kernel).

![Kernel](imgs/kernel.png)

#### Remarques
Les SVMs se concentrent principalement sur les échantillons à la frontière des classes. En pratique, les résultats sont similaires ) la logistic regression.

Si on utilise un kernel, il est nécessaire d'entraîner le SVM dans la dimension supérieure (logique) : il faut donc transformer toutes les entrées auparavant.

Le *gamma* du RBF kernel détermine l'influence des échantillons.

### Arbres de décision
<img src="imgs/decision_tree_schema.png" width="500">

Le principe est d'exploiter les attributs de l'ensemble de données pour apprendre une série de "questions" pour inférer les classes. Chaque noeud sépare les données qui permettent d'obtenir le plus grand gain d'information, et ce processus est répété jusqu'à ce que les feuilles soient *pures* (i.e. ne représentent qu'une seule classe).

On teste sur le dataset du parent *D<sub>p</sub>* la séparation selon l'attribut *f*, en fonction de l'impureté *I* et du nombre d'échantillons *N* du parent et des enfants :

<img src="imgs/info_gain.png" width="300">

L'impureté (qu'on cherche à réduire), est calculée selon l'entropie (elle-même calculée sur la proportion d'échantillons appartenant à une certaine classe pour un certain noeud). C'est-à-dire qu'elle sera nul si tous les échantillons sont dans la même classe et maximale si tous les échantillons sont différents.

L'indice gini peut aussi être utilisé pour le calcul de l'impureté. Celui-ci est similaire à l'entropie mais se base sur la probabilité d'erreur de classification.

#### Remarques
Généralement, on fait une séparation binaire pour gagner en temps de calcul.

### Random forest
Il s'agit d'un ensemble d'arbres de décisions (estimators), dont la classification se fait au vote majoritaire sur les k arbres.

Les performances sont généralement meilleures qu'avec un seul arbre.

### K-nearest neighbors (KNN)
<img src="imgs/knn.png" width="300">

Le principe est de se dire que les éléments proches les uns par rapport aux autres sont probablement de la même classe.

L'algorithme est le suivant :
1. Choisis un nombre de voisins *k* et une mesure de distance
2. Trouver les *k* voisins les plus proches à classer
3. Vote à la majorité (pondérée ou non)

On utilise généralement une distance de minkowski.  

Un inconvénient majeur de cette méthode est que l'ensemble des exemples d'entrainement doit être gardé en mémoire, et par conséquent les knn sont peu performants pour une dimensionnalité élevée.

## Ensemble learning
<img src="imgs/ensemble_learning.png" width="400">

On part du principe qu'un ensemble de classeur plus faibles performent mieux qu'un seul très bon classeur.

A partir d'un ensemble d'entraînement, un certain nombre de classeurs sont produits (ceux-ci peuvent potentiellement être différents algorithmes ou sous-ensemble d'entraînement).

### Bagging (bootstrap aggregation)
<img src="imgs/bootstrap.png" width="400">

Le principe est d'utiliser différents sous-ensemble d'apprentissage (bootstrap) aléatoire avec remise.

Chaque bootstrap est utilisé pour entraîner un classeur.

Cela permet de réduire la variance, mais pas le biais. C'est pour cette raison qu'on utilise généralement des classeurs avec un biais faible (e.g. decision trees).

### Boosting
Le principe est de se focaliser sur les exemples difficiles à classer.

L'algorithme est le suivant :
1. Tirer un sous-ensemble du dataset pour entrainer un premier classeur
2. Tirer un autre sous-ensemble du dataset et y rajouter 50% des exemples mal classés par le classeur précédent pour entrainer le suivant
3. Répéter l'opération autant de fois que l'on veut
4. Faire un vote majoritaire

Théoriquement, cela permet de réduire à la fois le biais et la variance contrairement au bagging. Toutefois, la variance est souvent élevée dans les algorithmes de boosting.

#### Adaboost
Il s'agit d'un modèle de boosting qui utilise le dataset complet et dont les échantillons sont pondérés à chaque étape. Cela permet de construire un classeur qui apprend des erreurs passées.

![Adaboost](imgs/adaboost.png)

Exemple : 
1. Maximisation de la fonction de coûts
2. Ajustement des poids, et entrainement d'un second classeur
3. Ajustement des poids, et entrainement d'un troisième classeur
4. Vote majoritaire

## Text mining

### Représentation du texte

##### Bag of words
Vectorisation du vocabulaire par jeton et nombres d'occurences.

`hello world and hello universe` devient par exemple `[2, 1, 1, 1]` avec les jetons `{hello:0, world:1, and:2, universe:3}`.

Il s'agit des *raw terms frequencies* (tf) ou *n-gram*. 
Néanmoins comme le nombre d'occurence d'un mot ne représente pas son importance, on utilise le *term frequency - inverse document frequency* (tf-idf) qui compare le *tf* par rapport au nombre de document contenant le terme.

Il existe plusieurs déclinaisons de *tf-idf*. Celui-ci est généralement normalisé.

### Nettoyage du texte

Il faut généralement enlever les caractères spéciaux, les majuscules, etc.

#### Stemming

Le stemming consiste à supprimer les suffixes pour avoir en quelque sorte le radical (e.g. `running -> run`). 
Néanmoins l'efficacité dépends de la langue.

#### Lemmatisation

Consiste à retrouver les mots sous leur forme de base (e.g. `saw -> see`) en se basant sur un dictionnaire. 
Le problème de cette méthode est qu'elle est très couteuse.

#### Retirer les stop words

Les stop words sont des "mots vide" qui n'apportent pas grand-chose (e.g. les déterminants)

## Régression

Se démarque de la classification car la régression permet de prédire des tendances en terme de variable continue, et d'expliquer la relation entre une variable et une caractéristique.

Le coefficient de détermination R² est la méthode la plus utilisée pour évaluer les modèles de regression. 
Le R² est parfois égal à la corrélation au carré.

### Régression linéaire

<img src="imgs/regression.png" width="400">

La distance entre les échantillons et la ligne représente l’erreur de prédiction.
Il existe plusieurs méthodes pour trouver la droite de tendance: ordinary least squares (minimise la distance verticale par rapport à la droite au carré), random sample consensus (ransac, qui consiste à utiliser un échantillon pour calculer le modèle), ...

Pour observer les relations linéaires entre les variables, on utilise une matrice de corrélation (Pearson's r).
Si *r* vaut 0, il n'y a aucune corrélation.

#### Remarques
La régression linéaire est très sensible aux données aberrantes. On peut réduire l'impact selon la méthode employée (e.g. ransac).

Il est facilement possible de tomber en surapprentissage, qui peut être contrée par la régularisation: 
* ridge regression (l2) : ajoute une pénalité selon la somme des poids aux carrés
* lasso (l1) : ajoute une pénalité selon la somme des poids en valeur absolue
* elastic net : hybride l1/l2

### Régression polynomiale

<img src="imgs/regression2.png" width="400">

Il s'agit de réaliser une regression de degré supérieure, mais dans l'optique, cela reste un apprentissage de modèle linéaire.

Par exemple, pour un degré 2, si l'on pose `z = [x1, x2, x1x2, x1², x2²]`, il s'agit d'une régression linaire multiple en `z`.

### Regression trees

<img src="imgs/regression3.png" width="400">

Il s'agit d'arbre de décision, mais dont la fonction d'impureté est calculé par le MSE (mean squared error).

## Deep learning

Il s'agit d'un ensemble d'algorithmes conçus pour l'entrainement sur plusieurs couches.
La backgpropagation est ce qui a majoritairement rendu cela possible.
Il s'agit généralement des méthodes state-of-the-art pour les problèmes à données complexes (images, textes, voix).

Globalement pour faire du deep learning, il faut:
* Des fonctions différentiables et la backpropagation
* Des fonctions d'activation non linéaires
* Un optimiseur itératif
* Beaucoup de données

### Perceptron multicouche

<img src="imgs/mlp.png" width="400">

Il s'agit d'une sorte de "perceptron" avec des couches cachées connectées de façon dense (en effet, contrairement au perceptron de base, les unités sont continues et non binaires, et les activations sont sigmoïdales).
La couche de sortie est représenté par un vecteur one-hot pour faire de la classification multiple.

La propagation avant permet de calculer la sortie du réseau.
Chaque couche sert d'entrée à la couche suivante, sans qu'il n'y ait de boucle.

La fonction de coûts est la même que logistic regression, mais généralisée à toutes les unités du réseau.
Il faut donc calculer la dérivée partielle de la matrice de poids par rapport à tous les poids du réseau (sachant que les matrices n'ont pas forcément la même dimension).

#### Paramètres particuliers

* *l2* permet de réduire le surapprentissage
* *alpha* permet d'ajouter un momentum au gradient de l'epoch pour accélerer l'apprentissage
* *decrease* permet de réduire le learning rate au fil du temps

## Fonctions d'activation

Il existe deux familles : linéaire et non linéaire.

Sigmoid et tanh sont particulièrement adpaté pour prédire une probabilité comme sortie.
Elles sont dérivables et monotones. Tanh retourne des scores négatif pour les entrées négatives.

Relu (rectified linear unit) permet de débloquer l'apprentissage si sigmoid et tanh ne marche pas.
Moins de problème avec les gradient qui disparaissent (underflow), et les opérations sont plus simples (pas d'exponentielles).
Il existe des variantes comme le leaky relu.

<img src="imgs/activation.png" width="400">

## Réseau de neurones à convolutions (CNN)

Type particulier de réseau qui utilisent l'opération de convolution plutôt que la multiplication matricielle.
Particulièrement adapaté pour les données sous formes de grilles (images, sons, séquences).

La convolution prend une entrée et un kernel (filtre de convolution, souvent de taille impair) pour produire une sortie (feature map).

<img src="imgs/convolution.png" width="200">

Ce que l'on cherche à apprendre sont les kernels, qui sont généralement assez petits.
En soit, plutôt que d'apprendre des poids différents pour chaque entrée, un seul ensemble de poids (celui du kernel) est appris.
Un autre avantage des convolutions est la connectivité locale ainsi que le fait de pouvoir travailler sur des entrées de tailles variables.

Les filtres (kernel) peuvent: 
* être initialisé de façon aléatoire ou manuellement (edge detector, corner detector, ...)
* appris de façon supervisé ou non supervisé (via clustering et sur de petits morceaux d'images)

Le pooling permet d'essayer de rendre la représentation peu variable à de petits changements (e.g. translation): Max pooling sur un petit voisinage, etc. Cela peut également permettre de faire du downsampling, bien que ce soit possible directement avec la convolution (striding).Le pooling peut aussi permettre de garantir la taille d'une entrée si celle-ci est variable. Néanmoins, il est possible de tomber dans le sous-apprentissage avec trop de pooling.

Une propriété intéressante des CNNs est qu'ils peuvent retourner un objet structuré en haute dimension (e.g. la probabilité qu'un pixel appartienne à une classe), ce qui permet de faire des masques pour la segmentation d'images.

<img src="imgs/seg.png" width="150">

Les CNNs peuvent facilement être parallélisé pour fonctionner sur GPU, et ils diminuent globalement le nombre d'opération et l'utilisation de la mémoire.

#### Architectures majeurs

Lenet, Alexnet, VGG, Inception/GoogleNet, Resnet.
