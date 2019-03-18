#include <windows.h>
#include <algorithm>
#include "Entete.h"


#pragma comment (lib,"DescenteDLL.lib")  
//%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTANT: %%%%%%%%%%%%%%%%%%%%%%%%% 
//Le fichier de probleme (.txt) doit se trouver dans le répertoire courant du projet pour une exécution à l'aide du compilateur.
//Les fichiers de la DLL (GeneticDLL.dll et GeneticDLL.lib) doivent se trouver dans le même répertoire que l'exécutable (.exe-DEBUG) et 
//dans le répertoire courant du projet pour une exécution à l'aide du compilateur.
//Indiquer les arguments du programme dans les propriétés du projet - débogage - arguements.
//Sinon, utiliser le répertoire execution.

//*****************************************************************************************
// Prototype des fonctions se trouvant dans la DLL 
//*****************************************************************************************
//DESCRIPTION:	Lecture du Fichier probleme et initialiation de la structure Problem
extern "C" _declspec(dllimport) void LectureProbleme(std::string FileName, TProblem & unProb, TAlgo &unAlgo);

//DESCRIPTION:	Fonction d'affichage à l'écran permettant de voir si les données du fichier problème ont été lues correctement
extern "C" _declspec(dllimport) void AfficherProbleme (TProblem unProb);

//DESCRIPTION: Affichage d'une solution a l'écran pour validation
extern "C" _declspec(dllimport) void AfficherSolution(const TSolution uneSolution, int N, std::string Titre);

//DESCRIPTION: Affichage à l'écran de la solution finale, du nombre d'évaluations effectuées et de certains paramètres
extern "C" _declspec(dllimport) void AfficherResultats (const TSolution uneSol, TProblem unProb, TAlgo unAlgo);

//DESCRIPTION: Affichage dans un fichier(en append) de la solution finale, du nombre d'évaluations effectuées et de certains paramètres
extern "C" _declspec(dllimport) void AfficherResultatsFichier (const TSolution uneSol, TProblem unProb, TAlgo unAlgo, std::string FileName);

//DESCRIPTION:	Évaluation de la fonction objectif d'une solution et MAJ du compteur d'évaluation. 
//				Retourne un long représentant la distance totale de la trounée incluant le retour un point initial
extern "C" _declspec(dllimport) long EvaluerSolution(const TSolution uneSolution, TProblem unProb, TAlgo &unAlgo);

//DESCRIPTION:	Création d'une séquence aléatoire de parcours des villes et évaluation de la fonction objectif. Allocation dynamique de mémoire
// pour la séquence (.Seq)
extern "C" _declspec(dllimport) void CreerSolutionAleatoire(TSolution & uneSolution, TProblem unProb, TAlgo &unAlgo);

//DESCRIPTION: Copie de la séquence et de la fonction objectif dans une nouvelle TSolution. La nouvelle TSolution est retournée.
extern "C" _declspec(dllimport) void CopierSolution (const TSolution uneSol, TSolution &Copie, TProblem unProb);

//DESCRIPTION:	Libération de la mémoire allouée dynamiquement
extern "C" _declspec(dllimport) void	LibererMemoireFinPgm	(TSolution uneCourante, TSolution uneNext, TSolution uneBest, TProblem unProb);

//*****************************************************************************************
// Prototype des fonctions locales 
//*****************************************************************************************

//DESCRIPTION:	Création d'une solution voisine à partir de la solution uneSol. NB:uneSol ne doit pas être modifiée
TSolution GetSolutionVoisine (const TSolution uneSol, TProblem unProb, TAlgo &unAlgo);

//DESCRIPTION:	 Echange de deux villes sélectionnée aléatoirement. NB:uneSol ne doit pas être modifiée
TSolution	Echange			(const TSolution uneSol, TProblem unProb, TAlgo &unAlgo, vector<tuple<int, int, int, int>> arcs);


