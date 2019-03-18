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

struct TRecuit
{
	double	Temperature;				//Temperature courante du systeme
	int		TempInit;					//Temperature initiale du systeme 
	int		NoPalier;					//Compteur de palier pour la "Dur�e de la temp�rature" ou "Longueur d'�poque".
										//Dur�e de la temp�rature: nombre de r�p�titions � effectuer pour chaque valeur de la temp�rature.
	int		Delta;						//D�gradation courante entre la solution Courante et la solution Next
	double	Alpha;						//Schema de "reduction de la temp�rature" : Temp(t+1) = Alpha * Temp(t)
	int		NbPalier;					//Schema de "Duree de la temp�rature": Duree = NB_EVAL_MAX/NbPalier

	int		CptEval;					//**COMPTEUR DU NOMBRE DE SOLUTIONS EVALUEES. SERT POUR CRITERE D'ARRET.
	int		NB_EVAL_MAX;				//**CRITERE D'ARRET: MAXIMUM "NB_EVAL_MAX" EVALUATIONS.
};

#endif