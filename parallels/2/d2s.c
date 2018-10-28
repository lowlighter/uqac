//Dépendances
  #include <stdio.h>
  #include <stdlib.h>
  #include <limits.h>
  #include <omp.h>

//Constantes
  #define INFINITY INT_MAX

//Déclarations
  void fusion(int* u, int* v, int* t);
  int n = 0;

/** Point d'entrée. */
  int main(int argc, char *argv[]) {
    //Lecture du nombre d'entrées
      scanf("%d", &n);

    //Allocation des tableaux à fusionner
      int* u = (int*) malloc((n+1) * sizeof(int));
      int* v = (int*) malloc((n+1) * sizeof(int));
    //Allocation du tableau résultant de la fusion
      int* t = (int*) malloc(2*n * sizeof(int));

    //Remplissage des tableaux à fusionner
      for (int i = 0; i < n; i++) scanf("%d", &u[i]);
      for (int i = 0; i < n; i++) scanf("%d", &v[i]);

    //Appel de la fusion + chronométrage
      double chrono = omp_get_wtime();
      fusion(u, v, t);
      printf("SEQUENTIEL\r\n");
      printf("Temps d'exécution : %lf\r\n", omp_get_wtime() - chrono);

    //Affichage du tableau résultant
      for (int i = 0; i < 2*n; i++) printf("%d ", t[i]);
      printf("\r\n");
  }

/** Fonction de fusion où u et v sont fusionnées dans t. */
  void fusion(int* u, int* v, int* t) {
    //Initialisation des itérateurs
      int i = 0;
      int j = 0;
    //Initialisation des valeurs de fin de tableau
      u[n] = INFINITY;
      v[n] = INFINITY;

    //Fusion
      for (int k = 0; k < 2*n; k++) {
        if (u[i] < v[j]) 
          t[k] = u[i++];
        else 
          t[k] = v[j++];
      }
  }