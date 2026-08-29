import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type DureeRetention = 'H24' | 'J7' | 'J30';
export type StatutConversation = 'EN_ATTENTE' | 'PROGRAMMEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE' | 'EXPIREE';

export interface ConversationApi {
  code: string;
  statut: StatutConversation;
  positionFileAttente: number | null;
  nbMessagesGratuitsUtilises: number;
  nbMessagesGratuitsRestants: number;
  forfaitActif: boolean;
  formuleNom: string | null;
  forfaitExpireLe: string | null;
  expireLe: string | null;
}

export interface MessageApi {
  id: string;
  auteurType: 'UTILISATEUR' | 'ECOUTANT';
  contenu: string;
  envoyeLe: string;
}

export interface HistoriqueConversationApi {
  conversation: ConversationApi;
  messages: MessageApi[];
}

export interface PaiementApi {
  paiementId: string;
  provider: string;
  statut: 'EN_ATTENTE' | 'REUSSI' | 'ECHOUE' | 'REMBOURSE';
  urlPaiement: string | null;
  montant: number;
  devise: string;
}

@Injectable({ providedIn: 'root' })
export class ConversationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/conversations`;

  demarrer(sujetOptionnel?: string): Observable<ConversationApi> {
    return this.http.post<ConversationApi>(this.baseUrl, { sujetOptionnel: sujetOptionnel ?? null });
  }

  trouverParCode(code: string): Observable<ConversationApi> {
    return this.http.get<ConversationApi>(`${this.baseUrl}/${code}`);
  }

  consulterHistorique(code: string): Observable<HistoriqueConversationApi> {
    return this.http.get<HistoriqueConversationApi>(`${this.baseUrl}/${code}/historique`);
  }

  initierPaiement(code: string, formuleId: string, provider: string): Observable<PaiementApi> {
    return this.http.post<PaiementApi>(`${this.baseUrl}/${code}/paiements`, { formuleId, provider });
  }

  confirmerPaiementSimule(paiementId: string): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/paiements/${paiementId}/simulateur/confirmer`, {});
  }
}