import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface FormuleAdminApi {
  id: string;
  nom: string;
  description: string | null;
  dureeMinutes: number;
  prix: number;
  devise: string;
  actif: boolean;
}

export interface ServiceAdminApi {
  id: string;
  nom: string;
  description: string | null;
  actif: boolean;
  formules: FormuleAdminApi[];
}

export interface CreerFormulePayload {
  nom: string;
  description: string;
  dureeMinutes: number;
  prix: number;
}

export interface ModifierFormulePayload {
  nom: string;
  description: string;
  dureeMinutes: number;
  prix: number;
  actif: boolean;
}

export interface EcoutantAdminApi {
  id: string;
  email: string;
  nom: string;
  actif: boolean;
  enLigne: boolean;
  horaireDebut: string | null;
  horaireFin: string | null;
  dureeRetentionMessages: string;
}

export interface CreerEcoutantPayload {
  email: string;
  motDePasse: string;
  nom: string;
}

export interface StatsApi {
  conversationsAujourdhui: number;
  conversationsSemaine: number;
  conversationsEnAttente: number;
  conversationsTerminees: number;
  ecoutantsEnLigne: number;
  paiementsReussis: number;
  revenuTotal: number;
}

export interface ConversationAdminApi {
  code: string;
  statut: string;
  ecoutantNom: string;
  formuleNom: string | null;
  forfaitActif: boolean;
  dateProgrammee: string | null;
  heureProgrammee: string | null;
  creeLe: string;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/admin`;

  stats(): Observable<StatsApi> {
    return this.http.get<StatsApi>(`${this.baseUrl}/stats`);
  }

  conversations(): Observable<ConversationAdminApi[]> {
    return this.http.get<ConversationAdminApi[]>(`${this.baseUrl}/conversations`);
  }
    ecoutants(): Observable<EcoutantAdminApi[]> {
    return this.http.get<EcoutantAdminApi[]>(`${this.baseUrl}/ecoutants`);
  }

  creerEcoutant(payload: CreerEcoutantPayload): Observable<EcoutantAdminApi> {
    return this.http.post<EcoutantAdminApi>(`${this.baseUrl}/ecoutants`, payload);
  }

  modifierEcoutant(id: string, nom: string, actif: boolean): Observable<EcoutantAdminApi> {
    return this.http.put<EcoutantAdminApi>(`${this.baseUrl}/ecoutants/${id}`, { nom, actif });
  }

    catalogue(): Observable<ServiceAdminApi[]> {
    return this.http.get<ServiceAdminApi[]>(`${this.baseUrl}/catalogue`);
  }

  creerFormule(serviceId: string, payload: CreerFormulePayload): Observable<FormuleAdminApi> {
    return this.http.post<FormuleAdminApi>(`${this.baseUrl}/services/${serviceId}/formules`, payload);
  }

  modifierFormule(id: string, payload: ModifierFormulePayload): Observable<FormuleAdminApi> {
    return this.http.put<FormuleAdminApi>(`${this.baseUrl}/formules/${id}`, payload);
  }
}