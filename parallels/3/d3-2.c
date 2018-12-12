/**
 * Exécute l'algorithme de Floyd avec RMA (Remote Memory Access).
 * 
 * Auteurs : Lecoq S., Azzouza T., Tondeux V.
 */

//Dépendances
#include <stdio.h>
#include <mpi.h>
#include <stdlib.h>

//Définitions
#define MIN(a,b)           ((a)<(b)?(a):(b))
#define MAX(a,b)           ((a)>(b)?(a):(b))
#define BLOCK_LOW(id,p,n)  ((id)*(n)/(p))
#define BLOCK_HIGH(id,p,n) (BLOCK_LOW((id)+1,p,n)-1)
#define BLOCK_SIZE(id,p,n) (BLOCK_HIGH(id,p,n)-BLOCK_LOW(id,p,n)+1)
#define BLOCK_OWNER(j,p,n) (((p)*((j)+1)-1)/(n))
#define INF 100

//--------------------------------------------------
int main (int argc, char *argv[]) {
    int* a;         /* Matrice d'adjacence */
    int  i, j, k;
    int  id;        /* Rang du process */
    int  n;         /* Dimension de la matrice */
    int  p;         /* Nombre de process */
    double  time = 0, max_time = 0;

    void show_matrix(int* a, int n);
    void compute_shortest_paths (int*, int, int, int, MPI_Win*);

    //Démarrage de MPI
    MPI_Init (&argc, &argv);
    MPI_Comm_rank (MPI_COMM_WORLD, &id);
    MPI_Comm_size (MPI_COMM_WORLD, &p);

    //Declaration de la fenetre qui stocke la matrice.
    MPI_Win win;

    //Initialisation (si process 0)
    if (!id) {
        //Lecture du fichier d'entrée
        scanf("%d", &n);
        a = (int*) malloc(n*n*sizeof(int));
        for (i = 0 ; i < n*n ; i++) {
            scanf("%d", &a[i]);
            if (a[i] < 0) {
                a[i] = INF;
            }
        }
        show_matrix(a, n);
    }

    //BROADCAST de la valeur n
    //(APPEL COLLECTIF)
    MPI_Bcast(&n, 1, MPI_INT, 0, MPI_COMM_WORLD);

     //Allocation mémoire de a pour les autres processus
    if (id) {
        a = (int*) malloc(n*n*sizeof(int));
    }

    //Création de la fenetre
    if (!id) {
        //Fenetre d'exposition de la matrice
        MPI_Win_create(a, n*n*sizeof(int), sizeof(int),
            MPI_INFO_NULL, MPI_COMM_WORLD, &win);
    }
    if (id) {
        //Fenetre d'accès pour la matrice
        MPI_Win_create(NULL, 0, sizeof(int),
            MPI_INFO_NULL, MPI_COMM_WORLD, &win);
        for (i = 0; i < n*n; i++) { a[i] = INF; }
    }


    //Floyd + Temps d'exécution
    MPI_Win_fence(0, win);
    time = -MPI_Wtime();
    compute_shortest_paths(a, id, p, n, &win);
    time += MPI_Wtime();
    MPI_Reduce (&time, &max_time, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);

    if (!id) {
        show_matrix(a, n);
        printf("Floyd, matrix size %d, %d processes: %6.2f seconds\n", n, p, max_time);
    }

    //Libère les ressources et résultats
    MPI_Win_free(&win);
    MPI_Finalize();
    free(a);

    return 0;
}





/**
 * Affiche un tableau bi-dimensionnel sur la sortie standard.
 */
void show_matrix(int* a, int n) {
    int i, k;
    for (i = 0 ; i < n*n ; i++) {
        a[i] == INF ? printf("   ∞ ") : printf("%4d ", a[i]);
        if ((i % n) == n-1) {
            printf("\n");
        }
    }
    printf("\n");
}

//-----------------------------------------------------------
void compute_shortest_paths (int* a, int id, int p, int n, MPI_Win* win) {
    int  i, j, k;//, root, offset;

    //Lignes à traiter
    int low = BLOCK_LOW(id, p, n); //Premier n° de ligne à traiter
    int high = BLOCK_HIGH(id, p, n); //Dernier n° de ligne à traiter
    int size = BLOCK_SIZE(id, p, n); //Nombre de lignes à traiter

    //Récupération des lignes qui sont assignées au processeur
    MPI_Get(&a[low*n], size*n, MPI_INT, 0, low*n, size*n, MPI_INT, *win);
    MPI_Win_fence(0, *win);

    //Floyd
    for (k = 0; k < n; k++) {
        //Broadcast (le propriétaire de la ligne k l'envoie à tous ceux qui ne l'ont pas en mémoire)
        MPI_Bcast(&a[k*n], n, MPI_INT, BLOCK_OWNER(k, p, n), MPI_COMM_WORLD);

        //Mise à jour des valeurs
        for (i = low; i < low+size; i++) {
            for (j = 0; j < n; j++) {
                a[i*n+j] = MIN(a[i*n+j], a[i*n+k] + a[k*n+j]);
            }
        }
    }
    //Ecriture des lignes traitées
    MPI_Put(&a[low*n], size*n, MPI_INT, 0, low*n, size*n, MPI_INT, *win);
    MPI_Win_fence(0, *win);
}