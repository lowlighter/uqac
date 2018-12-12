Exercice sur **OpenCL** (Algorithme de Floyd).

#### Prérequis

1. Installer un SDK pour OpenCL ([SDK Intel](https://software.intel.com/en-us/intel-opencl)) si ce n'est pas fait
2. Ouvrir **OpenCL.vcxproj** dans Visual Studio
3. Cliquez *Projet → Propriétés de OpenCL* pour ouvrir les propriétés du projet
4. Dans *C/C++ → Général → Autres répertoires Include*, ajouter la valeur *$(INTELOCLSDKROOT)include*
5. Dans *Editeur de liens → Général → Répertoires de bibliothèques supplémentaires*, ajouter la valeur *$(INTELOCLSDKROOT)lib\x64*
6. Dans *Editeur de liens → Entrée → Dépendances supplémentaires*, ajouter la valeur *OpenCL.lib*
7. Lancer le programme en *Release x64* via le deboggeur local Visual Studio

#### Exemple d'exécution :

![Application](https://github.com/lowlighter/uqac/blob/master/parallels/4/demo.png)

*Note : Pour les grandes valeurs de N, le tableau ne sera pas affiché au commplet mais il sera possible d'afficher manuellement le contenu d'une cellule de la matrice de sortie.*