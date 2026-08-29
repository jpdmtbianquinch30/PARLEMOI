import { Routes } from '@angular/router';
import { Home } from './features/home/home';
import { Chat } from './features/chat/chat';
import { SuivreConversation } from './features/suivre-conversation/suivre-conversation';
import { Login } from './features/login/login';
import { Reservation } from './features/reservation/reservation';
import { SuivreReservation } from './features/suivre-reservation/suivre-reservation';
import { Services } from './features/services/services';
import { Formules } from './features/formules/formules';
import { Confidentialite } from './features/confidentialite/confidentialite';
import { CommentCaMarche } from './features/comment-ca-marche/comment-ca-marche';
import { Faq } from './features/faq/faq';
import { creerGuardRole } from './core/auth-guard';
import { EcoutantDashboard } from './features/ecoutant-dashboard/ecoutant-dashboard';
import { AdminDashboard } from './features/admin-dashboard/admin-dashboard';
import { EcoutantChat } from './features/ecoutant-chat/ecoutant-chat';

export const routes: Routes = [
  { path: '', component: Home },
  { path: 'chat', component: Chat },
  { path: 'chat/:code', component: Chat },
  { path: 'suivre-conversation', component: SuivreConversation },
  { path: 'reservation', component: Reservation },
  { path: 'suivre', component: SuivreReservation },
  { path: 'services', component: Services },
  { path: 'services/:slug', component: Services },
  { path: 'formules', component: Formules },
  { path: 'formules/:slug', component: Formules },
  { path: 'confidentialite', component: Confidentialite },
  { path: 'comment-ca-marche', component: CommentCaMarche },
  { path: 'faq', component: Faq },

  { path: 'ecoutant/login', component: Login, data: { role: 'ECOUTANT' } },
  { path: 'admin/login', component: Login, data: { role: 'ADMIN' } },
  { path: 'ecoutant', component: EcoutantDashboard, canActivate: [creerGuardRole('ECOUTANT')] },
    { path: 'admin', component: AdminDashboard, canActivate: [creerGuardRole('ADMIN')] },
  { path: 'ecoutant/conversations/:code', component: EcoutantChat, canActivate: [creerGuardRole('ECOUTANT')] }
];