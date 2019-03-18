#include <windows.h>
#include <algorithm>
#include "Entete.h"
#pragma comment (lib,"RecuitDLL.lib")  
#define K_LIMIT 7
//%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTANT: %%%%%%%%%%%%%%%%%%%%%%%%% 
//Le fichier de probleme (.txt) et les fichiers de la DLL (RecuitDLL.dll et RecuitDLL.lib) doivent 
//se trouver dans le r�pertoire courant du projet pour une ex�cution � l'aide du compilateur. Indiquer les
//arguments du programme dans les propri�t�s du projet - d�bogage - arguements.
//Sinon, utiliser le r�pertoire execution.

//*****************************************************************************************
// Prototype des fonctions se trouvant dans la DLL 
//*****************************************************************************************
//DESCRIPTION:	Lecture du Fichier probleme et initialiation de la structure Problem
extern "C" _declspec(dllimport) void LectureProbleme(std::string FileName, TProblem & unProb, TRecuit &unRecuit);

//DESCRIPTION:	Fonction d'affichage � l'�cran permettant de voir si les donn�es du fichier probl�me ont �t� lues correctement
extern "C" _declspec(dllimport) void AfficherProbleme (TProblem unProb);

//DESCRIPTION: Affichage d'une solution a l'�cran pour validation
extern "C" _declspec(dllimport) void AfficherSolution(const TSolution uneSolution, int N, std::string Titre);

//DESCRIPTION: Affichage � l'�cran de la solution finale, du nombre d'�valuations effectu�es et de certains param�tres
extern "C" _declspec(dllimport) void AfficherResultats (const TSolution uneSol, TProblem unProb, TRecuit unRecuit);

//DESCRIPTION: Affichage dans un fichier(en append) de la solution finale, du nombre d'�valuations effectu�es et de certains param�tres
extern "C" _declspec(dllimport) void AfficherResultatsFichier (const TSolution uneSol, TProblem unProb, TRecuit unRecuit, std::string FileName);

//DESCRIPTION:	�valuation de la fonction objectif d'une solution et MAJ du compteur d'�valuation. 
//				Retourne un long repr�sentant la distance totale de la troun�e incluant le retour un point initial
extern "C" _declspec(dllimport) long EvaluerSolution(const TSolution uneSolution, TProblem unProb, TRecuit &unRecuit);

//DESCRIPTION:	Cr�ation d'une s�quence al�atoire de parcours des villes et �valuation de la fonction objectif. Allocation dynamique de m�moire
// pour la s�quence (.Seq)
extern "C" _declspec(dllimport) void CreerSolutionAleatoire(TSolution & uneSolution, TProblem unProb, TRecuit &unRecuit);

//DESCRIPTION: Copie de la s�quence et de la fonction objectif dans une nouvelle TSolution. La nouvelle TSolution est retourn�e.
extern "C" _declspec(dllimport) void CopierSolution (const TSolution uneSol, TSolution &Copie, TProblem unProb);

//DESCRIPTION:	Lib�ration de la m�moire allou�e dynamiquement
extern "C" _declspec(dllimport) void	LibererMemoireFinPgm	(TSolution uneCourante, TSolution uneNext, TSolution uneBest, TProblem unProb);

//*****************************************************************************************
// Prototype des fonctions locales 
//*****************************************************************************************

//DESCRIPTION:	Cr�ation d'une solution voisine � partir de la solution uneSol. NB:uneSol ne doit pas �tre modifi�e
TSolution GetSolutionVoisine(const TSolution uneSol, TProblem unProb, TRecuit &unRecuit);

//DESCRIPTION:	 Echange de deux villes s�lectionn�e al�atoirement. NB:uneSol ne doit pas �tre modifi�e
TSolution Echange(const TSolution uneSol, TProblem unProb, TRecuit &unRecuit, vector<tuple<int, int, int, int>> arcs);


