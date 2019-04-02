#include <windows.h>
#include "Entete.h"
#pragma comment (lib,"GeneticDLL.lib")

#define NB_PARENTS 3
#define NB_CASTES 3

//%%%%%%%%%%%%%%%%%%%%%%%%% IMPORTANT: %%%%%%%%%%%%%%%%%%%%%%%%% 
//Le fichier de probleme (.txt) et les fichiers de la DLL (GeneticDLL.dll et GeneticDLL.lib) doivent 
//se trouver dans le répertoire courant du projet pour une exécution à l'aide du compilateur. Indiquer les
//arguments du programme dans les propriétés du projet - débogage - arguements.
//Sinon, utiliser le répertoire execution.

//***************************************************************************************
// Prototype des fonctions se trouvant dans la DLL 
//***************************************************************************************
//DESCRIPTION:	Lecture du Fichier probleme et initialiation de la structure Problem
extern "C" _declspec(dllimport) void LectureProbleme(std::string FileName, TProblem & unProb, TGenetic &unGenetic);

//DESCRIPTION:	Fonction d'affichage à l'écran permettant de voir si les données du fichier problème ont été lues correctement
extern "C" _declspec(dllimport) void AfficherProbleme(TProblem unProb);

//DESCRIPTION:	Évaluation de la fonction objectif d'une solution et MAJ du compteur d'évaluation. 
extern "C" _declspec(dllimport) void EvaluerSolution(TIndividu & uneSol, TProblem unProb, TGenetic &unGenetic);

//DESCRIPTION: Fonction qui génére une population initiale en s'assurant d'avoir des solutions valides*/
extern "C" _declspec(dllimport)void CreerPopInitialeAleaValide(std::vector<TIndividu> & unePop, TProblem unProb, TGenetic & unGenetic);

//DESCRIPTION: Fonction qui affiche le détail des solutions (de Debut jusqu'a Fin-1) dans la population courante
extern "C" _declspec(dllimport) void AfficherSolutions(std::vector<TIndividu> unePop, int Debut, int Fin, int Iter, TProblem unProb);

//DESCRIPTION: Fonction de tri croissant des individus dans la population entre Debut et Fin-1 INCLUSIVEMENT 
extern "C" _declspec(dllimport) void TrierPopulation(std::vector<TIndividu> & unePop, int Debut, int Fin);

//DESCRIPTION: Copie de la séquence et de la fonction objectif dans une nouvelle TSolution. La nouvelle TSolution est retournée.
extern "C" _declspec(dllimport) void CopierSolution(const TIndividu uneSol, TIndividu &Copie, TProblem unProb);

//DESCRIPTION: Fonction qui réalise la MUTATION (modification aléatoire) sur une solution: Inversion de sous-séquence et échange de 2 commandes.
extern "C" _declspec(dllimport) void Mutation(TIndividu & Mutant, TProblem unProb, TGenetic & unGen);

//DESCRIPTION: Fonction de sélection d'un individu par tournoi
//extern "C" _declspec(dllexport) Selection(std::vector<TIndividu> unePop, int _Taille, TProblem unProb);

//DESCRIPTION: Fonction affichant les résultats de l'algorithme génétique
extern "C" _declspec(dllexport) void AfficherResultats(TIndividu uneBest, TProblem unProb, TGenetic unGen);

//DESCRIPTION: Fonction affichant les résultats de l'algorithme génétique dans un fichier texte
extern "C" _declspec(dllexport) void AfficherResultatsFichier(TIndividu uneBest, TProblem unProb, TGenetic unGen, std::string FileName);

//DESCRIPTION:	Libération de la mémoire allouée dynamiquement
extern "C" _declspec(dllexport) void LibererMemoireFinPgm(std::vector<TIndividu> & unePop, std::vector<TIndividu> & unePopEnfant, TIndividu & uneBest, TProblem & unProb, TGenetic unGen);

