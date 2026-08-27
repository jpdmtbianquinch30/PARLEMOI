import { Component, signal, computed, inject } from '@angular/core';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';

interface FormuleItem {
  slug: string;
  nom: string;
  prix: string;
  duree: string;
  badge?: string;
  inclus: string[];
}

const FORMULES: FormuleItem[] = [
  {
    slug: 'decouverte',
    nom: 'Découverte',
    prix: '10 000 FCFA',
    duree: '30 minutes',
    inclus: ['Écoute attentive', 'Conversation confidentielle', 'Échange individuel']
  },
  {
    slug: 'confort',
    nom: 'Confort',
    prix: '15 000 FCFA',
    duree: '1 heure',
    badge: 'LA PLUS CHOISIE',
    inclus: ['Écoute personnalisée', 'Discussion approfondie', 'Échange confidentiel']
  },
  {
    slug: 'premium',
    nom: 'Premium',
    prix: '25 000 FCFA',
    duree: '2 heures',
    inclus: ['Échange prolongé', 'Conversation personnalisée', "Moment d'écoute approfondi"]
  }
];

@Component({
  selector: 'app-formules',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './formules.html',
  styleUrl: './formules.scss'
})
export class Formules {
  private route = inject(ActivatedRoute);
  private params = toSignal(this.route.paramMap);

  formules = signal(FORMULES);

  formuleActive = computed(() => {
    const slug = this.params()?.get('slug');
    return slug ? this.formules().find(f => f.slug === slug) ?? null : null;
  });
}