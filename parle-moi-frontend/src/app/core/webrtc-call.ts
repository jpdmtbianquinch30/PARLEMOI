import { Injectable, signal } from '@angular/core';
import { ChatSocketService } from './chat-socket';

export type EtatAppel = 'INACTIF' | 'SONNERIE_SORTANTE' | 'SONNERIE_ENTRANTE' | 'EN_COURS' | 'TERMINE';

interface TurnCredentialsApi {
  username: string;
  credential: string;
  ttlSecondes: number;
  urls: string[];
}

@Injectable({ providedIn: 'root' })
export class WebrtcCallService {
  private peerConnection: RTCPeerConnection | null = null;
  private fluxLocal: MediaStream | null = null;
  private code = '';
  private jwt: string | undefined;

  etat = signal<EtatAppel>('INACTIF');
  dureeSecondes = signal(0);
  erreur = signal<string | null>(null);

  private minuteur: ReturnType<typeof setInterval> | null = null;
  private audioDistant: HTMLAudioElement | null = null;

  constructor(private chatSocket: ChatSocketService) {}

  // A appeler une seule fois, apres avoir recupere les credentials TURN aupres du backend
  private async creerConnexion(credentials: TurnCredentialsApi): Promise<RTCPeerConnection> {
    const iceServers: RTCIceServer[] = credentials.urls.map((url) => ({
      urls: url,
      username: credentials.username,
      credential: credentials.credential
    }));

    const pc = new RTCPeerConnection({ iceServers });

    pc.onicecandidate = (event) => {
      if (event.candidate) {
        this.chatSocket.envoyerSignalAppel(this.code, 'CANDIDAT', JSON.stringify(event.candidate));
      }
    };

    pc.ontrack = (event) => {
      this.audioDistant = new Audio();
      this.audioDistant.srcObject = event.streams[0];
      this.audioDistant.autoplay = true;
    };

    pc.onconnectionstatechange = () => {
      if (pc.connectionState === 'connected' && this.etat() !== 'EN_COURS') {
        this.etat.set('EN_COURS');
        this.demarrerMinuteur();
      }
      if (pc.connectionState === 'failed' || pc.connectionState === 'disconnected') {
        this.terminer(false);
      }
    };

    return pc;
  }

  async demarrerAppelSortant(code: string, turnCredentials: TurnCredentialsApi, jwt?: string): Promise<void> {
    this.code = code;
    this.jwt = jwt;
    this.erreur.set(null);

    try {
      this.fluxLocal = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
    } catch {
      this.erreur.set("Impossible d'accéder au microphone. Vérifiez les autorisations du navigateur.");
      return;
    }

    this.peerConnection = await this.creerConnexion(turnCredentials);
    this.fluxLocal.getTracks().forEach((track) => this.peerConnection!.addTrack(track, this.fluxLocal!));

    const offre = await this.peerConnection.createOffer();
    await this.peerConnection.setLocalDescription(offre);

    this.chatSocket.envoyerSignalAppel(code, 'DEMARRER', null);
    this.chatSocket.envoyerSignalAppel(code, 'OFFRE', JSON.stringify(offre));
    this.etat.set('SONNERIE_SORTANTE');
  }

  // Appele quand une OFFRE entrante est recue (sonnerie entrante) - ne cree pas encore le flux local,
  // ca se fait seulement quand l'utilisateur clique "Accepter" (accepterAppelEntrant)
  recevoirSonnerieEntrante(code: string, offreJson: string): void {
    this.code = code;
    this.offreEnAttente = offreJson;
    this.etat.set('SONNERIE_ENTRANTE');
  }

  private offreEnAttente: string | null = null;