//***************************************************************************************
// Prototype des fonctions locales 
//***************************************************************************************
TIndividu Croisement(TProblem unProb, TGenetic & unGen, vector<pair<TIndividu, int>> Parents, std::vector<int> &Castes);
void Remplacement(std::vector<TIndividu> & Parents, std::vector<TIndividu> Enfants, TProblem unProb, TGenetic unGen, std::vector<int> &Castes);

//Utilitaire de tri
bool sortbysec(const pair<int, int> &a, const pair<int, int> &b) { return (a.second < b.second); }
bool sortbyobjf(const pair<TIndividu, int> &a, const pair<TIndividu, int> &b) { return (a.first.FctObj < b.first.FctObj); }

//Calcul du taux de consanguinité. Empeche des solutions trop similaires de se croiser.
double Consanguinity(std::vector<pair<TIndividu, int>> parents, TProblem unProb, TIndividu Best);

//Selection par tournois
int Selection(std::vector<TIndividu> unePop, int _Taille, TProblem unProb);

/**
* Fonction main
*/
int main(int NbParam, char *Param[])
{
	TProblem LeProb;					//Définition de l'instance de problème
	TGenetic LeGenetic;					//Définition des paramètres du recuit simulé
	std::vector<TIndividu> Pop;			//Population composée de Taille_Pop Individus 
	std::vector<TIndividu> PopEnfant;	//Population d'enfant
	TIndividu Best;						//Meilleure solution depuis le début de l'algorithme
	std::vector<int> Castes;

	int i;
	double Alea;

	string NomFichier;

	//Lecture des paramètres
	NomFichier.assign(Param[1]);
	LeGenetic.TaillePop = atoi(Param[2]);
	LeGenetic.ProbCr = atof(Param[3]);
	LeGenetic.ProbMut = atof(Param[4]);
	LeGenetic.NB_EVAL_MAX = atoi(Param[5]);
	LeGenetic.TaillePopEnfant = (int)ceil(LeGenetic.ProbCr * LeGenetic.TaillePop);
	LeGenetic.Gen = 0;

	//Définition de la dimension des tableaux
	Pop.resize(LeGenetic.TaillePop);				//Le tableau utilise les indices de 0 à TaillePop-1.
	PopEnfant.resize(LeGenetic.TaillePopEnfant);	//Le tableau utilise les indices de 0 à TaillePopEnfant-1

													//Lecture du fichier de donnees
	LectureProbleme(NomFichier, LeProb, LeGenetic);
	AfficherProbleme(LeProb);

	//Initialisation de la population initiale NB: Initialisation de la population entraine des evaluation de solutions.
	//CptEval est donc = TaillePop au retour de la fonction.
	CreerPopInitialeAleaValide(Pop, LeProb, LeGenetic);
	//AfficherSolutions(Pop, 0, LeGenetic.TaillePop, LeGenetic.Gen, LeProb);
	

	Castes.resize(Pop.size());
	//Initialisation des castes
	for (int i = 0; i < Pop.size(); i++) {
		Castes[i] = i % NB_CASTES;
	}

	
	//Tri de la population
	TrierPopulation(Pop, 0, LeGenetic.TaillePop);
	AfficherSolutions(Pop, 0, LeGenetic.TaillePop, LeGenetic.Gen, LeProb);

	//Initialisation de de la meilleure solution
	CopierSolution(Pop[0], Best, LeProb);
	cout << endl << "Meilleure solution de la population initiale: " << Best.FctObj << endl << endl;

	//====================================================================

	//Boucle principale de l'algorithme génétique
	do
	{
		LeGenetic.Gen++;
		//Sélection et croisement
		for (i = 0; i<LeGenetic.TaillePopEnfant; i++)
		{	
			// Condition pour eviter le depassement d'évaluation
			if (LeGenetic.CptEval >= LeGenetic.NB_EVAL_MAX) {
				// MAJ de la pop enfant pour la dernière boucle.
				LeGenetic.TaillePopEnfant = i;
				break;
			}
			//+SÉLECTION de NB_PARENTS parents
			vector<pair<TIndividu, int>> Parents;
			int k = 0;
			int nbParent = NB_PARENTS < 3 ? NB_PARENTS : 2 + (rand() / INT_MAX > 0.8) * (rand() % (NB_PARENTS -2));
			while (Parents.size() < nbParent) {
				int index = Selection(Pop, LeGenetic.TaillePop, LeProb);
				Parents.push_back(make_pair(Pop[index], Castes[index]));
				if ((k++ < 1000) && ((rand() / INT_MAX) > Consanguinity(Parents, LeProb, Best))) Parents.pop_back();
			}

			//CROISEMENT entre les NB_PARENTS parents. Création d'UN enfant.
			PopEnfant[i] = Croisement(LeProb, LeGenetic, Parents, Castes);

			//MUTATION d'une solution
			Alea = double(rand()) / double(RAND_MAX);
			if (Alea < LeGenetic.ProbMut)
			{
				//Vérification pour ne pas perdre la meilleure solution connue avant mutation
				if (Best.FctObj > PopEnfant[i].FctObj)
					CopierSolution(PopEnfant[i], Best, LeProb);
				Mutation(PopEnfant[i], LeProb, LeGenetic);
			}
		}
		//AfficherSolutions(PopEnfant, 0, LeGenetic.TaillePopEnfant, LeGenetic.Gen, LeProb);

		//REMPLACEMENT de la population pour la prochaine génération
		Remplacement(Pop, PopEnfant, LeProb, LeGenetic, Castes);

		//Conservation de la meilleure solution
		TrierPopulation(Pop, 0, LeGenetic.TaillePop);

		if (Best.FctObj > Pop[0].FctObj) {
			CopierSolution(Pop[0], Best, LeProb);
		}

		cout << "Meilleure solution trouvee (Generation# " << LeGenetic.Gen << "): " << Best.FctObj << endl;

	} while (LeGenetic.CptEval < LeGenetic.NB_EVAL_MAX);

	AfficherResultats(Best, LeProb, LeGenetic);

	// Affichage de la meilleure solution
	cout << "Meilleure solution: " << endl;
	for (auto ville : Best.Seq)
		cout << ville << " ";
	cout << endl;

	AfficherResultatsFichier(Best, LeProb, LeGenetic, "Resutats.txt");

	LibererMemoireFinPgm(Pop, PopEnfant, Best, LeProb, LeGenetic);

	system("PAUSE");
	return 0;
}

