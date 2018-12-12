__kernel void floyd(__global int* A, int n, int k) {
	
	//Récupération des indices
	int i = get_global_id(0);
	int j = get_global_id(1);

	//Récupération des valeurs
	int A_ij = A[i*n+j];
	int A_ik_kj = A[i*n+k] + A[k*n+j];

	//Actualisation du chemin le plus court
	if (A_ik_kj < A_ij)
		A[i*n+j] = A_ik_kj;

}
//Ne pas supprimer ce commentaire (pour une raison obscure des caractères supplémentaires s'ajoute lors du fread)