import { Component, OnInit, OnDestroy, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConversationService, ConversationApi, MessageApi, PaiementApi } from '../../core/conversation';
import { ChatSocketService, EvenementChat, EvenementAppel } from '../../core/chat-socket';
import { CatalogueService, FormuleApi } from '../../core/catalogue';
import { WebrtcCallService } from '../../core/webrtc-call';

type EtatChat = 'chargement' | 'pret' | 'paywall' | 'erreur';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chat.html',
  styleUrl: './chat.scss'
})
export class Chat implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private conversationService = inject(ConversationService);
  private catalogueService = inject(CatalogueService);
  private chatSocket = inject(ChatSocketService);
  webrtcCall = inject(WebrtcCallService);

  code = signal<string>('');
  conversation = signal<ConversationApi | null>(null);
  messagesGratuitsRestants = computed(() => {
    const conv = this.conversation();
    if (!conv || conv.forfaitActif) return null;
    const nbEnvoyesParUtilisateur = this.messages().filter(m => m.auteurType === 'UTILISATEUR').length;
    return Math.max(0, 5 - nbEnvoyesParUtilisateur);
  });
  messages = signal<MessageApi[]>([]);
  champMessage = '';
  etat = signal<EtatChat>('chargement');
  erreurMessage = signal<string | null>(null);
  banniere = signal<string | null>(null);

  formules = signal<FormuleApi[]>([]);
  formuleSelectionneeId = signal<string | null>(null);
  paiementActif = signal<PaiementApi | null>(null);
  paiementEnCours = signal(false);

  appelEnCoursDeDemarrage = signal(false);
  confirmationRaccrochage = signal(false);

  connecte = this.chatSocket.connecte;

  ngOnInit(): void {
    this.catalogueService.lister().subscribe(services => {
      this.formules.set(services.flatMap(s => s.formules));
    });

    const codeParam = this.route.snapshot.paramMap.get('code');
    if (codeParam) {
      this.chargerConversationExistante(codeParam);
    } else {
      this.demarrerNouvelleConversation();
    }
  }

  ngOnDestroy(): void {
    this.chatSocket.deconnecter();
    this.webrtcCall.raccrocher();
  }

  private demarrerNouvelleConversation(): void {
    this.conversationService.demarrer().subscribe({
      next: (conv) => this.router.navigate(['/chat', conv.code], { replaceUrl: true }),
      error: () => {
        this.etat.set('erreur');
        this.erreurMessage.set('Impossible de demarrer une conversation pour le moment. Reessayez dans un instant.');
      }
    });
  }

  private chargerConversationExistante(code: string): void {
    this.code.set(code);
    this.conversationService.consulterHistorique(code).subscribe({
      next: (historique) => {
        this.conversation.set(historique.conversation);
        this.messages.set(historique.messages);
        this.mettreAJourEtatDepuisConversation(historique.conversation);
        this.chatSocket.connecter(
          code,
          (evenement) => this.gererEvenement(evenement),
          (evenement) => this.webrtcCall.traiterEvenementAppel(code, evenement)
        );
      },
      error: () => {
        this.etat.set('erreur');
        this.erreurMessage.set('Cette conversation est introuvable ou a expire.');
      }
    });
  }

  private gererEvenement(evenement: EvenementChat): void {
    switch (evenement.categorie) {
      case 'message':
        this.messages.update(liste => [...liste, evenement.donnees]);
        break;
      case 'systeme':
        this.banniere.set(evenement.donnees.message);
        if (evenement.donnees.type === 'PAYWALL') this.etat.set('paywall');
        break;
      case 'forfait-statut':
        this.banniere.set(evenement.donnees.message);
        if (evenement.donnees.type === 'FORFAIT_TERMINE') {
          this.etat.set('paywall');
          this.conversation.update(c => (c ? { ...c, forfaitActif: false } : c));
        }
        break;
      case 'forfait-active':
        this.etat.set('pret');
        this.banniere.set(`Forfait "${evenement.donnees.formuleNom}" active !`);
        this.paiementActif.set(null);
        this.formuleSelectionneeId.set(null);
        this.conversation.update(c => c ? {
          ...c, forfaitActif: true, formuleNom: evenement.donnees.formuleNom, forfaitExpireLe: evenement.donnees.forfaitExpireLe
        } : c);
        break;
    }
  }

  private mettreAJourEtatDepuisConversation(conv: ConversationApi): void {
    const paywallActif = !conv.forfaitActif && conv.nbMessagesGratuitsRestants <= 0;
    this.etat.set(paywallActif ? 'paywall' : 'pret');
  }

  envoyerMessage(): void {
    const contenu = this.champMessage.trim();
    if (!contenu || this.etat() === 'paywall') return;
    this.chatSocket.envoyer(this.code(), contenu);
    this.champMessage = '';
  }

  choisirFormule(formuleId: string): void {
    this.formuleSelectionneeId.set(formuleId);
    this.paiementActif.set(null);
  }

  payerAvec(provider: 'WAVE' | 'ORANGE_MONEY'): void {
    const formuleId = this.formuleSelectionneeId();
    if (!formuleId) return;
    this.paiementEnCours.set(true);
    this.conversationService.initierPaiement(this.code(), formuleId, provider).subscribe({
      next: (paiement) => { this.paiementActif.set(paiement); this.paiementEnCours.set(false); },
      error: () => { this.paiementEnCours.set(false); this.banniere.set("Le paiement n'a pas pu etre initie. Reessayez."); }
    });
  }

  simulerConfirmationPaiement(): void {
    const paiement = this.paiementActif();
    if (!paiement) return;
    this.conversationService.confirmerPaiementSimule(paiement.paiementId).subscribe({
      error: () => this.banniere.set('La confirmation du paiement a echoue.')
    });
  }

  fermerBanniere(): void {
    this.banniere.set(null);
  }

  // --- Appel ---

  peutAppeler(): boolean {
    return !!this.conversation()?.forfaitActif && this.webrtcCall.etat() === 'INACTIF';
  }

  demarrerAppel(): void {
    this.appelEnCoursDeDemarrage.set(true);
    this.conversationService.turnCredentials(this.code()).subscribe({
      next: (credentials) => {
        this.appelEnCoursDeDemarrage.set(false);
        this.webrtcCall.demarrerAppelSortant(this.code(), credentials);
      },
      error: () => {
        this.appelEnCoursDeDemarrage.set(false);
        this.banniere.set("Impossible de demarrer l'appel pour le moment.");
      }
    });
  }

  accepterAppel(): void {
    this.conversationService.turnCredentials(this.code()).subscribe({
      next: (credentials) => this.webrtcCall.accepterAppelEntrant(credentials)
    });
  }

  refuserAppel(): void {
    this.webrtcCall.refuserAppelEntrant();
  }

  demanderRaccrochage(): void {
    // SCRUM-10 : avertissement clair avant de raccrocher, jamais de coupure silencieuse
    this.confirmationRaccrochage.set(true);
  }

  confirmerRaccrochage(): void {
    this.confirmationRaccrochage.set(false);
    this.webrtcCall.raccrocher();
  }

  annulerRaccrochage(): void {
    this.confirmationRaccrochage.set(false);
  }

  formaterDuree(secondes: number): string {
    const m = Math.floor(secondes / 60).toString().padStart(2, '0');
    const s = (secondes % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }
}