//******************************************************************************************
// Fonction main
//*****************************************************************************************
int main(int NbParam, char *Param[])
{
	TSolution Courante;		//Solution active au cours des it�rations
	TSolution Next;			//Solution voisine retenue � une it�ration
	TSolution Best;			//Meilleure solution depuis le d�but de l'algorithme
	TProblem LeProb;		//D�finition de l'instance de probl�me
	TRecuit LeRecuit;		//D�finition des param�tres du recuit simul�
	string NomFichier;
		
	//**Lecture des param�tres
	NomFichier.assign(Param[1]);
	LeRecuit.TempInit	= atoi(Param[2]);
	LeRecuit.Alpha		= atof(Param[3]);
	LeRecuit.NbPalier	= atoi(Param[4]);
	LeRecuit.NB_EVAL_MAX= atoi(Param[5]);

	//**Lecture du fichier de donnees
	LectureProbleme(NomFichier, LeProb, LeRecuit);
	AfficherProbleme(LeProb);
	
	// Seed random
	srand(time(NULL));
	//**Cr�ation de la solution initiale 
	CreerSolutionAleatoire(Courante, LeProb, LeRecuit);
	//AfficherSolution(Courante, LeProb.NbVilles, "SolInitiale: ");

	//+Initialisation
	LeRecuit.NoPalier = 0;
	LeRecuit.Temperature = LeRecuit.TempInit;
	CopierSolution(Courante, Best, LeProb);

	do
	{
		//+Nombre it�rations
		int n = 0;
		//+Num�ro du pallier
		LeRecuit.NoPalier++;
		//+Boucle de pallier
		while ((n++ < (ceil(LeRecuit.NB_EVAL_MAX / K_LIMIT) / LeRecuit.NbPalier))&&((LeRecuit.CptEval < LeRecuit.NB_EVAL_MAX))) {
			// Generation de voisin
			Next = GetSolutionVoisine(Courante, LeProb, LeRecuit);
			// Calcul du d�placement
			LeRecuit.Delta = Next.FctObj - Courante.FctObj;
			// Deplacement d'am�lioration
			if (LeRecuit.Delta <= 0)
			{
				// On met � jour la solution courante.
				Courante = Next;

				//+Si meilleure solution, on la garde en m�moire.
				if (Courante.FctObj < Best.FctObj) {
					CopierSolution(Courante,Best,LeProb);
				}
				AfficherSolution(Courante, LeProb.NbVilles, "NouvelleCourante: ");
			}
			//+Deplacement d�t�rioration uniquement si fonction d'acceptation valid�.
			else if (rand() / RAND_MAX < exp(-LeRecuit.Delta/ LeRecuit.Temperature)) {
				Courante = Next;
			}
			//+Debug
			cout << endl << "Iteration " << LeRecuit.CptEval << " : " << LeRecuit.Temperature << " K " << "(Pallier " << LeRecuit.NoPalier << ", Best " << Best.FctObj << ")";
		} 


		//+Mise � jour de la temp�rature lorsque l'on change de pallier.
		LeRecuit.Temperature *= LeRecuit.Alpha;
		
	} while (LeRecuit.CptEval < LeRecuit.NB_EVAL_MAX);

	AfficherResultats(Best, LeProb, LeRecuit);
	AfficherResultatsFichier(Best, LeProb, LeRecuit,"Resultats.txt");
	
	LibererMemoireFinPgm(Courante, Next, Best, LeProb);

	system("PAUSE");
	return 0;
}

