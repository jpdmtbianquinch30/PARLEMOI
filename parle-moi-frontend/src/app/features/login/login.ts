import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, Role } from '../../core/auth';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  private route = inject(ActivatedRoute);
  roleAttendu: Role = (this.route.snapshot.data['role'] as Role) ?? 'ECOUTANT';

  private authService = inject(AuthService);
  private router = inject(Router);

  email = '';
  motDePasse = '';
  chargement = signal(false);
  erreur = signal<string | null>(null);

  get titre(): string {
    return this.roleAttendu === 'ADMIN' ? 'Connexion administrateur' : 'Connexion écoutante';
  }

  connecter(): void {
    if (!this.email || !this.motDePasse) {
      return;
    }

    this.chargement.set(true);
    this.erreur.set(null);

    this.authService.login(this.email, this.motDePasse).subscribe({
      next: (reponse) => {
        this.chargement.set(false);

        if (reponse.role !== this.roleAttendu) {
          this.erreur.set("Ce compte n'a pas les droits necessaires pour acceder a cet espace.");
          this.authService.deconnecter();
          return;
        }

        const destination = reponse.role === 'ADMIN' ? '/admin' : '/ecoutant';
        this.router.navigate([destination]);
      },
      error: (err) => {
        this.chargement.set(false);
        if (err.status === 423) {
          this.erreur.set('Compte temporairement verrouille suite a plusieurs echecs. Reessayez plus tard.');
        } else {
          this.erreur.set('Email ou mot de passe incorrect.');
        }
      }
    });
  }
}