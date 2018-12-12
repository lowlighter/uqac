/**
 * Exécute l'algorithme de Floyd avec OpenCL.
 *
 * Auteurs : Lecoq S., Azzouza T., Tondeux V.
 */

//Dépendances
#include <stdio.h>
#include <stdlib.h>
#include <sys/stat.h>
#include <time.h>
#include <CL/cl.h>

//Définitions
#define SOURCE_CL "Source.cl"

//Déclarations de fonctions
char* loadProgramSource(const char *filename);

/**
 * Point d'entrée du programme.
 */
int main() {

	//Config
	int USE_LOCAL_GROUP = 0; //Si 1, utilise les groupes de taille locale
	int BLOCK_SIZE = 3; //Effectif si USE_LOCAL_GROUP est 1, taille des groupes locaux (doit être un multiple de N)
	int N = 0; //Dimensions
	int MAX_N_FOR_FULL_DISPLAY = 30; //Affichage de la matrice si N est petit

	//-----------------------------------------------------
	// ETAPE 0 : Configuration et initialisation des données
	//-----------------------------------------------------

	//Entrée utilisateur : dimensions de la matrice d'entrée
	printf(">>> CONFIGURATION\n");
	printf("Input N value : ");
	scanf_s("%d", &N);

	//Entrée utilisateur : taille des Workgroup
	printf("Input Workgroup size (type 0 for auto) : ");
	scanf_s("%d", &BLOCK_SIZE);
	USE_LOCAL_GROUP = BLOCK_SIZE > 0 ? 1 : 0;
	printf("\n");

	//Initialization
	int *A = NULL;
	int i = 0, j = 0;
	size_t datasize = sizeof(int) * N * N;

	//Initialisation des données
	printf(">>> DATA INITIALIZATION\n");
	printf("N : %d (%d elements)\n", N, N * N);
	printf("Status : ");
	A = (int*) malloc(datasize);
	for (i = 0; i < N; i++) {
		for (j = 0; j < N; j++) {
			A[i*N + j] = N + 1; //Valeur par défaut
			if (i == j) A[i*N + j] = 0; //Arc de i à i
			if (i == j-1) A[i*N + j] = 1; //Arc de i à i+1
			if ((i == N-1)&&(j == 0)) A[i*N + j] = 1; //Arc de n-1 à 0
		}
	}
	printf("OK\n");

	//Affichage de la matrice
	if (N <= MAX_N_FOR_FULL_DISPLAY) {
		printf("Values of A[i][j] :\n");
		for (i = 0; i < N*N; i++) {
			printf("%3d ", A[i]);
			if ((i % N) == N - 1) printf("\n");
		}
	}
	printf("\n");

	//-----------------------------------------------------
	// ETAPE 1 : Chargement du code source
	//-----------------------------------------------------

	//Debug
	cl_int status;
	printf(">>> SOURCE CODE\n");

	//Chargement du code source
	printf("loadProgramSource : ");
	char* programSource = loadProgramSource(SOURCE_CL);
	if (programSource == NULL) printf("ERROR (NULL)\n"); else printf("OK\n");
	printf("\n");

	//-----------------------------------------------------
	// ETAPE 2 : Récupération des plateformes
	//-----------------------------------------------------

	//Debug
	printf(">>> PLATFORMS AND DEVICES\n");

	//Récupération du nombre de plateformes
	cl_uint numPlatforms = 0;
	cl_platform_id *platforms = NULL;
	status = clGetPlatformIDs(0, NULL, &numPlatforms);
	printf("Available platforms : %d\n", numPlatforms);

	//Récupération des plateformes
	platforms = (cl_platform_id*)malloc(numPlatforms * sizeof(cl_platform_id));
	status = clGetPlatformIDs(numPlatforms, platforms, NULL);

	//Récupération du nom de la plateforme principale
	char Name[1000];
	clGetPlatformInfo(platforms[0], CL_PLATFORM_NAME, sizeof(Name), Name, NULL);
	printf("Primary platform : %s\n", Name);
	fflush(stdout);

	//-----------------------------------------------------
	// ETAPE 3 : Récupération des périphériques
	//-----------------------------------------------------

	//Récupération du nombre de périphériques de la plateforme principale
	cl_uint numDevices = 0;
	cl_device_id *devices = NULL;
	status = clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_ALL, 0, NULL, &numDevices);
	printf("Available devices : %d\n", (int)numDevices);

	//Récupération des noms des périphériques de la plateforme principale
	devices = (cl_device_id*)malloc(numDevices * sizeof(cl_device_id));
	status = clGetDeviceIDs(platforms[0], CL_DEVICE_TYPE_ALL, numDevices, devices, NULL);
	for (unsigned int i = 0; i < numDevices; i++) {
		clGetDeviceInfo(devices[i], CL_DEVICE_NAME, sizeof(Name), Name, NULL);
		printf("Device %d: %s\n", i, Name);
	}
	printf("\n");

	//-----------------------------------------------------
	// ETAPE 4 : Création du contexte et de la file d'attente
	//-----------------------------------------------------

	//Debug
	printf(">>> OPENCL CONFIGURATION\n");

	//Création du contexte
	printf("clCreateContext : ");
	fflush(stdout);
	cl_context context = clCreateContext(NULL, numDevices, devices, NULL, NULL, &status);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//Création de la file d'attente
	printf("clCreateCommandQueueWithProperties : ");
	fflush(stdout);
	cl_command_queue cmdQueue = clCreateCommandQueueWithProperties(context, devices[1], 0, &status);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//-----------------------------------------------------
	// ETAPE 5 : Compilation du programme OpenCL
	//-----------------------------------------------------

	//Chargement du programme
	printf("clCreateProgramWithSource : ");
	fflush(stdout);
	cl_program program = clCreateProgramWithSource(context,	1, (const char**)&programSource, NULL, &status);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//Compilation
	printf("clBuildProgam : ");
	fflush(stdout);
	status = clBuildProgram(program, numDevices, devices, NULL, NULL, NULL);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//Affichage des erreurs de compilation
	if (status == CL_BUILD_PROGRAM_FAILURE) {
		size_t log_size;
		clGetProgramBuildInfo(program, devices[0], CL_PROGRAM_BUILD_LOG, 0, NULL, &log_size);
		char *log = (char *)malloc(log_size);
		clGetProgramBuildInfo(program, devices[0], CL_PROGRAM_BUILD_LOG, log_size, log, NULL);
		printf("%s\n", log);
		return 0;
	}

	//-----------------------------------------------------
	// ETAPE 6 : Création des buffers pour les périphériques
	//-----------------------------------------------------

	//Création des buffers
	printf("clCreateBuffer : ");
	fflush(stdout);
	cl_mem bufferA = clCreateBuffer(context, CL_MEM_READ_WRITE, datasize, NULL, &status);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//Ecriture des données de l'hôte dans les buffers des périphériques
	printf("clEnqueueWriteBuffer : ");
	fflush(stdout);
	status = clEnqueueWriteBuffer(cmdQueue, bufferA, CL_TRUE, 0, datasize, A, 0, NULL, NULL);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");


	//-----------------------------------------------------
	// ETAPE 7 : Création et configuration du Kernel
	//-----------------------------------------------------

	//Création du kernel
	cl_kernel kernel = NULL;
	printf("clCreateKernel : ");
	fflush(stdout);
	kernel = clCreateKernel(program, "floyd", &status);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//Passage des paramètres destiné au kernel
	printf("clSetKernelArg : ");
	fflush(stdout);
	int K = 0;
	status = clSetKernelArg(kernel, 0, sizeof(cl_mem), (void*)&bufferA);
	status = clSetKernelArg(kernel, 1, sizeof(int), &N);
	status = clSetKernelArg(kernel,	2, sizeof(int), &K);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");

	//-----------------------------------------------------
	// ETAPE 8 : Configuration des work-items
	//-----------------------------------------------------

	//Nombre max de work-item dans un work-group
	size_t MaxGroup;
	clGetDeviceInfo(devices[1], CL_DEVICE_MAX_WORK_GROUP_SIZE, sizeof(size_t), &MaxGroup, NULL);
	printf("CL_DEVICE_MAX_WORK_GROUP_SIZE : %d\n", (int)MaxGroup);

	//Nombre max de work-item dans un work-group selon chaque dimension
	size_t MaxItems[3];
	clGetDeviceInfo(devices[1], CL_DEVICE_MAX_WORK_ITEM_SIZES, 3 * sizeof(size_t), MaxItems, NULL);
	printf("CL_DEVICE_MAX_WORK_ITEM_SIZES : (%d, %d, %d)\n", (int)MaxItems[0], (int)MaxItems[1], (int)MaxItems[2]);

	//Répartition des éléments
	size_t globalWorkSize[2] = { N, N };
	size_t localWorkSize[3] = { BLOCK_SIZE, BLOCK_SIZE };
	printf("\n");

	//-----------------------------------------------------
	// ETAPE 9 : Mise en file d'attente des appels aux kernels
	//-----------------------------------------------------

	//Debug
	printf(">>> KERNELS CALL\n");
	clock_t begin = clock();

	//Appels via clEnqueueNDRangeKernel
	int gstatus = 0;
	printf("\rKernel call : ... ");
	for (K = 0; K < N; K++) {
		clSetKernelArg(kernel, 2, sizeof(int), &K);
		gstatus += clEnqueueNDRangeKernel(cmdQueue, kernel, 2, NULL, globalWorkSize, USE_LOCAL_GROUP ? localWorkSize : NULL, 0, NULL, NULL);
	}
	clFinish(cmdQueue);
	if (status) printf("\rKernel call : ERROR   \n", status); else printf("\rKernel call : OK        \n");

	//Debug
	clock_t end = clock();
	double time_spent = (double)(end - begin) / CLOCKS_PER_SEC;
	printf("Execution time : %f sec\n", time_spent);
	printf("\n");

	//-----------------------------------------------------
	// ETAPE 10 : Récupération des données sur l'hôte depuis le buffer de sortie
	//-----------------------------------------------------

	//Debug
	printf(">>> RESULTS\n");

	//Lecture de la matrice C
	printf("clEnqueueReadBuffer : ", time_spent);
	status = clEnqueueReadBuffer(cmdQueue, bufferA, CL_TRUE, 0, datasize, A, 0, NULL, NULL);
	clFinish(cmdQueue);
	if (status) printf("ERROR (code %d)\n", status); else printf("OK\n");
	printf("\n");

	//Affichage de la matrice
	if (N <= MAX_N_FOR_FULL_DISPLAY) {
		printf("Values of A[i][j] :\n");
		for (i = 0; i < N*N; i++) {
			printf("%3d ", A[i]);
			if ((i % N) == N - 1) printf("\n");
		}
	}
	else {
		while (1) {
			printf("See value of A[i][j] :\n");
			printf("[i] : ");
			scanf_s("%d", &i);
			printf("[j] : ");
			scanf_s("%d", &j);
			if ((i >= 0)&(j >= 0)&&(i < N)&&(j < N)) printf("A[%d][%d] = %d\n\n", i, j, A[i*N+j]);
			else printf("A[%d][%d] = NULL\n\n", i, j);
		}
	}
	
	//-----------------------------------------------------
	// ETAPE 11 : Nettoyage des ressources
	//-----------------------------------------------------

	// Libération des ressources OpenCL
	clReleaseKernel(kernel);
	clReleaseProgram(program);
	clReleaseCommandQueue(cmdQueue);
	clReleaseMemObject(bufferA);
	clReleaseContext(context);

	//Libération des ressources de l'hôte
	free(A);
	free(platforms);
	free(devices);

	return 0;
}

/**
 * Charge un programme source en mémoire.
 */
char* loadProgramSource(const char *filename) {
	//Initialisation
	FILE *f;
	errno_t err;
	char *source;

	//Ouverture du fichier
	err = fopen_s(&f, filename, "r");
	if (err == 0) {
		//Récupération de la taille du fichier
		fseek(f, 0, SEEK_END);
		long fsize = ftell(f);
		fseek(f, 0, SEEK_SET);

		//Mise en mémoire du contenu du fichier
		source = (char *) malloc(fsize + 1);
		fread(source, fsize, 1, f);
		fclose(f);
		source[fsize] = 0;
		return source;
	}

	//Echec de la lecture du fichier
	return NULL;
}