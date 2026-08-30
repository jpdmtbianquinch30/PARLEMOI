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

  connecter(
    code: string,
    onEvenement: (evenement: EvenementChat) => void,
    onEvenementAppel?: (evenement: EvenementAppel) => void,
    jwt?: string
  ): void {
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

        // Abonnement appel fait ICI, dans le meme callback onConnect - c'est le seul
        // moment ou le client est garanti connecte. L'appeler juste apres connecter()
        // etait une race condition : le STOMP client peut ne pas encore etre pret.
        if (onEvenementAppel) {
          this.client!.subscribe(`/topic/conversations/${code}/appel`, (frame: IMessage) => {
            onEvenementAppel(JSON.parse(frame.body) as EvenementAppel);
          });
        }
      },
      onDisconnect: () => this.connecte.set(false),
      onStompError: () => this.connecte.set(false)
    });

    this.client.activate();
  }

    envoyer(code: string, contenu: string, fichierId?: string | null): void {
    if (!this.client || !this.client.connected) {
      return;
    }
    this.client.publish({
      destination: `/app/conversations/${code}/envoyer`,
      body: JSON.stringify({ contenu, fichierId: fichierId ?? null })
    });
  }

  envoyerSignalAppel(code: string, type: string, contenu: string | null): void {
    if (!this.client || !this.client.connected) {
      return;
    }
    this.client.publish({
      destination: `/app/conversations/${code}/appel/signal`,
      body: JSON.stringify({ type, contenu })
    });
  }

  deconnecter(): void {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }
    this.connecte.set(false);
  }

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
}