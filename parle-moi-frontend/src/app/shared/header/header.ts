import { Component, signal, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  private router = inject(Router);

  menuOuvert = signal(false);
  servicesOuvert = signal(false);
  formulesOuvert = signal(false);
  suiviOuvert = signal(false);

  urlActuelle = toSignal(
    this.router.events.pipe(
      filter((e): e is NavigationEnd => e instanceof NavigationEnd),
      map(e => e.urlAfterRedirects)
    ),
    { initialValue: this.router.url }
  );

  servicesActif = computed(() => this.urlActuelle().startsWith('/services'));
  formulesActif = computed(() => this.urlActuelle().startsWith('/formules'));
  suiviActif = computed(() =>
    this.urlActuelle().startsWith('/suivre-conversation') || this.urlActuelle().startsWith('/suivre')
  );

  toggleMenu() {
    this.menuOuvert.update(v => !v);
  }

  toggleServices() {
    this.servicesOuvert.update(v => !v);
    this.formulesOuvert.set(false);
    this.suiviOuvert.set(false);
  }

  toggleFormules() {
    this.formulesOuvert.update(v => !v);
    this.servicesOuvert.set(false);
    this.suiviOuvert.set(false);
  }

  toggleSuivi() {
    this.suiviOuvert.update(v => !v);
    this.servicesOuvert.set(false);
    this.formulesOuvert.set(false);
  }

  fermerMenu() {
    this.menuOuvert.set(false);
    this.servicesOuvert.set(false);
    this.formulesOuvert.set(false);
    this.suiviOuvert.set(false);
  }
}