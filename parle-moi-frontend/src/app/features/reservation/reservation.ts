import { Component, signal, computed, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ReservationService, ReservationResponse } from '../../core/reservation';
import { CatalogueService, FormuleApi } from '../../core/catalogue';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-reservation',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './reservation.html',
  styleUrl: './reservation.scss'
})
export class Reservation implements OnInit {
  private reservationService = inject(ReservationService);
  private catalogueService = inject(CatalogueService);

  formules = signal<FormuleApi[]>([]);
  chargementFormules = signal(true);
  erreurChargement = signal(false);

  etape = signal(1);
  formuleChoisie = signal<FormuleApi | null>(null);
  dateChoisie = signal('');
  heureChoisie = signal('');
  sujetOptionnel = signal('');

  creneauxDisponibles = signal([
    '09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00'
  ]);

  sujets = ['Je prefere ne pas preciser', 'Besoin de parler', 'Rupture', 'Relation', 'Motivation', 'Famille', 'Autre'];

  enCours = signal(false);
  erreur = signal('');
  confirmation = signal<ReservationResponse | null>(null);

  peutContinuer = computed(() => {
    if (this.etape() === 1) return !!this.formuleChoisie();
    if (this.etape() === 2) return !!this.dateChoisie();
    if (this.etape() === 3) return !!this.heureChoisie();
    return true;
  });

  ngOnInit() {
    this.catalogueService.lister().subscribe({
      next: (services) => {
        // On aplatit toutes les formules de tous les services actifs en une seule liste
        const toutesFormules = services.flatMap(s => s.formules);
        this.formules.set(toutesFormules);
        this.chargementFormules.set(false);
      },
      error: () => {
        this.erreurChargement.set(true);
        this.chargementFormules.set(false);
      }
    });
  }

  choisirFormule(f: FormuleApi) {
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
        this.erreur.set(err.error?.message || 'Une erreur est survenue. Reessaie.');
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