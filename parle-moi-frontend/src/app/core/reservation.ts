import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ReservationRequest {
  formuleId: string;
  dateReservation: string;
  heureReservation: string;
  sujetOptionnel?: string;
}

export interface ReservationResponse {
  code: string;
  formuleNom: string;
  dateReservation: string;
  heureReservation: string;
  statut: string;
}

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/reservations`;

  creer(payload: ReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(this.baseUrl, payload);
  }

  trouverParCode(code: string): Observable<ReservationResponse> {
    return this.http.get<ReservationResponse>(`${this.baseUrl}/${code}`);
  }

  annuler(code: string): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.baseUrl}/${code}/annuler`, {});
  }
}