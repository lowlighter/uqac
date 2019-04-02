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


struct TProblem							//**D�finition du probl�me:
{
	std::string Nom;					//**Nom du fichier de donn�es
	int NbVilles;						//**Taille du probleme: Nombre de villes (incluant les villes de d�part et d'arriv�e qui sont fixes)
	std::vector<std::vector <long> > Distance;	//**Distance entre chaque paire de villes. NB: Tableau de 0 � NbVilles-1.  Indice 0 utilis�.
	std::vector<std::vector <int> > Pred;		//**Pour chaque ville: Liste des villes qui doivent d'abord �tre visit�es. 
												//NB: Premi�re dimension du Tableau de 0 � NbVilles-1. Deuxi�me dimension sera variable selon le nombre de pr�d�cesseurs.
	std::vector<std::vector <int> > Succ;		//**Pour chaque ville: Liste des villes qui peuvent �tre visit�es apr�s (successeur). 
												//NB: Premi�re dimension du Tableau de 0 � NbVilles-1. Deuxi�me dimension sera variable selon le nombre de successeurs.
};

struct TIndividu						//**D�finition d'une solution: 
{
	std::vector<int> Seq;				//**Indique la s�quence dans laquelle les villes sont visit�es. NB: Tableau de 0 � NbVilles-1. NB: Positions 0 et NbVilles-1 sont fixes.
	bool Valide;						//**Indique si la solution respecte les contraintes de pr�c�dence. V�rifier lorsque la solution est �valu�e.
	long FctObj;						//**Valeur de la fonction obj: Distance totale.	
};

struct TGenetic
{
	int		TaillePop;					//**Taille de la population (nombre d'individus)
	int		TaillePopEnfant;			//**Taille de la populationEnfant (nombre d'enfants)
	double	ProbCr;						//**Probabilit� de croisement [0%,100%]
	double	ProbMut;					//**Probabilit� de mutation [0%,100%] 
	int		Gen;						//**Compteur du nombre de g�n�rations

	int		CptEval;					//**COMPTEUR DU NOMBRE DE SOLUTIONS EVALUEES. SERT POUR CRITERE D'ARRET.
	int		NB_EVAL_MAX;				//**CRITERE D'ARRET: MAXIMUM "NB_EVAL_MAX" EVALUATIONS.
};

struct TVille 
{
	set<int> Adj;
	set<int> Pred;
};

#endif