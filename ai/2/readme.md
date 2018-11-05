# Résolution d'un Sudoku par CSP

* [Démonstration](https://lowlighter.github.io/access/5005d70afd47a09d181c918c16ca0c4346c3b70c/uqac/ai/2/)

### Interface

![Interface](https://github.com/lowlighter/uqac/blob/master/ai/2/resources/interface.png)

1. Grille du Sudoku
2. Options de résolutions
3. Lance la résolution du Sudoku avec les options de résolution spécifiées
4. Stoppe la résolution du Sudoku qui est en cours
5. Vide la grille du Sudoku et l'initialise avec un nouveau puzzle
6. Vide la grille du Sudoku

#### Grille du Sudoku
- Les cases noires spécifient le puzzle de départ. 
- Les cases bleues représentent la solution en train d'être générée par le backtracking.

Pour éditer manuellement le Sudoku, il suffit de cliquer sur une case puis modifier sa valeur (les cases écrites manuellement deviendront automatiquement noires).
Les cases bleues seront réinitialisées avant chaque résolution, il n'est donc pas nécessaire de les nettoyer.

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
  * algo.backtracking.js : Implémentation du *Backtracking*
  * algo.basic.js : Implémentation de l'heuristique *Sans heuristique*
  * algo.mrv.js : Implémentation de l'heuristique *Minimum Remaining Values*
  * algo.degreeheuristic.js : Implémentation de l'heuristique *Degree Heuristic*
  * algo.lcv.js : Implémentation de l'ordonnanceur de valeurs *Least Constraining Values*
  * algo.ac3.js : Implémentation de l'*Arc-Consistency #3*
  * csp.js : Définition générale d'un *Constraint Satisfaction Problem*
  * csp.assignment.js : Définition générale d'un *Assignment* d'un CSP
  * csp.sudoku.js : Définition d'un *Sudoku* sous forme de CSP
* /resources 
  * makepuzzle.js : Générateur aléatoire de Sudoku (auteur original : Blagovest Dachev)
  * Fichiers annexes contenant entre autres le moteur de rendu et les fonctions utilitaires.
  * Textures et styles de l’interface graphique
* index.html : Point d’entrée de l’application