/**
* Sélection des parents.
* Cette fonction retourne un indice dans la population.
* Une sélection par tournoi est effectuée.
* Selection du meilleur individu parmis 2 tiré de la population.
*/
int Selection(std::vector<TIndividu> unePop, int _Taille, TProblem unProb) {

	int a, b;
	a = rand() % _Taille;
	// On selectionne un second individu différent du premier.
	while ((b = rand() % _Taille) == a) {}

	return unePop[a].FctObj > unePop[b].FctObj ? a : b;
}

/**
* Calcul du taux de consanguinité.
* Celui-ci dépend de la similitude entre les différents parents.
* Plus les parents se ressemblent, plus celui-ci sera élévé.
* Néanmoins, pour ne pas "pénaliser" les "bonnes" solution, un facteur correcteur est appliqué
* en fonction de la meilleure solution actuellement trouvée.
*/
double Consanguinity(std::vector<pair<TIndividu, int>> parents, TProblem unProb, TIndividu Best) {

	//S'il n'y a qu'un seul parent ou moins, le taux de consanguinité est nul
	if (parents.size() <= 1) return 0;

	//Calcule le nombre de simularités entre deux individus
	int c = 0, n = parents.size();
	double x = 0;
	for (int j = 0; j < n; j++) {
		for (int i = 0; i < unProb.NbVilles - 1; i++) {
			c += (parents[(n + j - 1) % n].first.Seq[i] == parents[(n + j) % n].first.Seq[i]) / n;
		}
		x = max(x, parents[j].first.FctObj / Best.FctObj);
	}
	c /= unProb.NbVilles;

	//Facteur correcteur (basé sur une sigmoïde)
	double corr = 2 / (1 + exp(5 * (x - 1)));
	return c * (1 - corr);

}


