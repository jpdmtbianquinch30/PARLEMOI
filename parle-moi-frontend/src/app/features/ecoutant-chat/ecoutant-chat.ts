import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ConversationService, ConversationApi, MessageApi } from '../../core/conversation';
import { ChatSocketService, EvenementChat } from '../../core/chat-socket';
import { WebrtcCallService } from '../../core/webrtc-call';
import { AuthService } from '../../core/auth';

@Component({
  selector: 'app-ecoutant-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ecoutant-chat.html',
  styleUrl: './ecoutant-chat.scss'
})
export class EcoutantChat implements OnInit, OnDestroy {
  private route = inject(ActivatedRoute);
  private conversationService = inject(ConversationService);
  private chatSocket = inject(ChatSocketService);
  private authService = inject(AuthService);
  webrtcCall = inject(WebrtcCallService);

  code = signal<string>('');
  conversation = signal<ConversationApi | null>(null);
  messages = signal<MessageApi[]>([]);
  champMessage = '';
  chargement = signal(true);
  erreur = signal<string | null>(null);
  banniere = signal<string | null>(null);

  appelEnCoursDeDemarrage = signal(false);
  confirmationRaccrochage = signal(false);

  ngOnInit(): void {
    const code = this.route.snapshot.paramMap.get('code');
    if (!code) return;
    this.code.set(code);

    this.conversationService.consulterHistorique(code).subscribe({
      next: (historique) => {
        this.conversation.set(historique.conversation);
        this.messages.set(historique.messages);
        this.chargement.set(false);

        this.chatSocket.connecter(
          code,
          (evenement) => this.gererEvenement(evenement),
          (evenement) => this.webrtcCall.traiterEvenementAppel(code, evenement),
          this.authService.token() ?? undefined
        );
      },
      error: () => {
        this.chargement.set(false);
        this.erreur.set('Conversation introuvable ou expiree.');
      }
    });
  }

  ngOnDestroy(): void {
    this.chatSocket.deconnecter();
    this.webrtcCall.raccrocher();
  }

  private gererEvenement(evenement: EvenementChat): void {
    switch (evenement.categorie) {
      case 'message':
        this.messages.update(liste => [...liste, evenement.donnees]);
        break;
      case 'systeme':
        this.banniere.set(evenement.donnees.message);
        break;
      case 'forfait-statut':
        this.banniere.set(evenement.donnees.message);
        if (evenement.donnees.type === 'FORFAIT_TERMINE') {
          this.conversation.update(c => (c ? { ...c, forfaitActif: false } : c));
        }
        break;
      case 'forfait-active':
        this.banniere.set(`Forfait "${evenement.donnees.formuleNom}" active par l'utilisateur.`);
        this.conversation.update(c => c ? {
          ...c, forfaitActif: true, formuleNom: evenement.donnees.formuleNom, forfaitExpireLe: evenement.donnees.forfaitExpireLe
        } : c);
        break;
    }
  }

  envoyerMessage(): void {
    const contenu = this.champMessage.trim();
    if (!contenu) return;
    this.chatSocket.envoyer(this.code(), contenu);
    this.champMessage = '';
  }

  fermerBanniere(): void {
    this.banniere.set(null);
  }

  // --- Appel ---

  demarrerAppel(): void {
    this.appelEnCoursDeDemarrage.set(true);
    this.conversationService.turnCredentials(this.code()).subscribe({
      next: (credentials) => {
        this.appelEnCoursDeDemarrage.set(false);
        this.webrtcCall.demarrerAppelSortant(this.code(), credentials, this.authService.token() ?? undefined);
      },
      error: () => {
        this.appelEnCoursDeDemarrage.set(false);
        this.banniere.set("Impossible de demarrer l'appel pour le moment.");
      }
    });
  }

  accepterAppel(): void {
    this.conversationService.turnCredentials(this.code()).subscribe({
      next: (credentials) => this.webrtcCall.accepterAppelEntrant(credentials, this.authService.token() ?? undefined)
    });
  }

  refuserAppel(): void {
    this.webrtcCall.refuserAppelEntrant();
  }

  demanderRaccrochage(): void {
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