import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Chat } from './features/chat/chat';
import { Reservation } from './features/reservation/reservation';
import { SuivreReservation } from './features/suivre-reservation/suivre-reservation';
import { Services } from './features/services/services';
import { Formules } from './features/formules/formules';
import { Confidentialite } from './features/confidentialite/confidentialite';
import { CommentCaMarche } from './features/comment-ca-marche/comment-ca-marche';
import { Faq } from './features/faq/faq';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'chat', component: Chat },
  { path: 'chat/:code', component: Chat },
  { path: 'reservation', component: Reservation },
  { path: 'suivre', component: SuivreReservation },
  { path: 'services', component: Services },
  { path: 'services/:slug', component: Services },
  { path: 'formules', component: Formules },
  { path: 'formules/:slug', component: Formules },
  { path: 'confidentialite', component: Confidentialite },
  { path: 'comment-ca-marche', component: CommentCaMarche },
  { path: 'faq', component: Faq }
];