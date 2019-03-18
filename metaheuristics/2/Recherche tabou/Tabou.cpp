#include <windows.h>
#include "Entete.h"
#pragma comment (lib,"TabouDLL.lib")  
//%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTANT: %%%%%%%%%%%%%%%%%%%%%%%%% 
//Le fichier de probleme (.txt) et les fichiers de la DLL (DescenteDLL.dll et DescenteDLL.lib) doivent 
//se trouver dans le r�pertoire courant du projet pour une ex�cution � l'aide du compilateur. Indiquer les
//arguments du programme dans les propri�t�s du projet - d�bogage - arguements.
//Sinon, utiliser le r�pertoire execution.

//*****************************************************************************************
// Prototype des fonctions se trouvant dans la DLL 
//*****************************************************************************************
//DESCRIPTION:	Lecture du Fichier probleme et initialiation de la structure Problem
extern "C" _declspec(dllimport) void LectureProbleme(std::string FileName, TProblem & unProb, TTabou &unTabou);

//DESCRIPTION:	Fonction d'affichage � l'�cran permettant de voir si les donn�es du fichier probl�me ont �t� lues correctement
extern "C" _declspec(dllimport) void AfficherProbleme (TProblem unProb);

//DESCRIPTION: Affichage d'une solution a l'�cran pour validation
extern "C" _declspec(dllimport) void AfficherSolution(const TSolution uneSolution, int N, std::string Titre);

//DESCRIPTION: Affichage � l'�cran de la solution finale, du nombre d'�valuations effectu�es et de certains param�tres
extern "C" _declspec(dllimport) void AfficherResultats (const TSolution uneSol, TProblem unProb, TTabou unTabou);

//DESCRIPTION: Affichage dans un fichier(en append) de la solution finale, du nombre d'�valuations effectu�es et de certains param�tres
extern "C" _declspec(dllimport) void AfficherResultatsFichier (const TSolution uneSol, TProblem unProb, TTabou unTabou, std::string FileName);

//DESCRIPTION:	�valuation de la fonction objectif d'une solution et MAJ du compteur d'�valuation. 
//				Retourne un long repr�sentant la distance totale de la troun�e incluant le retour un point initial
extern "C" _declspec(dllimport) long EvaluerSolution(const TSolution uneSolution, TProblem unProb, TTabou &unTabou);

//DESCRIPTION:	Cr�ation d'une s�quence al�atoire de parcours des villes et �valuation de la fonction objectif. Allocation dynamique de m�moire
// pour la s�quence (.Seq)
extern "C" _declspec(dllimport) void CreerSolutionAleatoire(TSolution & uneSolution, TProblem unProb, TTabou &unTabou);

//DESCRIPTION: Copie de la s�quence et de la fonction objectif dans une nouvelle TSolution. La nouvelle TSolution est retourn�e.
extern "C" _declspec(dllimport) void CopierSolution (const TSolution uneSol, TSolution &Copie, TProblem unProb);

//DESCRIPTION:	Lib�ration de la m�moire allou�e dynamiquement
extern "C" _declspec(dllimport) void	LibererMemoireFinPgm	(TSolution uneCourante, TSolution uneNext, TSolution uneBest, TProblem unProb);

//*****************************************************************************************
// Prototype des fonctions locales 
//*****************************************************************************************

//DESCRIPTION:	Cr�ation d'une solution voisine � partir de la solution uneSol. NB:uneSol ne doit pas �tre modifi�e
TSolution GetSolutionVoisine (const TSolution uneSol, TProblem unProb, TTabou &unTabou);

//DESCRIPTION:	 Echange de deux villes s�lectionn�e al�atoirement. NB:uneSol ne doit pas �tre modifi�e
TSolution	Echange			(const TSolution uneSol, TProblem unProb, TTabou &unTabou);


//******************************************************************************************
// Fonction main
//*****************************************************************************************
int main(int NbParam, char *Param[])
{
	TSolution Courante;		//Solution active au cours des it�rations
	TSolution Next;			//Solution voisine retenue � une it�ration
	TSolution Best;			//Meilleure solution depuis le d�but de l'algorithme
	TProblem LeProb;		//D�finition de l'instance de probl�me
	TTabou LeTabou;			//D�finition des param�tres de l'agorithme
	string NomFichier;
		
	//**Lecture des param�tres
	NomFichier.assign(Param[1]);
	LeTabou.NB_EVAL_MAX= atoi(Param[2]);

	//**Lecture du fichier de donnees
	LectureProbleme(NomFichier, LeProb, LeTabou);
	AfficherProbleme(LeProb);
	
	//**Cr�ation de la solution initiale 
	CreerSolutionAleatoire(Courante, LeProb, LeTabou);
	//AfficherSolution(Courante, LeProb.NbVilles, "SolInitiale: ");

	do
	{
		Next = GetSolutionVoisine(Courante, LeProb, LeTabou);
		//AfficherSolution(Courante, LeProb.NbVilles, "Courante: ");
		//AfficherSolution(Next, LeProb.NbVilles, "Next: ");
		if (Next.FctObj <= Courante.FctObj)	//**am�lioration
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

//DESCRIPTION: Cr�ation d'une solution voisine � partir de la solution uneSol qui ne doit pas �tre modifi�e.
//Dans cette fonction, on appel le TYPE DE VOISINAGE s�lectionn� + on d�termine la STRAT�GIE D'ORIENTATION. 
//Ainsi, si la R�GLE DE PIVOT n�cessite l'�tude de plusieurs voisins, la fonction TYPE DE VOISINAGE sera appel�e plusieurs fois.
//Le TYPE DE PARCOURS dans le voisinage interviendra normalement dans la focntion TYPE DE VOISINAGE.
TSolution GetSolutionVoisine (const TSolution uneSol, TProblem unProb, TTabou &unTabou)
{
	//Type de voisinage : � indiquer (Echange 2 villes al�atoires)
	//Parcours dans le voisinage : � indiquer	(Aleatoire)
	//R�gle de pivot : � indiquer	(First-Impove)
	//Taille du voisinage (nombre de voisins � chaque it�ration) : 1

	TSolution unVoisin;
	
	unVoisin = Echange(uneSol, unProb, unTabou);
	return (unVoisin);	
}

//DESCRIPTION: Echange de deux villes s�lectionn�e al�atoirement
TSolution Echange (const TSolution uneSol, TProblem unProb, TTabou &unTabou)
{
	int PosA, PosB, Tmp;
	TSolution Copie;
	
	//Utilisation d'une nouvelle TSolution pour ne pas modifier La solution courante (uneSol)
	CopierSolution(uneSol, Copie, unProb);
	
	//Tirage al�atoire des 2 villes
	PosA = rand() % unProb.NbVilles;
	do
	{
		PosB = rand() % unProb.NbVilles;
	}while (PosA == PosB);
	//Verification pour ne pas perdre une �valuation
	
	//Echange des 2 villes
	Tmp = Copie.Seq[PosA];
	Copie.Seq[PosA] = Copie.Seq[PosB];
	Copie.Seq[PosB] = Tmp;
	
	//Le nouveau voisin doit �tre �valu� 
	Copie.FctObj = EvaluerSolution(Copie, unProb, unTabou);
	return(Copie);
}
