import { Component, signal, computed, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReservationService, ReservationRequest, ReservationResponse } from '../../core/reservation';
interface Formule {
  id: string;
  nom: string;
  duree: string;
  prix: string;
}

@Component({
  selector: 'app-reservation',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './reservation.html',
  styleUrl: './reservation.scss'
})
export class Reservation {
  private reservationService = inject(ReservationService);

  // ⚠️ Remplace ces IDs par les vrais UUID générés par ton DataSeeder
  formules = signal<Formule[]>([
  { id: 'fbfe6265-cab6-4ba8-94d7-129beeec37d0', nom: 'Découverte', duree: '30 min', prix: '10 000 FCFA' },
  { id: 'b2cfd422-f92a-4a76-a6e2-31b661db57c2', nom: 'Confort', duree: '1 h', prix: '15 000 FCFA' },
  { id: '848ff442-0eed-43b5-b975-d1a34d7c8014', nom: 'Premium', duree: '2 h', prix: '25 000 FCFA' },
]);
  etape = signal(1);
  formuleChoisie = signal<Formule | null>(null);
  dateChoisie = signal('');
  heureChoisie = signal('');
  sujetOptionnel = signal('');

  creneauxDisponibles = signal([
    '09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00'
  ]);

  sujets = ['Je préfère ne pas préciser', 'Besoin de parler', 'Rupture', 'Relation', 'Motivation', 'Famille', 'Autre'];

  enCours = signal(false);
  erreur = signal('');
  confirmation = signal<ReservationResponse | null>(null);

  peutContinuer = computed(() => {
    if (this.etape() === 1) return !!this.formuleChoisie();
    if (this.etape() === 2) return !!this.dateChoisie();
    if (this.etape() === 3) return !!this.heureChoisie();
    return true;
  });

  choisirFormule(f: Formule) {
    this.formuleChoisie.set(f);
  }

  choisirHeure(h: string) {
    this.heureChoisie.set(h);
  }

  suivant() {
    this.etape.update(e => e + 1);
  }

  precedent() {
    this.etape.update(e => Math.max(1, e - 1));
  }

  confirmer() {
    const formule = this.formuleChoisie();
    if (!formule) return;

    this.enCours.set(true);
    this.erreur.set('');

    this.reservationService.creer({
      formuleId: formule.id,
      dateReservation: this.dateChoisie(),
      heureReservation: `${this.heureChoisie()}:00`,
      sujetOptionnel: this.sujetOptionnel() || undefined
    }).subscribe({
      next: (res) => {
        this.confirmation.set(res);
        this.enCours.set(false);
      },
      error: (err) => {
        this.erreur.set(err.error?.message || 'Une erreur est survenue. Réessaie.');
        this.enCours.set(false);
      }
    });
  }

  copierCode() {
    const code = this.confirmation()?.code;
    if (code) {
      navigator.clipboard.writeText(code);
    }
  }
}