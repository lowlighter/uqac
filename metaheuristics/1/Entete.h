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
#include <tuple>
using namespace std;


struct TProblem							//**D�finition du probl�me:
{
	std::string Nom;					//**Nom du fichier de donn�es
	int NbVilles;						//**Taille du probleme: Nombre de villes
	std::vector<std::vector <int> > Distance;		//**Distance entre chaque paire de villes. NB: Tableau de 0 � NbVilles-1.  Indice 0 utilis�.
};

struct TSolution						//**D�finition d'une solution: 
{
	std::vector<int> Seq;				//**Indique la s�quence dans laquelle les villes sont visit�es. NB: Tableau de 0 � NbVilles-1.
	long FctObj;						//**Valeur de la fonction obj: Distance totale.	
};

struct TAlgo
{
	int		CptEval;					//**COMPTEUR DU NOMBRE DE SOLUTIONS EVALUEES. SERT POUR CRITERE D'ARRET.
	int		NB_EVAL_MAX;				//**CRITERE D'ARRET: MAXIMUM "NB_EVAL_MAX" EVALUATIONS.
};

#endif