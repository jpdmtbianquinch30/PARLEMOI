import { Component, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReservationService, ReservationResponse } from '../../core/reservation';

@Component({
  selector: 'app-suivre-reservation',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './suivre-reservation.html',
  styleUrl: './suivre-reservation.scss'
})
export class SuivreReservation {
  private reservationService = inject(ReservationService);

  code = signal('');
  resultat = signal<ReservationResponse | null>(null);
  erreur = signal('');
  enCours = signal(false);
  annulationEnCours = signal(false);
  confirmationAnnulation = signal(false);

  rechercher() {
    const codeSaisi = this.code().trim().toUpperCase();
    if (!codeSaisi) return;

    this.enCours.set(true);
    this.erreur.set('');
    this.resultat.set(null);

    this.reservationService.trouverParCode(codeSaisi).subscribe({
      next: (res) => {
        this.resultat.set(res);
        this.enCours.set(false);
      },
      error: () => {
        this.erreur.set('Aucune réservation trouvée avec ce code.');
        this.enCours.set(false);
      }
    });
  }

  demanderAnnulation() {
    this.confirmationAnnulation.set(true);
  }

  annulerConfirme() {
    const codeSaisi = this.code().trim().toUpperCase();
    this.annulationEnCours.set(true);

    this.reservationService.annuler(codeSaisi).subscribe({
      next: (res) => {
        this.resultat.set(res);
        this.annulationEnCours.set(false);
        this.confirmationAnnulation.set(false);
      },
      error: () => {
        this.erreur.set("L'annulation a échoué. Réessaie.");
        this.annulationEnCours.set(false);
        this.confirmationAnnulation.set(false);
      }
    });
  }

  annulerNon() {
    this.confirmationAnnulation.set(false);
  }
}