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
#include <time.h>
#include <tuple>
using namespace std;


struct TProblem							//**Définition du problème:
{
	std::string Nom;					//**Nom du fichier de données
	int NbVilles;						//**Taille du probleme: Nombre de villes
	std::vector<std::vector <int> > Distance;		//**Distance entre chaque paire de villes. NB: Tableau de 0 à NbVilles-1.  Indice 0 utilisé.
};

struct TSolution						//**Définition d'une solution: 
{
	std::vector<int> Seq;				//**Indique la séquence dans laquelle les villes sont visitées. NB: Tableau de 0 à NbVilles-1.
	long FctObj;						//**Valeur de la fonction obj: Distance totale.	
};

struct TRecuit
{
	double	Temperature;				//Temperature courante du systeme
	int		TempInit;					//Temperature initiale du systeme 
	int		NoPalier;					//Compteur de palier pour la "Durée de la température" ou "Longueur d'époque".
										//Durée de la température: nombre de répétitions à effectuer pour chaque valeur de la température.
	int		Delta;						//Dégradation courante entre la solution Courante et la solution Next
	double	Alpha;						//Schema de "reduction de la température" : Temp(t+1) = Alpha * Temp(t)
	int		NbPalier;					//Schema de "Duree de la température": Duree = NB_EVAL_MAX/NbPalier

	int		CptEval;					//**COMPTEUR DU NOMBRE DE SOLUTIONS EVALUEES. SERT POUR CRITERE D'ARRET.
	int		NB_EVAL_MAX;				//**CRITERE D'ARRET: MAXIMUM "NB_EVAL_MAX" EVALUATIONS.
};

#endif