import { IProduit } from 'app/shared/model/produit.model';

export interface ICategorie {
  id?: number;
  code?: string;
  description?: string;
  produits?: IProduit[];
}

export class Categorie implements ICategorie {
  constructor(public id?: number, public code?: string, public description?: string, public produits?: IProduit[]) {}
}
