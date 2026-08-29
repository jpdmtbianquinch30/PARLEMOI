import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export type Role = 'ECOUTANT' | 'ADMIN';

export interface LoginResponse {
  token: string;
  role: Role;
  nom: string;
}

interface SessionStockee {
  token: string;
  role: Role;
  nom: string;
}

const CLE_STOCKAGE = 'parlemoi_session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/auth`;

  session = signal<SessionStockee | null>(this.lireSessionStockee());

  login(email: string, motDePasse: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/login`, { email, motDePasse }).pipe(
      tap((reponse) => {
        const nouvelleSession: SessionStockee = { token: reponse.token, role: reponse.role, nom: reponse.nom };
        localStorage.setItem(CLE_STOCKAGE, JSON.stringify(nouvelleSession));
        this.session.set(nouvelleSession);
      })
    );
  }

  deconnecter(): void {
    localStorage.removeItem(CLE_STOCKAGE);
    this.session.set(null);
  }

  estConnecte(): boolean {
    return this.session() !== null;
  }

  aLeRole(role: Role): boolean {
    return this.session()?.role === role;
  }

  token(): string | null {
    return this.session()?.token ?? null;
  }

  private lireSessionStockee(): SessionStockee | null {
    const brut = localStorage.getItem(CLE_STOCKAGE);
    if (!brut) {
      return null;
    }
    try {
      return JSON.parse(brut) as SessionStockee;
    } catch {
      return null;
    }
  }
}