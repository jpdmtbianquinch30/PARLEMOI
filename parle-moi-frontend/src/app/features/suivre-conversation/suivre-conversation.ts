import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ConversationService } from '../../core/conversation';

@Component({
  selector: 'app-suivre-conversation',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './suivre-conversation.html',
  styleUrl: './suivre-conversation.scss'
})
export class SuivreConversation {
  private conversationService = inject(ConversationService);
  private router = inject(Router);

  code = '';
  recherche = signal(false);
  erreur = signal<string | null>(null);

  rechercher(): void {
    const codeSaisi = this.code.trim().toUpperCase();
    if (!codeSaisi) {
      return;
    }

    this.recherche.set(true);
    this.erreur.set(null);

    this.conversationService.trouverParCode(codeSaisi).subscribe({
      next: () => {
        this.recherche.set(false);
        this.router.navigate(['/chat', codeSaisi]);
      },
      error: () => {
        this.recherche.set(false);
        this.erreur.set('Aucune conversation trouvee avec ce code, ou elle a expire.');
      }
    });
  }
}