//DESCRIPTION: Cr�ation d'une solution voisine � partir de la solution uneSol qui ne doit pas �tre modifi�e.
//Dans cette fonction, on appel le TYPE DE VOISINAGE s�lectionn� + on d�termine la STRAT�GIE D'ORIENTATION. 
//Ainsi, si la R�GLE DE PIVOT n�cessite l'�tude de plusieurs voisins, la fonction TYPE DE VOISINAGE sera appel�e plusieurs fois.
//Le TYPE DE PARCOURS dans le voisinage interviendra normalement dans la focntion TYPE DE VOISINAGE.
//� modifier par les �tudiants
TSolution GetSolutionVoisine(const TSolution uneSol, TProblem unProb, TRecuit &unRecuit)
{
	//Type de voisinage : � indiquer (Echange 2 villes al�atoires)
	//Parcours dans le voisinage : � indiquer	(Aleatoire)
	//R�gle de pivot : � indiquer	(First-Impove)
	//Taille du voisinage (nombre de voisins � chaque it�ration) : 1

	//=================================================================================
	//R�cup�ration de la liste des arcs de la solution courante

	//Initialisation (param�trable)
	bool debug = false;
	vector<TSolution> voisins;

	//On g�n�re K voisins afin de selectionner le meilleur possible.
	for (int k = 0; k < K_LIMIT; k++) {

		//Lorsque le nombre max d'�valuation est atteinte, on quitte
		if (unRecuit.CptEval == unRecuit.NB_EVAL_MAX) { break; }

		//Initialisation
		int nbArcs = 3;
		int arcs_orientes = 5;
		int arcs_orientes_choisis = 1;
		int n = unProb.NbVilles;
		int arcs_aleatoires_choisis = nbArcs - arcs_orientes_choisis;
		vector<tuple<int, int, int, int>> arcs, arcs_choisis;
		arcs.reserve(n);
		arcs_choisis.reserve(nbArcs);

		//G�n�ration de la liste des arcs 
		for (int i = 0; i < n; i++) {
			int firstIndex = (n + i - 1) % n;
			int a = uneSol.Seq[firstIndex];
			int b = uneSol.Seq[i];
			arcs.push_back(make_tuple(a, b, firstIndex, i));
		}

		//Tri des arcs selon leur distance
		sort(arcs.begin(), arcs.end(), [&, unProb](tuple<int, int, int, int> a, tuple<int, int, int, int> b) { return unProb.Distance[get<0>(a)][get<1>(a)] > unProb.Distance[get<0>(b)][get<1>(b)]; });

		//Affichage de la liste des arcs (tri�)
		if (debug)
			for (int i = 0; i < n; i++)
				cout << "Arc " << i << " : " << get<0>(arcs[i]) << " -> " << get<1>(arcs[i]) << " (" << unProb.Distance[get<0>(arcs[i])][get<1>(arcs[i])] << ")" << endl;


		//=================================================================================
		//S�lection des arcs

		//Arcs orient�s
		for (int i = 0; i < arcs_orientes_choisis; i++) {
			int m = rand() % arcs_orientes;
			auto arc = arcs[m];
			arcs_choisis.push_back(arc);
			arcs.erase(remove(begin(arcs), end(arcs), arc), end(arcs));
			arcs_orientes--;
			if (debug) cout << "> Choix " << arcs_choisis.size() << " (oriente " << m << " <= " << arcs_orientes << " / " << arcs.size() << ") : " << get<0>(arc) << " -> " << get<1>(arc) << " (" << unProb.Distance[get<0>(arc)][get<1>(arc)] << ")" << endl;
		}

		//Choix al�atoires
		for (int i = 0; i < arcs_aleatoires_choisis; i++) {
			int m = rand() % arcs.size();
			auto arc = arcs[m];
			arcs_choisis.push_back(arc);
			arcs.erase(remove(begin(arcs), end(arcs), arc), end(arcs));
			if (debug) cout << "> Choix " << arcs_choisis.size() << " (aleatoire " << m << " / " << arcs.size() << ") : " << get<0>(arc) << " -> " << get<1>(arc) << " (" << unProb.Distance[get<0>(arc)][get<1>(arc)] << ")" << endl;
		}


		//=================================================================================

		//Utiliser la variable arcs_choisis pour le 3-opt

		TSolution unVoisin = Echange(uneSol, unProb, unRecuit, arcs_choisis);

		// On ajoute le nouveau voisin g�n�r� parmis les k-voisins.
		voisins.push_back(unVoisin);
	}

	//On cherche la meilleur solution, celle qui minimise la fonction objet
	TSolution meilleurSol = voisins[0];
	for (int i = 1; i < voisins.size(); i++) {
		meilleurSol = voisins[i].FctObj < voisins[i - 1].FctObj ? voisins[i] : voisins[i - 1];
	}

	if (debug) cout << "> Valeur de la meilleur solution " << meilleurSol.FctObj << endl;

	//Affichage des arcs choisis
	if (debug)
		system("PAUSE");

	//On retourne le meilleur voisin parmis les k g�n�r�s.
	return meilleurSol;
}

