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
    { valeur: '3', label: 'Formules flexibles' },
    { valeur: '0', label: 'Jugement' }
  ];

  atouts = [
    { titre: 'Écoute', texte: 'Une écoute attentive, sans interruption et sans jugement.', icone: 'ecoute.png' },
    { titre: 'Bienveillance', texte: 'Un échange humain dans un environnement respectueux.', icone: 'hands.png' },
    { titre: 'Confidentialité', texte: 'Une réservation conçue pour préserver votre vie privée.', icone: 'lock.png' },
    { titre: 'Sans jugement', texte: 'Vous pouvez parler librement de ce que vous ressentez.', icone: 'heart.png' }
  ];

  formulesApercu = [
    { nom: 'Découverte', prix: '10 000 FCFA', duree: '30 min' },
    { nom: 'Confort', prix: '15 000 FCFA', duree: '1 h', badge: 'LA PLUS CHOISIE' },
    { nom: 'Premium', prix: '25 000 FCFA', duree: '2 h' }
  ];

  temoignages = signal([
    { texte: "Je me suis sentie écoutée sans être jugée.", signature: 'Cliente anonyme' },
    { texte: "J'avais simplement besoin de parler à quelqu'un.", signature: 'Client anonyme' }
  ]);
}