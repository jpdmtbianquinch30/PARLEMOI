import { Component, signal } from '@angular/core';

interface FaqItem {
  question: string;
  reponse: string;
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [],
  templateUrl: './faq.html',
  styleUrl: './faq.scss'
})
export class Faq {
  items = signal<FaqItem[]>([
    { question: 'Dois-je créer un compte ?', reponse: 'Non. La réservation est accessible sans compte.' },
    { question: 'Dois-je donner mon nom ?', reponse: 'Non.' },
    { question: 'Dois-je donner mon numéro de téléphone ?', reponse: "Non, dans le cadre normal du processus de réservation." },
    { question: 'Dois-je donner mon email ?', reponse: 'Non.' },
    { question: 'Puis-je utiliser un pseudonyme ?', reponse: 'Non. Aucun nom ou pseudonyme n\'est nécessaire.' },
    { question: 'Comment retrouver ma réservation ?', reponse: 'Avec votre code de réservation unique, via la page "Suivre ma demande".' },
    { question: 'Que se passe-t-il si je perds mon code ?', reponse: "La réservation étant totalement anonyme, il n'est pas possible de la récupérer sans ce code." },
    { question: 'Puis-je annuler ?', reponse: 'Oui, directement depuis la page "Suivre ma demande", avec votre code.' },
    { question: 'Est-ce un service médical ?', reponse: "PARLE-MOI est un espace d'écoute et de conversation. Il ne remplace pas un professionnel de santé ou un service d'urgence." }
  ]);

  ouvert = signal<number | null>(null);

  toggle(index: number) {
    this.ouvert.update(v => (v === index ? null : index));
  }
}