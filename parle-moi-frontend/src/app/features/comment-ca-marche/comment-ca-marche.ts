import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-comment-ca-marche',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './comment-ca-marche.html',
  styleUrl: './comment-ca-marche.scss'
})
export class CommentCaMarche {
  etapes = [
    {
      num: '01',
      titre: 'Vous démarrez une conversation',
      texte: "Un clic sur \"Parler maintenant\" ouvre un chat immédiatement. Aucun compte, aucun formulaire, aucune information personnelle demandée.",
      icone: 'CSMconversation.png'
    },
    {
      num: '02',
      titre: 'Un code vous est donné',
      texte: "Un code unique (ex: PM-7K4X92) apparaît dans le chat. Notez-le : c'est le seul moyen de retrouver cette conversation plus tard, pendant 1 mois.",
      icone: 'CSMcode.png'
    },
    {
      num: '03',
      titre: '5 premiers messages offerts',
      texte: "Vous échangez librement avec une écoutante, ou patientez quelques instants en file d'attente si elle est déjà en conversation.",
      icone: 'CSM5messages.png'
    },
    {
      num: '04',
      titre: 'Un forfait pour continuer',
      texte: "Une fois les messages offerts épuisés, activez un forfait directement dans le chat (Wave ou Orange Money) pour poursuivre par écrit ou passer à l'appel vocal.",
      icone: 'CSMpayer.png'
    },
    {
      num: '05',
      titre: 'Un appel, si vous le souhaitez',
      texte: "Pendant un forfait actif, vous pouvez basculer vers un appel vocal en un clic, directement depuis le chat.",
      icone: 'CSMappel.png'
    },
    {
      num: '06',
      titre: 'Votre conversation reste accessible',
      texte: "Avec votre code, retrouvez l'historique de vos échanges pendant 1 mois, depuis n'importe quel appareil.",
      icone: 'CSM1mois.png'
    }
  ];

  precisions = [
    { titre: 'Et si personne n\'est disponible ?', texte: 'Vous patientez en file d\'attente dans le chat, ou choisissez de programmer un appel à un horaire proposé par l\'écoutante.' },
    { titre: 'Mes messages sont-ils conservés ?', texte: 'Chaque écoutante configure elle-même une durée de conservation (24h, 7 jours ou 30 jours), après quoi vos messages sont supprimés automatiquement.' },
    { titre: 'Puis-je envoyer une image ou un document ?', texte: 'Oui, directement dans le chat. Chaque fichier est vérifié et stocké de façon sécurisée, jamais accessible publiquement.' }
  ];
}