//******************************************************************************************
// Fonction main
//*****************************************************************************************
int main(int NbParam, char *Param[])
{
	TSolution Courante;		//Solution active au cours des itérations
	TSolution Next;			//Solution voisine retenue à une itération
	TSolution Best;			//Meilleure solution depuis le début de l'algorithme
	TProblem LeProb;		//Définition de l'instance de problème
	TAlgo LAlgo;			//Définition des paramètres de l'agorithme
	string NomFichier;
		
	//**Lecture des paramètres
	NomFichier.assign(Param[1]);
	LAlgo.NB_EVAL_MAX= atoi(Param[2]);

	//**Lecture du fichier de donnees
	LectureProbleme(NomFichier, LeProb, LAlgo);
	AfficherProbleme(LeProb);
	
	//**Création de la solution initiale 
	CreerSolutionAleatoire(Courante, LeProb, LAlgo);
	//AfficherSolution(Courante, LeProb.NbVilles, "SolInitiale: ");

	int m = 0, p = -1;
	do
	{
		Next = GetSolutionVoisine(Courante, LeProb, LAlgo);
		//AfficherSolution(Courante, LeProb.NbVilles, "Courante: ");
		//AfficherSolution(Next, LeProb.NbVilles, "Next: ");
		if (Next.FctObj <= Courante.FctObj)	//**amélioration
		{
				Courante = Next;
				//cout << "Fct Obj Nouvelle Courante: " << Courante.FctObj << endl;
				//AfficherSolution(Courante, LeProb.NbVilles, "NouvelleCourante: ");
		}
		if (Courante.FctObj != p) cout << m << "\t" << Courante.FctObj << endl;
		m++;
		p = Courante.FctObj;
	}while (LAlgo.CptEval < LAlgo.NB_EVAL_MAX);

	AfficherResultats(Courante, LeProb, LAlgo);
	AfficherResultatsFichier(Courante, LeProb, LAlgo,"Resultats.txt");
	
	LibererMemoireFinPgm(Courante, Next, Best, LeProb);

	system("PAUSE");
	return 0;
}

//DESCRIPTION: Création d'une solution voisine à partir de la solution uneSol qui ne doit pas être modifiée.
//Dans cette fonction, on appel le TYPE DE VOISINAGE sélectionné + on détermine la STRATÉGIE D'ORIENTATION. 
//Ainsi, si la RÈGLE DE PIVOT nécessite l'étude de plusieurs voisins, la fonction TYPE DE VOISINAGE sera appelée plusieurs fois.
//Le TYPE DE PARCOURS dans le voisinage interviendra normalement dans la focntion TYPE DE VOISINAGE.
TSolution GetSolutionVoisine (const TSolution uneSol, TProblem unProb, TAlgo &unAlgo)
{
	//Type de voisinage : 3-opt
	//Parcours dans le voisinage : Orienté (x1) + Aléatoire (x2)
	//Règle de pivot : k-improveBest
	//Taille du voisinage (nombre de voisins à chaque itération) : 7

	//=================================================================================
	//Récupération de la liste des arcs de la solution courante

	//Initialisation (paramétrable)
	bool debug = false;
	const int K_LIMIT = 3;
	vector<TSolution> voisins;

	//On génére K voisins afin de selectionner le meilleur possible.
	for (int k = 0; k < K_LIMIT; k++) {

		//Lorsque le nombre max d'évaluation est atteinte, on quitte
		if (unAlgo.CptEval == unAlgo.NB_EVAL_MAX) { break; }

		//Initialisation
		int nbArcs = 3;
		int arcs_orientes = 3;
		int arcs_orientes_choisis = 0;
		int n = unProb.NbVilles;
		int arcs_aleatoires_choisis = nbArcs - arcs_orientes_choisis;
		vector<tuple<int, int, int, int>> arcs, arcs_choisis;
		arcs.reserve(n);
		arcs_choisis.reserve(nbArcs);

		//Génération de la liste des arcs 
		for (int i = 0; i < n; i++) {
			int firstIndex = (n + i - 1) % n;
			int a = uneSol.Seq[firstIndex];
			int b = uneSol.Seq[i];
			arcs.push_back(make_tuple(a, b, firstIndex, i));
		}

		//Tri des arcs selon leur distance
		sort(arcs.begin(), arcs.end(), [&, unProb](tuple<int, int, int, int> a, tuple<int, int, int, int> b) { return unProb.Distance[get<0>(a)][get<1>(a)] > unProb.Distance[get<0>(b)][get<1>(b)]; });

		//Affichage de la liste des arcs (trié)
		if (debug)
			for (int i = 0; i < n; i++)
				cout << "Arc " << i << " : " << get<0>(arcs[i]) << " -> " << get<1>(arcs[i]) << " (" << unProb.Distance[get<0>(arcs[i])][get<1>(arcs[i])] << ")" << endl;


		//=================================================================================
		//Sélection des arcs

		//Arcs orientés
		for (int i = 0; i < arcs_orientes_choisis; i++) {
			int m = rand() % arcs_orientes;
			auto arc = arcs[m];
			arcs_choisis.push_back(arc);
			arcs.erase(remove(begin(arcs), end(arcs), arc), end(arcs));
			arcs_orientes--;
			if (debug) cout << "> Choix " << arcs_choisis.size() << " (oriente " << m << " <= " << arcs_orientes << " / " << arcs.size() << ") : " << get<0>(arc) << " -> " << get<1>(arc) << " (" << unProb.Distance[get<0>(arc)][get<1>(arc)] << ")" << endl;
		}

		//Choix aléatoires
		for (int i = 0; i < arcs_aleatoires_choisis; i++) {
			int m = rand() % arcs.size();
			auto arc = arcs[m];
			arcs_choisis.push_back(arc);
			arcs.erase(remove(begin(arcs), end(arcs), arc), end(arcs));
			if (debug) cout << "> Choix " << arcs_choisis.size() << " (aleatoire " << m << " / " << arcs.size() << ") : " << get<0>(arc) << " -> " << get<1>(arc) << " (" << unProb.Distance[get<0>(arc)][get<1>(arc)] << ")" << endl;
		}

		//=================================================================================

		// On ajoute le nouveau voisin généré parmis les k-voisins.
		TSolution unVoisin = Echange(uneSol, unProb, unAlgo, arcs_choisis);
		voisins.push_back(unVoisin);
	}

	//On cherche la meilleur solution, celle qui minimise la fonction objet
	TSolution meilleurSol = voisins[0];
	for (int i = 1; i < voisins.size(); i++) {
		meilleurSol = voisins[i].FctObj < voisins[i - 1].FctObj ? voisins[i] : voisins[i-1];
	}
	
	if (debug) cout << "> Valeur de la meilleur solution " << meilleurSol.FctObj << endl;
	
	//Affichage des arcs choisis
	if (debug)
		system("PAUSE");

	//On retourne le meilleur voisin parmis les k générés.
	return meilleurSol;
}

