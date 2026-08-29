import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ConversationService, ConversationApi, MessageApi, PaiementApi } from '../../core/conversation';
import { ChatSocketService, EvenementChat } from '../../core/chat-socket';
import { CatalogueService, FormuleApi } from '../../core/catalogue';

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

  code = signal<string>('');
  conversation = signal<ConversationApi | null>(null);
  messages = signal<MessageApi[]>([]);
  champMessage = '';
  etat = signal<EtatChat>('chargement');
  erreurMessage = signal<string | null>(null);
  banniere = signal<string | null>(null);

  formules = signal<FormuleApi[]>([]);
  formuleSelectionneeId = signal<string | null>(null);
  paiementActif = signal<PaiementApi | null>(null);
  paiementEnCours = signal(false);

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
        this.chatSocket.connecter(code, (evenement) => this.gererEvenement(evenement));
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
        if (evenement.donnees.type === 'PAYWALL') {
          this.etat.set('paywall');
        }
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
          ...c,
          forfaitActif: true,
          formuleNom: evenement.donnees.formuleNom,
          forfaitExpireLe: evenement.donnees.forfaitExpireLe
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
    if (!contenu || this.etat() === 'paywall') {
      return;
    }
    this.chatSocket.envoyer(this.code(), contenu);
    this.champMessage = '';
  }

  choisirFormule(formuleId: string): void {
    this.formuleSelectionneeId.set(formuleId);
    this.paiementActif.set(null);
  }

  payerAvec(provider: 'WAVE' | 'ORANGE_MONEY'): void {
    const formuleId = this.formuleSelectionneeId();
    if (!formuleId) {
      return;
    }
    this.paiementEnCours.set(true);
    this.conversationService.initierPaiement(this.code(), formuleId, provider).subscribe({
      next: (paiement) => {
        this.paiementActif.set(paiement);
        this.paiementEnCours.set(false);
      },
      error: () => {
        this.paiementEnCours.set(false);
        this.banniere.set("Le paiement n'a pas pu etre initie. Reessayez.");
      }
    });
  }

  simulerConfirmationPaiement(): void {
    const paiement = this.paiementActif();
    if (!paiement) {
      return;
    }
    // La mise a jour reelle de l'etat (forfaitActif) arrive via l'evenement WebSocket
    // FORFAIT_ACTIVE, pas via cette reponse HTTP - on ne modifie rien manuellement ici.
    this.conversationService.confirmerPaiementSimule(paiement.paiementId).subscribe({
      error: () => this.banniere.set('La confirmation du paiement a echoue.')
    });
  }

  fermerBanniere(): void {
    this.banniere.set(null);
  }
}