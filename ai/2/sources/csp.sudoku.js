/** 
 * Sudoku.
 * 
 * Chaque case du sudoku représente une variable (il y a donc 9*9 variables).
 * Celles-ci sont initialisées à 'NaN' pour indiquer qu'elles ne sont pas définies, 
 *  ou à un nombre s'il s'agit d'une variable "fixe" qui fait partie du problème initial.
 * 
 * Les variables sont identifées par une string de la façon suivante : 
 *  "x:y", qui est basée sur les coordonnées de la case dans le sudoku.
 * 
 * Les domaines de valeurs de chaque case sont les chiffres que peut prendre chacune des cases.
 * Ils sont initialement définis de la façon suivante :
 *  [1..9] si la variable vaut 'NaN' (i.e. il s'agit d'une variable à résoudre)
 *  [Ø] si la variable est fixée (i.e. il s'agit d'une variable définissant le problème)
 * 
 * Les contraintes du sudoku sont représentées par un objet de la façon suivante :
 *  {a, b, op} où 'a' et 'b' sont les variables affectées par la contrainte et 'op' l'opérateur.
 *  Ici, seul l'opérateur '!=' a été défini car c'est le seul requis pour résoudre un sudoku.
 *  Note : le test de contrainte '!=' implémenté ici exploite la particularité que "NaN === NaN //false"
 */ 
  class Sudoku extends ConstraintSatisfactionProblem {

    /** Constructeur. */
      constructor() {
        //Héritage
          super() 

        //Initalise les données propres au sudoku
          this.init()
          this.compute_domains()
          this.compute_constraints()
      }

    /** 
     * Initialise le Sudoku.
     * Des variables déjà initialisées peuvent être passées en paramètre.
     */
      init(variables = {}) {
        //Nettoyage des variables
          for (let i = 0; i < 9; i++)
              for (let j = 0; j < 9; j++)
                this.variables.set(`${i}:${j}`, NaN)

        //Application des valeurs initiales pour chaque variable passée en paramètre
          for (let variable in variables)
            this.variables.set(variable, variables[variable])

        //Mise à jour des domaines de valeurs de chaque variable
          this.compute_domains()
      }

    /** 
     * Méthode permettant de générer les contraintes binaires du Sudoku. 
     */
      compute_constraints() {
        //Création des contraintes sur les lignes
          for (let y = 0; y < 9; y++)
            for (let i = 0; i < 9; i++)
              for (let j = 0; j < 9; j++)
                if (!(i === j))
                  this.constraints.push({a:`${i}:${y}`, b:`${j}:${y}`, op:"!="})

        //Création des contraintes sur les colonnes
          for (let x = 0; x < 9; x++)
            for (let i = 0; i < 9; i++)
              for (let j = 0; j < 9; j++)
                if (!(i === j))
                  this.constraints.push({a:`${x}:${i}`, b:`${x}:${j}`, op:"!="})

        //Création des contraintes par région
          for (let x = 0; x < 9; x+=3)
            for (let y = 0; y < 9; y+=3)
              for (let i = 0; i < 9; i++)
                for (let j = 0; j < 9; j++) {
                  let xa = x + (i%3), ya = y + ~~(i/3), xb = x + (j%3), yb = y + ~~(j/3)
                  if (!((xa === xb)&&(ya === yb)))
                      this.constraints.push({a:`${xa}:${ya}`, b:`${xb}:${yb}`, op:"!="})
                }

        //Génération de la carte de contraintes et du voisinage
          this.compute_constraints_map()
          this.compute_neighborhoods()
      }

    /** 
     * Initialise les domaines de valeurs de chaque variable. 
     */
      compute_domains() {
        //Liste des valeurs possibles
          let values = []
          for (let i = 1; i <= 9; i++) values.push(i)

        //Redéfinition des domaines de valeurs 
          this.domains.clear()
          this.variables.forEach((value, variable) => this.domains.set(variable, new Set(isNaN(value) ? values : [])))
      }

    /** 
     * Retourne le voisinage d'une variable.
     */
      neighbors(variable) {
        return this.neighborhoods.get(variable)
      }
   
  }

  