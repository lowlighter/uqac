Exercices sur **MPI**.

### d3-1.c | Nombre de premiers jumeaux < N

#### Compilation et lancement
```
mpicc d3-1.c -lm
./mscp ./a.out
mpirun -np 16 ./a.out
```

#### Exemple d'exécution
La première colonne indique le n° du noeud, la seconde le nombre de premiers jumeaux qu'il a trouvé et le troisième son temps d'exécution en secondes.
La dernière colonne montre l'interval de nombres traité par le noeud.
```
Entrez une valeur entiere: 65536
(5)      58            0.001655  [20480 : 24576]
(12)     50            0.001081  [49152 : 53248]
(13)     44            0.001107  [53248 : 57344]
(14)     40            0.001141  [57344 : 61440]
(10)     46            0.001015  [40960 : 45056]
(11)     45            0.002823  [45056 : 49152]
(9)      42            0.010990  [36864 : 40960]
(15)     36            0.001115  [61440 : 65536]
(2)      64            0.000584  [8192 : 12288]
(3)      49            0.002434  [12288 : 16384]
(4)      58            0.001633  [16384 : 20480]
(6)      51            0.000820  [24576 : 28672]
(7)      48            0.001531  [28672 : 32768]
(0)      107           0.000311  [0 : 4096]
(1)      70            0.010304  [4096 : 8192]
(8)      52            0.001454  [32768 : 36864]
65536    860           0.019911
```


### d3-2.c | Algorithme de Floyd
```
mpicc d3-2.c
./mscp ./a.out
mpirun -np 16 ./a.out < adjacence_matrix
```

#### Exemple d'exécution
La première matrice est celle reçue en entrée, la seconde est celle après avoir exécuté l'algorithme de Floyd.
La valeur `-1` est utilisée pour symboliser la valeur `∞`.
```
   0    3    3    ∞    5
   3    0   10    8    ∞
   3   10    0    ∞    7
   ∞    8    ∞    0    ∞
   5    ∞    7    ∞    0

   0    3    3   11    5
   3    0    6    8    8
   3    6    0   14    7
  11    8   14    0   16
   5    8    7   16    0
```

### mscp | Multiple - SCP
**MPI** fonctionne sur plusieurs noeuds, il est donc nécessaire de transférer via la commande `scp` le fichier exécutable aux autres noeuds après compilation.

Ce script bash est un modèle simple permettant de réaliser cette opération, toutefois il requiert d'être modifié par rapport aux noms de vos serveurs si vous comptez réutiliser le code.