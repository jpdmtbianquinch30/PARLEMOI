import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EcoutantService, EcoutantProfilApi, ConversationEcoutantApi } from '../../core/ecoutant';
import { DureeRetention } from '../../core/conversation';
import { AuthService } from '../../core/auth';

@Component({
  selector: 'app-ecoutant-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ecoutant-dashboard.html',
  styleUrl: './ecoutant-dashboard.scss'
})
export class EcoutantDashboard implements OnInit {
  private ecoutantService = inject(EcoutantService);
  private authService = inject(AuthService);
  private router = inject(Router);

  profil = signal<EcoutantProfilApi | null>(null);
  demandes = signal<ConversationEcoutantApi[]>([]);
  chargement = signal(true);
  erreur = signal<string | null>(null);

  horaireDebutSaisi = '';
  horaireFinSaisi = '';
  retentionSaisie: DureeRetention = 'J7';

  codeEnProgrammation = signal<string | null>(null);
  dateProposee = '';
  heureProposee = '';

  ngOnInit(): void {
    this.chargerTout();
  }

  private chargerTout(): void {
    this.chargement.set(true);
    this.ecoutantService.moi().subscribe({
      next: (profil) => {
        this.profil.set(profil);
        this.horaireDebutSaisi = profil.horaireDebut ?? '';
        this.horaireFinSaisi = profil.horaireFin ?? '';
        this.retentionSaisie = profil.dureeRetentionMessages;
        this.chargerDemandes();
      },
      error: () => {
        this.chargement.set(false);
        this.erreur.set('Impossible de charger votre profil.');
      }
    });
  }

  private chargerDemandes(): void {
    this.ecoutantService.listerDemandes().subscribe({
      next: (demandes) => {
        this.demandes.set(demandes);
        this.chargement.set(false);
      },
      error: () => {
        this.chargement.set(false);
        this.erreur.set('Impossible de charger les demandes.');
      }
    });
  }

  basculerStatutEnLigne(): void {
    const enLigne = !this.profil()?.enLigne;
    this.ecoutantService.mettreAJourStatutEnLigne(enLigne).subscribe({
      next: (profil) => {
        this.profil.set(profil);
        this.chargerDemandes();
      }
    });
  }

  enregistrerHoraires(): void {
    if (!this.horaireDebutSaisi || !this.horaireFinSaisi) {
      return;
    }
    this.ecoutantService.mettreAJourHoraires(this.horaireDebutSaisi, this.horaireFinSaisi).subscribe({
      next: (profil) => this.profil.set(profil)
    });
  }

  enregistrerRetention(): void {
    this.ecoutantService.mettreAJourRetention(this.retentionSaisie).subscribe({
      next: (profil) => this.profil.set(profil)
    });
  }

  confirmer(code: string): void {
    this.ecoutantService.confirmer(code).subscribe({ next: () => this.chargerDemandes() });
  }

  mettreEnAttente(code: string): void {
    this.ecoutantService.mettreEnAttente(code).subscribe({ next: () => this.chargerDemandes() });
  }

  ouvrirProgrammation(code: string): void {
    this.codeEnProgrammation.set(code);
    this.dateProposee = '';
    this.heureProposee = '';
  }

  annulerProgrammation(): void {
    this.codeEnProgrammation.set(null);
  }

  validerProgrammation(): void {
    const code = this.codeEnProgrammation();
    if (!code || !this.dateProposee || !this.heureProposee) {
      return;
    }
    this.ecoutantService.proposerHoraire(code, this.dateProposee, this.heureProposee).subscribe({
      next: () => {
        this.codeEnProgrammation.set(null);
        this.chargerDemandes();
      }
    });
  }

  deconnecter(): void {
    this.authService.deconnecter();
    this.router.navigate(['/ecoutant/login']);
  }
}