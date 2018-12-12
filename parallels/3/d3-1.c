/**
 * Compte le nombre de nombre premiers jumeaux (i.e. nombre premiers successifs avec un écart de 2). 
 * Le travail est répartie entre les différents noeuds grâce à MPI.
 * 
 * Auteurs : Lecoq S., Azzouza T., Tondeux V.
 */

//Dépendances
#include <math.h>
#include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

//Déclarations
int main (int argc, char *argv[]);
int prime_number (int n, int id, int p);

/******************************************************************************/
int main (int argc, char *argv[]) {
    //Initialisation
    int i, ierr;
    int n;
    int id, p;
    int primes, primes_part;
    double wtime;
    int next = 0;

    //Initialisation de MPI
    ierr = MPI_Init(&argc, &argv);
    ierr = MPI_Comm_size(MPI_COMM_WORLD, &p);
    ierr = MPI_Comm_rank(MPI_COMM_WORLD, &id);

    //Entrée utilisateur (si processus principal)
    if (!id){
        printf("Entrez une valeur entiere: ");
        fflush(stdout);
        scanf("%d", &n);
        if (n < 2*p) {
            fprintf(stderr, "La taille de la liste doit être supérieure ou égale au double du nombre de threads.\n");
            return 1;
        }
        if (n % p) printf("n n'est pas divisible par p. Il manquera donc %d (reste de la division euclidienne de n/p) éléments non traités à la fin.\n", n%p);
    }

    //MPI
    {
        //Début du chronomètre
        if (id == 0) wtime = MPI_Wtime();
        //Broadcast de la valeur n à tous les noeuds
        ierr = MPI_Bcast(&n, 1, MPI_INT, 0, MPI_COMM_WORLD);
        //Exécution de la fonction
        primes_part = prime_number(n, id, p);
        //Réduction du résultat
        ierr = MPI_Reduce(&primes_part, &primes, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
        //Affichage (si processus principal)
        if (!id) {
            wtime = MPI_Wtime() - wtime;
            printf ("  %8d  %8d  %14f\n", n, primes, wtime);
        }
    }
    ierr = MPI_Finalize();

    return 0;
}

/******************************************************************************/
int prime_number ( int n, int id, int p ) {
    //Initialisation
    int i, j;
    int prime, total = 0;
    int first_prime_number = 0, prime_block_prec;
    int prec = 0, twin = 0;
    double wtime = MPI_Wtime();

    //On commence à un indice impair pour éviter les nombres pairs (ceux-ci étant divisibles par 2 donc non premiers)
    int debut=id*(n/p);
    debut=(debut%2)?debut: debut+1;

    //On s'interdit de débuter à 1, car 1 n'est pas premier.
    if (debut <= 1) debut = 3;

    //Parcours des nombres impairs
    for (i=debut; i< (id+1)*(n/p); i=i+2) {
    
        //Test de nombre premier
        prime = 1;
        for ( j = 2; j <= sqrt((double) i) ; j++ ) {
            //Pas premier
            if ( ( i % j ) == 0 ) {
                prime = 0;
                break;
            }
        }

        //Si le nombre est premier
        if (prime == 1) {
            //Sauvegarde du premier nombre premier du bloc
            if (!first_prime_number) first_prime_number = i;

            //Test des jumeaux
            if (i - prec == 2) twin++;
            prec = i;
        }
    }

    //Envoi du dernier nombre premier trouvé au bloc suivant
    if (id < p -1) {
        MPI_Send(&prec, 1, MPI_INT, id+1, 0, MPI_COMM_WORLD);
    }
    //Récéption du dernier nombre premier trouvé par le bloc précédent
    if (id > 0) {
        MPI_Recv(&prime_block_prec, 1, MPI_INT, id-1, 0, MPI_COMM_WORLD, 0);
        if ((first_prime_number - prime_block_prec) == 2) twin++;
    }

    //Affichage du résultat
    printf("(%d)\t %d\t %14f\t [%d : %d]\n", id, twin, MPI_Wtime()-wtime, id*(n/p), (id+1)*(n/p)+(n%p));
    fflush(stdout);
  return twin;
}