import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface FormuleApi {
  id: string;
  nom: string;
  description: string;
  dureeMinutes: number;
  prix: number;
  devise: string;
}

export interface ServiceApi {
  id: string;
  nom: string;
  description: string;
  formules: FormuleApi[];
}

@Injectable({ providedIn: 'root' })
export class CatalogueService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiUrl}/services`;

  lister(): Observable<ServiceApi[]> {
    return this.http.get<ServiceApi[]>(this.baseUrl);
  }
}