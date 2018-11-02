//Dépendances
  #include <stdio.h>
  #include <stdlib.h>
  #include <omp.h>
  #include <unistd.h>
  #include <limits.h>

//Constantes
  #define INFINITY INT_MAX

//Déclarations
  void parallel_fusion(int* u, int p1, int r1, int p2, int r2, int* t, int p3);
  void fusion(int* u, int* v, int* t, int p1, int r1, int p2, int r2);
  int n = 0;
  int limit = 100000;
  int size = 0;

/** Point d'entrée. */
  int main(int argc, char *argv[]) {
    //Si précisé en argument, on met à jour le nombre de threads voulu
      if (argc >= 2) omp_set_num_threads(atoi(argv[1]));
      if (argc >= 3) limit = atol(argv[2]);

    //Lecture du nombre d'entrées
      scanf("%d", &n);
      
    //Allocation des tableaux à fusionner
      int* w = (int*) malloc(2*n * sizeof(int));
    //Allocation du tableau résultant de la fusion
      int* t = (int*) malloc(2*n * sizeof(int));

    //Remplissage des tableaux à fusionner
      for (int i = 0; i < 2*n; i++) scanf("%d", &w[i]);
      
    //Appel de la fusion + chronométrage
      double chrono = omp_get_wtime();
      #pragma omp parallel 
      {
        #pragma omp single
        { 
          parallel_fusion(w, 0, n, n, 2*n, t, 0); 
        }
        #pragma omp barrier
        {
          #pragma omp single
          {
            printf("PARALLEL (%d threads)\r\n", omp_get_num_threads());
            printf("Temps d'exécution : %lf\r\n", omp_get_wtime() - chrono);
          }
        }
      }
      
    //Affichage du tableau résultant
      for (int i = 0; i < 2*n; i++) printf("%d ", t[i]);
      printf("\r\n");
  }

/** Recherche x dans w entre les index p et r. */
  int binary_search(int x, int *w, int p, int r) {
    //Calcul de l'index médian
      int q = (p+r)/2;
    //Retour de l'index s'il s'agit de celui de l'élément recherché ou de l'unique élément du tableau
      if (r - p <= 1) return (x <= w[q]) ? q : q+1;
    //Appel récursif
      return (x <= w[q]) ? binary_search(x, w, p, q) : binary_search(x, w, q, r);
  }

/** Fonction de fusion où les parties [p1...r1] et [p2...r2] de w sont fusionnées dans t. */
  void parallel_fusion(int* w, int p1, int r1, int p2, int r2, int* t, int p3) {
    //Initialisation des itérateurs
      int n1 = r1-p1;
      int n2 = r2-p2;
    
    //On s'assure que [p1...r1] est le plus grand des deux sous-parties (on swappe si ce n'est pas le cas)
      if (n1 < n2) {
        int tmp;
        tmp = n1; n1 = n2; n2 = tmp;
        tmp = p1; p1 = p2; p2 = tmp;
        tmp = r1; r1 = r2; r2 = tmp;
      }

    //S'il reste encore des éléments à fusionner, fusion
      if (n1 > 0) {
        //Sequentiel
          if ((n2 > 0)&&(((r1 - p1) + (r2 - p2)) < limit)) {
            //Initialisation
              int* u = (int*) malloc( ((r1 - p1)+1) * sizeof(int));
              int* v = (int*) malloc( ((r2 - p2)+1) * sizeof(int));
              int i = 0;
              for (i = 0 ; i < r1-p1 ; i++) u[i] = w[p1+i];
              for (i = 0 ; i < r2-p2 ; i++) v[i] = w[p2+i];
            //Nouvelle tâche
              #pragma omp task
                fusion(u, v, t, p1, r1, p2, r2)
          }
        //Parallele
          else {
            //Calcul des index et affectation de l'élément
              int q1 = (p1+r1)/2;
              int q2 = binary_search(w[q1], w, p2, r2);
              int q3 = p3 + (q1-p1) + (q2-p2);
              t[q3] = w[q1];

            //Nouvelles tâches
              #pragma omp task
                parallel_fusion(w, p1, q1, p2, q2, t, p3);
              #pragma omp task
                parallel_fusion(w, q1+1, r1, q2, r2, t, q3+1);
          }
      }
  }

/** Fonction de fusion où u et v sont fusionnées dans t. */
  void fusion(int* u, int* v, int* t, int p1, int r1, int p2, int r2) {
    //Initialisation des itérateurs
      int i = 0;
      int j = 0;
    //Initialisation des valeurs de fin de tableau
      u[(r1-p1)] = INFINITY;
      v[(r2-p2)] = INFINITY;
   
    //Fusion
      int k = 0;
      for (k = p1 ; k < ((r1 - p1)+1) + ((r2 - p2)+1) ; k++) {
        if (u[i] < v[j]) 
          t[k] = u[i++];
        else 
          t[k] = v[j++];
      }
  }