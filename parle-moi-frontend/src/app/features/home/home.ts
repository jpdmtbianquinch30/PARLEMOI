import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home {
  badgesConfiance = ['100% anonyme', 'Sans jugement', 'Confidentiel'];

  stats = [
    { valeur: '100%', label: 'Anonyme' },
    { valeur: '5', label: 'Messages offerts' },
    { valeur: '0', label: 'Jugement' }
  ];

  atouts = [
    { titre: 'Écoute immédiate', texte: 'Un chat démarre en un clic, sans compte ni formulaire.', icone: 'ecoute.png' },
    { titre: 'Bienveillance', texte: 'Un échange humain dans un environnement respectueux.', icone: 'hands.png' },
    { titre: 'Confidentialité', texte: 'Aucune donnée personnelle demandée, jamais.', icone: 'lock.png' },
    { titre: 'Sans jugement', texte: 'Vous pouvez parler librement de ce que vous ressentez.', icone: 'heart.png' }
  ];

  etapes = [
    { num: '01', titre: 'Vous parlez', texte: 'Un chat démarre instantanément, un code unique vous est donné.' },
    { num: '02', titre: '5 messages offerts', texte: "Le temps de voir si l'échange vous convient." },
    { num: '03', titre: 'Un forfait, si besoin', texte: 'Pour continuer par écrit ou passer à l\'appel vocal.' },
    { num: '04', titre: 'Votre code, 1 mois', texte: 'Retrouvez votre conversation à tout moment, sans rien créer.' }
  ];

  formulesApercu = [
    { nom: 'Découverte', prix: '10 000 FCFA', duree: '30 min' },
    { nom: 'Confort', prix: '15 000 FCFA', duree: '1 h', badge: 'LA PLUS CHOISIE' },
    { nom: 'Premium', prix: '25 000 FCFA', duree: '2 h' }
  ];

  temoignages = signal([
    { texte: "Je me suis sentie écoutée sans être jugée.", signature: 'Cliente anonyme' },
    { texte: "J'avais simplement besoin de parler à quelqu'un, tout de suite.", signature: 'Client anonyme' }
  ]);
}