import { Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../environments/environment';
import { MessageApi } from './conversation';

export interface EvenementSysteme {
  type: 'ERREUR' | 'PAYWALL' | 'PAYWALL_IMMINENT';
  message: string;
  nbMessagesGratuitsRestants: number | null;
}

export interface EvenementForfaitStatut {
  type: 'AVERTISSEMENT_FIN_FORFAIT' | 'FORFAIT_TERMINE';
  message: string;
  forfaitExpireLe: string | null;
}

export interface EvenementForfaitActive {
  type: 'FORFAIT_ACTIVE';
  formuleNom: string;
  forfaitExpireLe: string;
}

export interface EvenementAppel {
  type: string;
  contenu: string | null;
  emetteur: 'UTILISATEUR' | 'ECOUTANT';
}

export type EvenementChat =
  | { categorie: 'message'; donnees: MessageApi }
  | { categorie: 'systeme'; donnees: EvenementSysteme }
  | { categorie: 'forfait-statut'; donnees: EvenementForfaitStatut }
  | { categorie: 'forfait-active'; donnees: EvenementForfaitActive };

@Injectable({ providedIn: 'root' })
export class ChatSocketService {
  private client: Client | null = null;

  connecte = signal(false);

  connecter(code: string, onEvenement: (evenement: EvenementChat) => void, jwt?: string): void {
    this.deconnecter();

    const headers: Record<string, string> = jwt
      ? { Authorization: `Bearer ${jwt}` }
      : { conversationCode: code };

    this.client = new Client({
      webSocketFactory: () => new SockJS(environment.wsUrl),
      connectHeaders: headers,
      reconnectDelay: 3000,
      onConnect: () => {
        this.connecte.set(true);
        this.client!.subscribe(`/topic/conversations/${code}`, (frame: IMessage) => {
          this.repartir(frame, onEvenement);
        });
      },
      onDisconnect: () => this.connecte.set(false),
      onStompError: () => this.connecte.set(false)
    });

    this.client.activate();
  }

  envoyer(code: string, contenu: string): void {
    if (!this.client || !this.client.connected) {
      return;
    }
    this.client.publish({
      destination: `/app/conversations/${code}/envoyer`,
      body: JSON.stringify({ contenu })
    });
  }

  deconnecter(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connecte.set(false);
  }

  // Distingue 4 formes de payload possibles sur le meme topic :
  // - un vrai message a un champ "id"
  // - FORFAIT_ACTIVE (envoye par PaiementService) a un champ "formuleNom", pas "message"
  // - AVERTISSEMENT_FIN_FORFAIT / FORFAIT_TERMINE (ForfaitSurveillanceService) ont un champ "message"
  // - tout le reste (ERREUR/PAYWALL/PAYWALL_IMMINENT, MessageService) est un evenement systeme
  private repartir(frame: IMessage, onEvenement: (evenement: EvenementChat) => void): void {
    const donnees = JSON.parse(frame.body);

    if ('id' in donnees) {
      onEvenement({ categorie: 'message', donnees: donnees as MessageApi });
      return;
    }

    if (donnees.type === 'FORFAIT_ACTIVE') {
      onEvenement({ categorie: 'forfait-active', donnees: donnees as EvenementForfaitActive });
      return;
    }

    if (donnees.type === 'AVERTISSEMENT_FIN_FORFAIT' || donnees.type === 'FORFAIT_TERMINE') {
      onEvenement({ categorie: 'forfait-statut', donnees: donnees as EvenementForfaitStatut });
      return;
    }

    onEvenement({ categorie: 'systeme', donnees: donnees as EvenementSysteme });
  }
  envoyerSignalAppel(code: string, type: string, contenu: string | null): void {
  const client = (this as any).client as Client | null;
  if (!client || !client.connected) {
    return;
  }
  client.publish({
    destination: `/app/conversations/${code}/appel/signal`,
    body: JSON.stringify({ type, contenu })
  });
}

ecouterAppel(code: string, onEvenement: (evenement: EvenementAppel) => void): void {
  const client = (this as any).client as Client | null;
  if (!client || !client.connected) {
    return;
  }
  client.subscribe(`/topic/conversations/${code}/appel`, (frame) => {
    onEvenement(JSON.parse(frame.body) as EvenementAppel);
  });
}
}