export interface IClient {
  id?: number;
  nom?: string;
  prenom?: string;
  tel?: string;
}

export class Client implements IClient {
  constructor(public id?: number, public nom?: string, public prenom?: string, public tel?: string) {}
}
