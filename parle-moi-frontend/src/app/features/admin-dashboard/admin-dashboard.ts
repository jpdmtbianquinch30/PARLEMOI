import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {
  AdminService, StatsApi, ConversationAdminApi, EcoutantAdminApi, ServiceAdminApi, FormuleAdminApi
} from '../../core/admin';
import { AuthService } from '../../core/auth';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard implements OnInit {
  private adminService = inject(AdminService);
  private authService = inject(AuthService);
  private router = inject(Router);

  onglet = signal<'apercu' | 'ecoutants' | 'catalogue'>('apercu');

  stats = signal<StatsApi | null>(null);
  conversations = signal<ConversationAdminApi[]>([]);
  ecoutants = signal<EcoutantAdminApi[]>([]);
  catalogue = signal<ServiceAdminApi[]>([]);

  chargement = signal(true);
  erreur = signal(false);

  // --- Creation ecoutant ---
  afficherFormulaireCreation = signal(false);
  nouvelEmail = '';
  nouveauMotDePasse = '';
  nouveauNom = '';
  creationEnCours = signal(false);
  erreurCreation = signal('');

  // --- Creation formule ---
  afficherFormulaireFormule = signal<string | null>(null); // serviceId cible, ou null si ferme
  formuleNom = '';
  formuleDescription = '';
  formuleDuree: number | null = null;
  formulePrix: number | null = null;
  formuleCreationEnCours = signal(false);
  erreurFormule = signal('');

  // --- Edition formule ---
  formuleEnEdition = signal<string | null>(null); // id de la formule en cours d'edition

  ngOnInit(): void {
    this.chargerApercu();
    this.chargerEcoutants();
    this.chargerCatalogue();
  }

  changerOnglet(onglet: 'apercu' | 'ecoutants' | 'catalogue'): void {
    this.onglet.set(onglet);
  }

  private chargerApercu(): void {
    this.adminService.stats().subscribe({ next: (s) => this.stats.set(s), error: () => this.erreur.set(true) });
    this.adminService.conversations().subscribe({
      next: (c) => { this.conversations.set(c); this.chargement.set(false); },
      error: () => { this.erreur.set(true); this.chargement.set(false); }
    });
  }

  private chargerEcoutants(): void {
    this.adminService.ecoutants().subscribe({ next: (e) => this.ecoutants.set(e) });
  }

  private chargerCatalogue(): void {
    this.adminService.catalogue().subscribe({ next: (c) => this.catalogue.set(c) });
  }

  ouvrirFormulaireCreation(): void {
    this.afficherFormulaireCreation.set(true);
    this.erreurCreation.set('');
  }

  annulerCreation(): void {
    this.afficherFormulaireCreation.set(false);
    this.nouvelEmail = '';
    this.nouveauMotDePasse = '';
    this.nouveauNom = '';
  }

  creerEcoutant(): void {
    if (!this.nouvelEmail || !this.nouveauMotDePasse || !this.nouveauNom) return;

    this.creationEnCours.set(true);
    this.erreurCreation.set('');

    this.adminService.creerEcoutant({
      email: this.nouvelEmail, motDePasse: this.nouveauMotDePasse, nom: this.nouveauNom
    }).subscribe({
      next: () => { this.creationEnCours.set(false); this.annulerCreation(); this.chargerEcoutants(); },
      error: (err) => {
        this.creationEnCours.set(false);
        this.erreurCreation.set(err.error?.message || 'Une erreur est survenue.');
      }
    });
  }

  basculerActif(ecoutant: EcoutantAdminApi): void {
    this.adminService.modifierEcoutant(ecoutant.id, ecoutant.nom, !ecoutant.actif).subscribe({
      next: () => this.chargerEcoutants()
    });
  }

  ouvrirFormulaireFormule(serviceId: string): void {
    this.afficherFormulaireFormule.set(serviceId);
    this.erreurFormule.set('');
  }

  annulerFormule(): void {
    this.afficherFormulaireFormule.set(null);
    this.formuleNom = '';
    this.formuleDescription = '';
    this.formuleDuree = null;
    this.formulePrix = null;
  }

  creerFormule(): void {
    const serviceId = this.afficherFormulaireFormule();
    if (!serviceId || !this.formuleNom || !this.formuleDuree || !this.formulePrix) return;

    this.formuleCreationEnCours.set(true);
    this.erreurFormule.set('');

    this.adminService.creerFormule(serviceId, {
      nom: this.formuleNom,
      description: this.formuleDescription,
      dureeMinutes: this.formuleDuree,
      prix: this.formulePrix
    }).subscribe({
      next: () => { this.formuleCreationEnCours.set(false); this.annulerFormule(); this.chargerCatalogue(); },
      error: (err) => {
        this.formuleCreationEnCours.set(false);
        this.erreurFormule.set(err.error?.message || 'Une erreur est survenue.');
      }
    });
  }

  ouvrirEdition(formule: FormuleAdminApi): void {
    this.formuleEnEdition.set(formule.id);
    this.formuleNom = formule.nom;
    this.formuleDescription = formule.description || '';
    this.formuleDuree = formule.dureeMinutes;
    this.formulePrix = formule.prix;
  }

  annulerEdition(): void {
    this.formuleEnEdition.set(null);
  }

  enregistrerEdition(actif: boolean): void {
    const id = this.formuleEnEdition();
    if (!id || !this.formuleNom || !this.formuleDuree || !this.formulePrix) return;

    this.adminService.modifierFormule(id, {
      nom: this.formuleNom,
      description: this.formuleDescription,
      dureeMinutes: this.formuleDuree,
      prix: this.formulePrix,
      actif
    }).subscribe({
      next: () => { this.formuleEnEdition.set(null); this.chargerCatalogue(); }
    });
  }

  // Bascule actif/inactif SANS toucher aux autres champs - utilise les valeurs reelles
  // de la formule cliquee, pas les champs de saisie partages avec le formulaire d'edition
  // (qui peuvent contenir les valeurs d'une AUTRE formule en cours d'edition).
  basculerActifFormule(formule: FormuleAdminApi): void {
    this.adminService.modifierFormule(formule.id, {
      nom: formule.nom,
      description: formule.description ?? '',
      dureeMinutes: formule.dureeMinutes,
      prix: formule.prix,
      actif: !formule.actif
    }).subscribe({
      next: () => this.chargerCatalogue()
    });
  }

  deconnecter(): void {
    this.authService.deconnecter();
    this.router.navigate(['/admin/login']);
  }
}