#include <windows.h>
#include "Entete.h"
#pragma comment (lib,"TabouDLL.lib")  
//%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTANT: %%%%%%%%%%%%%%%%%%%%%%%%% 
//Le fichier de probleme (.txt) et les fichiers de la DLL (DescenteDLL.dll et DescenteDLL.lib) doivent 
//se trouver dans le répertoire courant du projet pour une exécution à l'aide du compilateur. Indiquer les
//arguments du programme dans les propriétés du projet - débogage - arguements.
//Sinon, utiliser le répertoire execution.

//*****************************************************************************************
// Prototype des fonctions se trouvant dans la DLL 
//*****************************************************************************************
//DESCRIPTION:	Lecture du Fichier probleme et initialiation de la structure Problem
extern "C" _declspec(dllimport) void LectureProbleme(std::string FileName, TProblem & unProb, TTabou &unTabou);

//DESCRIPTION:	Fonction d'affichage à l'écran permettant de voir si les données du fichier problème ont été lues correctement
extern "C" _declspec(dllimport) void AfficherProbleme (TProblem unProb);

//DESCRIPTION: Affichage d'une solution a l'écran pour validation
extern "C" _declspec(dllimport) void AfficherSolution(const TSolution uneSolution, int N, std::string Titre);

//DESCRIPTION: Affichage à l'écran de la solution finale, du nombre d'évaluations effectuées et de certains paramètres
extern "C" _declspec(dllimport) void AfficherResultats (const TSolution uneSol, TProblem unProb, TTabou unTabou);

//DESCRIPTION: Affichage dans un fichier(en append) de la solution finale, du nombre d'évaluations effectuées et de certains paramètres
extern "C" _declspec(dllimport) void AfficherResultatsFichier (const TSolution uneSol, TProblem unProb, TTabou unTabou, std::string FileName);

//DESCRIPTION:	Évaluation de la fonction objectif d'une solution et MAJ du compteur d'évaluation. 
//				Retourne un long représentant la distance totale de la trounée incluant le retour un point initial
extern "C" _declspec(dllimport) long EvaluerSolution(const TSolution uneSolution, TProblem unProb, TTabou &unTabou);

//DESCRIPTION:	Création d'une séquence aléatoire de parcours des villes et évaluation de la fonction objectif. Allocation dynamique de mémoire
// pour la séquence (.Seq)
extern "C" _declspec(dllimport) void CreerSolutionAleatoire(TSolution & uneSolution, TProblem unProb, TTabou &unTabou);

//DESCRIPTION: Copie de la séquence et de la fonction objectif dans une nouvelle TSolution. La nouvelle TSolution est retournée.
extern "C" _declspec(dllimport) void CopierSolution (const TSolution uneSol, TSolution &Copie, TProblem unProb);

//DESCRIPTION:	Libération de la mémoire allouée dynamiquement
extern "C" _declspec(dllimport) void	LibererMemoireFinPgm	(TSolution uneCourante, TSolution uneNext, TSolution uneBest, TProblem unProb);

//*****************************************************************************************
// Prototype des fonctions locales 
//*****************************************************************************************

//DESCRIPTION:	Création d'une solution voisine à partir de la solution uneSol. NB:uneSol ne doit pas être modifiée
TSolution GetSolutionVoisine (const TSolution uneSol, TProblem unProb, TTabou &unTabou);

//DESCRIPTION:	 Echange de deux villes sélectionnée aléatoirement. NB:uneSol ne doit pas être modifiée
TSolution	Echange			(const TSolution uneSol, TProblem unProb, TTabou &unTabou);


//******************************************************************************************
// Fonction main
//*****************************************************************************************
int main(int NbParam, char *Param[])
{
	TSolution Courante;		//Solution active au cours des itérations
	TSolution Next;			//Solution voisine retenue à une itération
	TSolution Best;			//Meilleure solution depuis le début de l'algorithme
	TProblem LeProb;		//Définition de l'instance de problème
	TTabou LeTabou;			//Définition des paramètres de l'agorithme
	string NomFichier;
		
	//**Lecture des paramètres
	NomFichier.assign(Param[1]);
	LeTabou.NB_EVAL_MAX= atoi(Param[2]);

	//**Lecture du fichier de donnees
	LectureProbleme(NomFichier, LeProb, LeTabou);
	AfficherProbleme(LeProb);
	
	//**Création de la solution initiale 
	CreerSolutionAleatoire(Courante, LeProb, LeTabou);
	//AfficherSolution(Courante, LeProb.NbVilles, "SolInitiale: ");

	do
	{
		Next = GetSolutionVoisine(Courante, LeProb, LeTabou);
		//AfficherSolution(Courante, LeProb.NbVilles, "Courante: ");
		//AfficherSolution(Next, LeProb.NbVilles, "Next: ");
		if (Next.FctObj <= Courante.FctObj)	//**amélioration
		{
				Courante = Next;
				AfficherSolution(Courante, LeProb.NbVilles, "NouvelleCourante: ");
		}
	}while (LeTabou.CptEval < LeTabou.NB_EVAL_MAX);

	AfficherResultats(Courante, LeProb, LeTabou);
	AfficherResultatsFichier(Courante, LeProb, LeTabou,"Resultats.txt");
	
	LibererMemoireFinPgm(Courante, Next, Best, LeProb);

	system("PAUSE");
	return 0;
}

//DESCRIPTION: Création d'une solution voisine à partir de la solution uneSol qui ne doit pas être modifiée.
//Dans cette fonction, on appel le TYPE DE VOISINAGE sélectionné + on détermine la STRATÉGIE D'ORIENTATION. 
//Ainsi, si la RÈGLE DE PIVOT nécessite l'étude de plusieurs voisins, la fonction TYPE DE VOISINAGE sera appelée plusieurs fois.
//Le TYPE DE PARCOURS dans le voisinage interviendra normalement dans la focntion TYPE DE VOISINAGE.
TSolution GetSolutionVoisine (const TSolution uneSol, TProblem unProb, TTabou &unTabou)
{
	//Type de voisinage : à indiquer (Echange 2 villes aléatoires)
	//Parcours dans le voisinage : à indiquer	(Aleatoire)
	//Règle de pivot : à indiquer	(First-Impove)
	//Taille du voisinage (nombre de voisins à chaque itération) : 1

	TSolution unVoisin;
	
	unVoisin = Echange(uneSol, unProb, unTabou);
	return (unVoisin);	
}

//DESCRIPTION: Echange de deux villes sélectionnée aléatoirement
TSolution Echange (const TSolution uneSol, TProblem unProb, TTabou &unTabou)
{
	int PosA, PosB, Tmp;
	TSolution Copie;
	
	//Utilisation d'une nouvelle TSolution pour ne pas modifier La solution courante (uneSol)
	CopierSolution(uneSol, Copie, unProb);
	
	//Tirage aléatoire des 2 villes
	PosA = rand() % unProb.NbVilles;
	do
	{
		PosB = rand() % unProb.NbVilles;
	}while (PosA == PosB);
	//Verification pour ne pas perdre une évaluation
	
	//Echange des 2 villes
	Tmp = Copie.Seq[PosA];
	Copie.Seq[PosA] = Copie.Seq[PosB];
	Copie.Seq[PosB] = Tmp;
	
	//Le nouveau voisin doit être évalué 
	Copie.FctObj = EvaluerSolution(Copie, unProb, unTabou);
	return(Copie);
}