//DESCRIPTION: Echange de deux villes s�lectionn�es al�atoirement
//� modifier par les �tudiants. Vous devez prendre un autre type de voisinage
TSolution Echange(const TSolution uneSol, TProblem unProb, TRecuit &unRecuit, vector<tuple<int, int, int, int>> arcs)
{
	//Utilisation d'une nouvelle TSolution pour ne pas modifier La solution courante (uneSol)
	TSolution voisin;

	//On trie les arcs par leurs index dans la solution courante
	sort(arcs.begin(), arcs.end(), [](tuple<int, int, int, int> a, tuple<int, int, int, int> b) { return get<2>(a) < get<2>(b); });

	//On regarde s'il y a des points communs dans les arcs choisis.
	vector<pair<int, int>> pointsCommun;
	int n = arcs.size();
	for (int i = 0; i < n; i++) {
		if (get<1>(arcs[i]) == get<0>(arcs[(i + 1) % n])) {
			pointsCommun.push_back(make_pair(get<0>(arcs[i]), get<2>(arcs[i])));
		}
	}

	vector<tuple<int, int, int, int>> swappedArcs;

	// Generation de la nouvelle solution en fonction des arcs chosis. On effectue un �change 3 opt.
	switch (pointsCommun.size()) {
		// Si il y a que 0 ou 1 sommets en communs parmis les arcs
	case 0:
	case 1:
		swappedArcs.reserve(arcs.size());
		// Generation des nouveaux arcs. Il n'y a qu'un seul moyen d'effectuer un �change 3-opt asym�trique pour ne pas inverser des parties du graphes.
		swappedArcs.push_back(make_tuple(get<0>(arcs[0]), get<1>(arcs[1]), get<2>(arcs[0]), get<3>(arcs[1])));
		swappedArcs.push_back(make_tuple(get<0>(arcs[2]), get<1>(arcs[0]), get<2>(arcs[2]), get<3>(arcs[0])));
		swappedArcs.push_back(make_tuple(get<0>(arcs[1]), get<1>(arcs[2]), get<2>(arcs[1]), get<3>(arcs[2])));

		// Generation de la nouvelle solution.
		voisin.Seq.reserve(uneSol.Seq.size());
		for (int i = 0; i < 2; i++) {
			voisin.Seq.push_back(get<0>(swappedArcs[i]));
			voisin.Seq.insert(voisin.Seq.end(), uneSol.Seq.begin() + get<3>(swappedArcs[i]), uneSol.Seq.begin() + get<2>(swappedArcs[i + 1]));
		}
		voisin.Seq.push_back(get<0>(swappedArcs[2]));
		if (get<3>(swappedArcs[2]) < get<2>(swappedArcs[2])) {
			voisin.Seq.insert(voisin.Seq.end(), uneSol.Seq.begin() + get<3>(swappedArcs[2]), uneSol.Seq.begin() + get<2>(swappedArcs[0]));
		}
		else {
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

	//Le nouveau voisin doit �tre �valu� 
	voisin.FctObj = EvaluerSolution(voisin, unProb, unRecuit);

	return(voisin);
}