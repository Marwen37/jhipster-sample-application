import { Moment } from 'moment';

export interface IProduit {
  id?: number;
  code?: string;
  description?: string;
  prix?: number;
  dateCreation?: Moment;
  categorieId?: number;
}

export class Produit implements IProduit {
  constructor(
    public id?: number,
    public code?: string,
    public description?: string,
    public prix?: number,
    public dateCreation?: Moment,
    public categorieId?: number
  ) {}
}
