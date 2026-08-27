import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class Header {
  menuOuvert = signal(false);
  servicesOuvert = signal(false);
  formulesOuvert = signal(false);

  toggleMenu() {
    this.menuOuvert.update(v => !v);
  }

  toggleServices() {
    this.servicesOuvert.update(v => !v);
    this.formulesOuvert.set(false);
  }

  toggleFormules() {
    this.formulesOuvert.update(v => !v);
    this.servicesOuvert.set(false);
  }
}