  async accepterAppelEntrant(turnCredentials: TurnCredentialsApi, jwt?: string): Promise<void> {
    if (!this.offreEnAttente) {
      return;
    }
    this.jwt = jwt;
    this.erreur.set(null);

    try {
      this.fluxLocal = await navigator.mediaDevices.getUserMedia({ audio: true, video: false });
    } catch {
      this.erreur.set("Impossible d'accéder au microphone. Vérifiez les autorisations du navigateur.");
      this.chatSocket.envoyerSignalAppel(this.code, 'REFUSER', null);
      return;
    }

    this.peerConnection = await this.creerConnexion(turnCredentials);
    this.fluxLocal.getTracks().forEach((track) => this.peerConnection!.addTrack(track, this.fluxLocal!));

    const offre = JSON.parse(this.offreEnAttente) as RTCSessionDescriptionInit;
    await this.peerConnection.setRemoteDescription(offre);

    const reponse = await this.peerConnection.createAnswer();
    await this.peerConnection.setLocalDescription(reponse);

    this.chatSocket.envoyerSignalAppel(this.code, 'ACCEPTER', null);
    this.chatSocket.envoyerSignalAppel(this.code, 'REPONSE', JSON.stringify(reponse));
    this.offreEnAttente = null;
  }

  refuserAppelEntrant(): void {
    this.chatSocket.envoyerSignalAppel(this.code, 'REFUSER', null);
    this.offreEnAttente = null;
    this.etat.set('INACTIF');
  }

  async recevoirReponse(reponseJson: string): Promise<void> {
    if (!this.peerConnection) {
      return;
    }
    const reponse = JSON.parse(reponseJson) as RTCSessionDescriptionInit;
    await this.peerConnection.setRemoteDescription(reponse);
  }

  async recevoirCandidat(candidatJson: string): Promise<void> {
    if (!this.peerConnection) {
      return;
    }
    const candidat = JSON.parse(candidatJson) as RTCIceCandidateInit;
    try {
      await this.peerConnection.addIceCandidate(candidat);
    } catch {
      // Candidat arrive parfois avant la remote description - non bloquant, WebRTC en genere plusieurs
    }
  }

  raccrocher(): void {
    this.chatSocket.envoyerSignalAppel(this.code, 'RACCROCHER', null);
    this.terminer(true);
  }

    // Point d'entree unique pour dispatcher un evenement d'appel recu par WebSocket -
  // reutilise a l'identique cote utilisateur et cote ecoutante, jamais duplique.
  traiterEvenementAppel(
    code: string,
    evenement: import('./chat-socket').EvenementAppel,
    monRole: 'UTILISATEUR' | 'ECOUTANT'
  ): void {
    // Le serveur diffuse a TOUS les participants du topic, y compris l'emetteur lui-meme.
    // Sans ce filtre, l'appelant recoit son propre signal OFFRE en echo et se traite
    // lui-meme comme recevant un appel entrant - d'ou "Accepter/Refuser" affiche
    // par erreur des DEUX cotes simultanement.
    if (evenement.emetteur === monRole) {
      return;
    }

    switch (evenement.type) {
      case 'OFFRE':
        this.recevoirSonnerieEntrante(code, evenement.contenu!);
        break;
      case 'REPONSE':
        this.recevoirReponse(evenement.contenu!);
        break;
      case 'CANDIDAT':
        this.recevoirCandidat(evenement.contenu!);
        break;
      case 'REFUSER':
      case 'RACCROCHER':
        this.terminerSansSignal();
        break;
    }
  }

  // Appele quand l'AUTRE partie a raccroche ou refuse - ne renvoie pas de signal, juste nettoyage local
  terminerSansSignal(): void {
    this.terminer(false);
  }

  private terminer(envoyerSignalDejaFait: boolean): void {
    this.fluxLocal?.getTracks().forEach((track) => track.stop());
    this.fluxLocal = null;

    this.peerConnection?.close();
    this.peerConnection = null;

    if (this.audioDistant) {
      this.audioDistant.srcObject = null;
      this.audioDistant = null;
    }

    this.arreterMinuteur();
    this.etat.set('INACTIF');
    this.offreEnAttente = null;
  }

  private demarrerMinuteur(): void {
    this.dureeSecondes.set(0);
    this.minuteur = setInterval(() => this.dureeSecondes.update((v) => v + 1), 1000);
  }

  private arreterMinuteur(): void {
    if (this.minuteur) {
      clearInterval(this.minuteur);
      this.minuteur = null;
    }
    this.dureeSecondes.set(0);
  }
}