/**
* Fonction qui réalise le CROISEMENT (échange de genes) entre deux parents. Retourne l'enfant produit.
*/
TIndividu Croisement(TProblem unProb, TGenetic & unGen, vector<pair<TIndividu, int>> Parents, std::vector<int> &Castes)
{

	//====================================================================================
	//INITIALISATION

	TIndividu Enfant;
	map<int, TVille> villes;

	//Pour chaque ville, on va garder un structure TVille qui contient :
	//- une liste d'adjacence, construite à partir des gènes du parent
	//- une copie de la liste de préséances récupérée depuis le TProblem
	for (int i = 1; i < unProb.NbVilles - 1; i++)
		villes.insert(pair<int, TVille>(i, TVille{ set<int>(), set<int>(unProb.Pred[i].begin(), unProb.Pred[i].end()) }));

	//Pour chaque ville i, on récupère les adjacences (i - 1 et i + 1) dans chaque parent
	for (int i = 1; i < unProb.NbVilles - 1; i++) {
		for (auto parent : Parents) {
			villes[parent.first.Seq[i]].Adj.insert(parent.first.Seq[i - 1]);
			villes[parent.first.Seq[i]].Adj.insert(parent.first.Seq[i + 1]);
		}
	}

	//====================================================================================

	//Placement de la première ville
	int villeAjoutee = 0;

	//Placement des villes intermédiaires
	for (int k = 1; k < unProb.NbVilles; k++) {

		//Ajout de la ville dans l'enfant et suppression de celle-ci de la liste des villes restantes
		Enfant.Seq.push_back(villeAjoutee);
		villes.erase(villeAjoutee);
		//Suppression des occurences de la ville ajoutée dans les listes d'adjacence et préséance des villes restantes
		for (auto &tville : villes) {
			tville.second.Adj.erase(villeAjoutee);
			tville.second.Pred.erase(villeAjoutee);
		}

		//Trie des villes par ordre croissant du nombre d'adjacence qu'elles possèdent (i.e. de la plus contrainte à la moins contrainte)
		vector<pair<int, int>> villesOrdonnees;
		for (auto &ville : villes)
			villesOrdonnees.push_back(pair<int, int>(ville.first, ville.second.Adj.size()));
		sort(villesOrdonnees.begin(), villesOrdonnees.end(), sortbysec);

		//Recherche des villes candidates en commançant par les plus contraintes
		//i.e. celles qui ont le moins d'adjacence (de 0 à 4 voisins)
		for (int i = 0; i <= 2 * Parents.size(); i++) {

			//Récupération des villes avec i villes dans la liste d'adjacence
			vector<pair<int, int>> villesCandidates;
			auto a = find_if(villesOrdonnees.begin(), villesOrdonnees.end(), [&i](pair<int, int> p) { return p.second == i; }) - villesOrdonnees.begin();
			auto b = find_if(villesOrdonnees.begin(), villesOrdonnees.end(), [&i](pair<int, int> p) { return p.second == i + 1; }) - villesOrdonnees.begin();
			for (int j = a; j < b; j++)
				villesCandidates.push_back(pair<int, int>(villesOrdonnees[j].first, unProb.Distance[villeAjoutee][villesOrdonnees[j].first]));

			//Tri par distance par rapport à la ville actuelle
			sort(villesCandidates.begin(), villesCandidates.end(), sortbysec);

			//On teste ensuite le respect de la préséance des candidats
			//Le premier valide sera selectionné
			for (auto j : villesCandidates) {
				auto &ville = villes[j.first];
				if ((ville.Pred.size() == 0) && (i = INT_MAX)) {
					villeAjoutee = j.first;
					break;
				}
			}
		}
	}

	//Ajout de la dernière ville
	Enfant.Seq.push_back(unProb.NbVilles - 1);
	EvaluerSolution(Enfant, unProb, unGen);

	//Caste de l'enfant (dépendamment de celles de ses parents)
	Castes.push_back(Castes[Parents[rand() % Parents.size()].second]);
	return (Enfant);
}


