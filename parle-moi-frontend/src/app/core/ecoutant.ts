import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { DureeRetention } from './conversation';



export interface EcoutantProfilApi {
  id: string;
  nom: string;
  email: string;
  enLigne: boolean;
  horaireDebut: string | null;
  horaireFin: string | null;
  dureeRetentionMessages: DureeRetention;
}

export interface ConversationEcoutantApi {
  code: string;
  statut: 'EN_ATTENTE' | 'PROGRAMMEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE' | 'EXPIREE';
  positionFileAttente: number | null;
  sujetOptionnel: string | null;
  forfaitActif: boolean;
  formuleNom: string | null;
  forfaitExpireLe: string | null;
  dateProgrammee: string | null;
  heureProgrammee: string | null;
  creeLe: string;
}

@Injectable({ providedIn: 'root' })
export class EcoutantService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/ecoutant`;

  moi(): Observable<EcoutantProfilApi> {
    return this.http.get<EcoutantProfilApi>(`${this.baseUrl}/moi`);
  }

  mettreAJourStatutEnLigne(enLigne: boolean): Observable<EcoutantProfilApi> {
    return this.http.put<EcoutantProfilApi>(`${this.baseUrl}/statut-en-ligne`, { enLigne });
  }

  mettreAJourHoraires(horaireDebut: string, horaireFin: string): Observable<EcoutantProfilApi> {
    return this.http.put<EcoutantProfilApi>(`${this.baseUrl}/horaires`, { horaireDebut, horaireFin });
  }

  mettreAJourRetention(dureeRetentionMessages: DureeRetention): Observable<EcoutantProfilApi> {
    return this.http.put<EcoutantProfilApi>(`${this.baseUrl}/retention`, { dureeRetentionMessages });
  }

  listerDemandes(): Observable<ConversationEcoutantApi[]> {
    return this.http.get<ConversationEcoutantApi[]>(`${this.baseUrl}/demandes`);
  }

  confirmer(code: string): Observable<ConversationEcoutantApi> {
    return this.http.put<ConversationEcoutantApi>(`${this.baseUrl}/demandes/${code}/confirmer`, {});
  }

  mettreEnAttente(code: string): Observable<ConversationEcoutantApi> {
    return this.http.put<ConversationEcoutantApi>(`${this.baseUrl}/demandes/${code}/mettre-en-attente`, {});
  }

  proposerHoraire(code: string, dateProgrammee: string, heureProgrammee: string): Observable<ConversationEcoutantApi> {
    return this.http.put<ConversationEcoutantApi>(`${this.baseUrl}/demandes/${code}/proposer-horaire`, {
      dateProgrammee,
      heureProgrammee
    });
  }
}