//DESCRIPTION: Echange de deux villes sélectionnées aléatoirement
//à modifier par les étudiants. Vous devez prendre un autre type de voisinage
TSolution Echange (const TSolution uneSol, TProblem unProb, TAlgo &unAlgo, vector<tuple<int, int, int, int>> arcs)
{
	//Utilisation d'une nouvelle TSolution pour ne pas modifier La solution courante (uneSol)
	TSolution voisin;
	
	//On trie les arcs par leurs index dans la solution courante
	sort(arcs.begin(), arcs.end(), [](tuple<int, int, int, int> a, tuple<int, int, int, int> b) { return get<2>(a) < get<2>(b); });
	
	//On regarde s'il y a des points communs dans les arcs choisis.
	vector<pair<int,int>> pointsCommun;
	int n = arcs.size();
	for (int i = 0; i < n; i++) {
		if (get<1>(arcs[i]) == get<0>(arcs[(i + 1) % n])) {
			pointsCommun.push_back(make_pair(get<0>(arcs[i]), get<2>(arcs[i])));
		}
	}

	vector<tuple<int, int, int, int>> swappedArcs;

	// Generation de la nouvelle solution en fonction des arcs chosis. On effectue un échange 3 opt.
	switch (pointsCommun.size()) {
		// Si il y a que 0 ou 1 sommets en communs parmis les arcs
		case 0:
		case 1:
			swappedArcs.reserve(arcs.size());
			// Generation des nouveaux arcs. Il n'y a qu'un seul moyen d'effectuer un échange 3-opt asymétrique pour ne pas inverser des parties du graphes.
			swappedArcs.push_back(make_tuple(get<0>(arcs[0]), get<1>(arcs[1]), get<2>(arcs[0]), get<3>(arcs[1])));
			swappedArcs.push_back(make_tuple(get<0>(arcs[2]), get<1>(arcs[0]), get<2>(arcs[2]), get<3>(arcs[0])));
			swappedArcs.push_back(make_tuple(get<0>(arcs[1]), get<1>(arcs[2]), get<2>(arcs[1]), get<3>(arcs[2])));

			// Generation de la nouvelle solution.
			voisin.Seq.reserve(uneSol.Seq.size());
			for (int i = 0; i < 2; i++) {
				voisin.Seq.push_back(get<0>(swappedArcs[i]));
				voisin.Seq.insert(voisin.Seq.end(), uneSol.Seq.begin() + get<3>(swappedArcs[i]), uneSol.Seq.begin() + get<2>(swappedArcs[i+1]));
			}
			voisin.Seq.push_back(get<0>(swappedArcs[2]));
			if (get<3>(swappedArcs[2]) < get<2>(swappedArcs[2])) {
				voisin.Seq.insert(voisin.Seq.end(), uneSol.Seq.begin() + get<3>(swappedArcs[2]), uneSol.Seq.begin() + get<2>(swappedArcs[0]));
			} else {
				voisin.Seq.insert(voisin.Seq.end(), uneSol.Seq.begin() + get<3>(swappedArcs[2]), uneSol.Seq.end());
				voisin.Seq.insert(voisin.Seq.end(), uneSol.Seq.begin(), uneSol.Seq.begin() + get<2>(swappedArcs[0]));
			}
			break;
		//Si il y 2 sommets en communs parmis les arcs
		default:
			//On copie la solution et on interchanges les points communs des arcs.
			CopierSolution(uneSol, voisin, unProb);
			voisin.Seq[pointsCommun[0].second] = pointsCommun[1].first;
			voisin.Seq[pointsCommun[1].second] = pointsCommun[0].first;
			
	}

	//Le nouveau voisin doit être évalué 
	voisin.FctObj = EvaluerSolution(voisin, unProb, unAlgo);

	return(voisin);
}