/**
* Remplacement mu + lambda.
* On selectionne parmis les parents et les enfants les meilleures solutions.
* On conserve la même taille de population après le remplacement.
*/
void Remplacement(std::vector<TIndividu> &Parents, std::vector<TIndividu> Enfants, TProblem unProb, TGenetic unGen, std::vector<int> &Castes)
{
	//Déclaration et dimension dynamique d'une population temporaire pour contenir tous les parents et les enfants
	std::vector<TIndividu> Temporaire;
	Temporaire.reserve(unGen.TaillePop + unGen.TaillePopEnfant);

	// Ajout des parents dans la population temporaire
	for (auto el : Parents) {
		Temporaire.push_back(el);
	}
	// Ajout des enfants dans la population temporaire
	for (auto el : Enfants) {
		Temporaire.push_back(el);
	}

	// Trie de la population par leur fonction objective
	TrierPopulation(Temporaire, 0, unGen.TaillePop + unGen.TaillePopEnfant);


	// Libération de la population de parents
	Parents.clear();
	Parents.resize(unGen.TaillePop);

	// Selection des meilleures solutions parmis la solution temporaire, elle constitue notre nouvelle population
	// On la stock dans les parents
	// Toutefois, seule un certain nombre d'individus sont retenus par caste
	std::vector<int> CastesPop;
	CastesPop.resize(NB_CASTES, 0);
	std::vector<int> newCastes;
	newCastes.resize(unGen.TaillePop);
	double nbParCaste = (double)unGen.TaillePop / NB_CASTES;
	int j = 0;
	for (int i = 0; i < unGen.TaillePop + unGen.TaillePopEnfant; i++) {
		int caste = Castes[i];
		if (CastesPop[caste]++ < floor((caste + 1) * nbParCaste) - floor(caste * nbParCaste)) {
			CopierSolution(Temporaire[i], Parents[j], unProb);
			newCastes[j] = caste;
			j++;
		}
	}

	//Libère les ressources liées à la carte des castes.
	Castes = newCastes;

	//Libération de la population temporaire
	for (int i = 0; i< unGen.TaillePop + unGen.TaillePopEnfant; i++)
		Temporaire[i].Seq.clear();
	Temporaire.clear();

}


/**
* Trie une population par ordre croissant en fonction de la fonction objective.
*/

void TrierPopulation(std::vector<TIndividu> & unePop, std::vector<int> & castes, int Debut, int Fin) {

	// On crée un vecteur de pair individu & caste pour conserver les castes dans le trie
	std::vector<pair<TIndividu, int>> sortedPop;
	sortedPop.reserve(Fin);

	// On remplis le vecteur
	for (int i = 0; i < Fin; i++) {
		sortedPop[i] = make_pair(unePop[i], castes[i]);
	}
	
	// On trie les individus en fonction de leur fonction objective
	sort(sortedPop.begin(), sortedPop.end(), sortbyobjf);
	
	// On clear les vecteurs afin de leurs reassigner les valeurs triées
	unePop.clear();
	castes.clear();
	unePop.reserve(Fin);
	castes.reserve(Fin);

	// On reassigne les valeurs triées
	for (int i = 0; i < Fin; i++) {
		unePop[i] = sortedPop[i].first;
		castes[i] = sortedPop[i].second;
	}
}

