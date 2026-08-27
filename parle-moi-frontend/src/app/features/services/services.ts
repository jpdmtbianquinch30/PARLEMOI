import { Component, signal, computed, inject } from '@angular/core';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';

interface ServiceItem {
  slug: string;
  nom: string;
  image: string;
  description: string;
  detail: string;
}

const SERVICES: ServiceItem[] = [
  { slug: 'ecoute-discussion', nom: 'Écoute & discussion', image: 'service.png', description: "Un moment pour parler librement avec une personne attentive.", detail: "Que tu aies besoin de vider ton sac ou simplement de partager ce que tu vis, cet espace est là pour t'écouter, sans détourner la conversation vers autre chose." },
  { slug: 'soutien-rupture', nom: 'Soutien après une rupture', image: 'service.png', description: "Un espace pour exprimer ses émotions après une séparation.", detail: "Une rupture peut laisser un vide difficile à porter seul(e). Ici, tu peux parler de ce que tu ressens, à ton rythme, sans pression." },
  { slug: 'difficultes-relationnelles', nom: 'Difficultés relationnelles', image: 'service.png', description: "Parler des difficultés liées au couple, à l'amitié ou aux relations.", detail: "Les relations ne sont pas toujours simples. Prends un moment pour poser ce qui te pèse et voir plus clair dans ce que tu ressens." },
  { slug: 'motivation', nom: 'Motivation & encouragement', image: 'service.png', description: "Un échange pour retrouver confiance et motivation.", detail: "Certains jours sont plus lourds que d'autres. Un échange bienveillant peut t'aider à retrouver un peu d'élan." },
  { slug: 'conversation-libre', nom: 'Conversation libre', image: 'service.png', description: "Tu veux simplement parler ? Tu peux réserver un moment d'écoute.", detail: "Pas de sujet précis, juste l'envie de parler à quelqu'un. C'est amplement suffisant pour réserver ce moment." }
];
  


@Component({
  selector: 'app-services',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './services.html',
  styleUrl: './services.scss'
})
export class Services {
  private route = inject(ActivatedRoute);
  private params = toSignal(this.route.paramMap);

  services = signal(SERVICES);

  serviceActif = computed(() => {
    const slug = this.params()?.get('slug');
    return slug ? this.services().find(s => s.slug === slug) ?? null : null;
  });
}