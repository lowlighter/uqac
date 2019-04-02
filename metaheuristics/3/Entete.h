#ifndef __ENTETE_H_
#define __ENTETE_H_

#include <cstdio>
#include <cstdlib> 
#include <string>
#include <fstream>
#include <iostream>
#include <iomanip>
#include <ctime>  
#include <cmath>
#include <vector>
#include <set>
#include <map>
#include <algorithm>
#include <climits>
#include <unordered_map>
using namespace std;


struct TProblem							//**Définition du problème:
{
	std::string Nom;					//**Nom du fichier de données
	int NbVilles;						//**Taille du probleme: Nombre de villes (incluant les villes de départ et d'arrivée qui sont fixes)
	std::vector<std::vector <long> > Distance;	//**Distance entre chaque paire de villes. NB: Tableau de 0 à NbVilles-1.  Indice 0 utilisé.
	std::vector<std::vector <int> > Pred;		//**Pour chaque ville: Liste des villes qui doivent d'abord être visitées. 
												//NB: Première dimension du Tableau de 0 à NbVilles-1. Deuxième dimension sera variable selon le nombre de prédécesseurs.
	std::vector<std::vector <int> > Succ;		//**Pour chaque ville: Liste des villes qui peuvent être visitées après (successeur). 
												//NB: Première dimension du Tableau de 0 à NbVilles-1. Deuxième dimension sera variable selon le nombre de successeurs.
};

struct TIndividu						//**Définition d'une solution: 
{
	std::vector<int> Seq;				//**Indique la séquence dans laquelle les villes sont visitées. NB: Tableau de 0 à NbVilles-1. NB: Positions 0 et NbVilles-1 sont fixes.
	bool Valide;						//**Indique si la solution respecte les contraintes de précédence. Vérifier lorsque la solution est évaluée.
	long FctObj;						//**Valeur de la fonction obj: Distance totale.	
};

struct TGenetic
{
	int		TaillePop;					//**Taille de la population (nombre d'individus)
	int		TaillePopEnfant;			//**Taille de la populationEnfant (nombre d'enfants)
	double	ProbCr;						//**Probabilité de croisement [0%,100%]
	double	ProbMut;					//**Probabilité de mutation [0%,100%] 
	int		Gen;						//**Compteur du nombre de générations

	int		CptEval;					//**COMPTEUR DU NOMBRE DE SOLUTIONS EVALUEES. SERT POUR CRITERE D'ARRET.
	int		NB_EVAL_MAX;				//**CRITERE D'ARRET: MAXIMUM "NB_EVAL_MAX" EVALUATIONS.
};

struct TVille 
{
	set<int> Adj;
	set<int> Pred;
};

#endif