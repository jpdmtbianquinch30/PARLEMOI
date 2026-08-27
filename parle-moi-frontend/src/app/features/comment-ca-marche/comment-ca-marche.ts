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
    { num: '01', titre: 'Choisis ta formule', texte: 'Découverte, Confort ou Premium selon le temps dont tu as besoin.' },
    { num: '02', titre: 'Choisis ton créneau', texte: 'Une date et une heure qui te conviennent.' },
    { num: '03', titre: 'Réserve anonymement', texte: 'Aucune information personnelle, juste ton choix confirmé.' },
    { num: '04', titre: 'Conserve ton code', texte: 'Il te permet de retrouver et gérer ta réservation à tout moment.' }
  ];
}