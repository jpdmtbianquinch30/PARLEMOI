import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

interface FaqItem {
  question: string;
  reponse: string;
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './faq.html',
  styleUrl: './faq.scss'
})
export class Faq {
  items = signal<FaqItem[]>([
    { question: 'Dois-je créer un compte ?', reponse: "Non. Le chat démarre instantanément, sans inscription ni mot de passe." },
    { question: 'Dois-je donner mon nom, mon numéro ou mon email ?', reponse: "Non, jamais. Aucune information personnelle n'est demandée à aucun moment." },
    { question: 'Comment retrouver ma conversation plus tard ?', reponse: "Grâce au code unique donné au tout début du chat (ex: PM-7K4X92), depuis la page \"Suivre ma conversation\"." },
    { question: 'Que se passe-t-il si je perds mon code ?', reponse: "La conversation étant totalement anonyme, il n'est malheureusement pas possible de la récupérer sans ce code. Conservez-le précieusement." },
    { question: 'Les 5 premiers messages sont-ils vraiment gratuits ?', reponse: "Oui, sans aucune condition. Un forfait n'est nécessaire que si vous souhaitez continuer au-delà." },
    { question: 'Comment activer un forfait ?', reponse: "Directement dans le chat, via Wave ou Orange Money, une fois les messages offerts épuisés." },
    { question: 'Puis-je passer un appel vocal ?', reponse: "Oui, pendant un forfait actif, un bouton d'appel apparaît dans le chat." },
    { question: 'Et si l\'écoutante est déjà occupée ?', reponse: "Vous patientez en file d'attente dans le chat, ou pouvez proposer de programmer un appel à un horaire ultérieur." },
    { question: 'Puis-je envoyer une photo ou un document ?', reponse: "Oui, directement dans le chat. Chaque fichier est vérifié avant stockage et n'est jamais accessible publiquement." },
    { question: 'Mes messages sont-ils conservés indéfiniment ?', reponse: "Non. Chaque écoutante configure une durée de conservation (24h, 7 jours ou 30 jours) ; passé ce délai, vos messages sont supprimés automatiquement." },
    { question: 'Puis-je annuler ou arrêter une conversation ?', reponse: "Oui, à tout moment, simplement en fermant le chat. Rien ne vous engage." },
    { question: 'Est-ce un service médical ?', reponse: "Non. PARLE-MOI est un espace d'écoute et de conversation bienveillante. Il ne remplace pas un professionnel de santé ni un service d'urgence." }
  ]);

  ouvert = signal<number | null>(null);

  toggle(index: number) {
    this.ouvert.update(v => (v === index ? null : index));
  }
}