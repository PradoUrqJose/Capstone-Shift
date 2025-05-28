import { Routes } from '@angular/router';

import ColaboradoresComponent from './pages/colaboradores/colaboradores.component';
import EmpresasComponent from './pages/empresas/empresas.component';
import TurnosComponent from './pages/turnos/turnos.component';
import ReportesComponent from './pages/reportes/reportes.component';
import { HorasTrabajadasComponent } from './pages/reportes/horas-trabajadas/horas-trabajadas.component';
import { TurnosFeriadosComponent } from './pages/reportes/turnos-feriados/turnos-feriados.component';
import { SemanaNormalComponent } from './pages/reportes/semana-normal/semana-normal.component';
import { ColaboradorProfileComponent } from './pages/reportes/colaborador-profile/colaborador-profile.component';
import GestionarPuestosComponent from './pages/colaboradores/gestionar-puestos/gestionar-puestos.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'empresas', component: EmpresasComponent, canActivate: [AuthGuard] },
  { path: 'colaboradores', component: ColaboradoresComponent, canActivate: [AuthGuard] },
  { path: 'puestos', component: GestionarPuestosComponent, canActivate: [AuthGuard] },
  { path: 'turnos', component: TurnosComponent, canActivate: [AuthGuard] },
  {
    path: 'reportes',
    component: ReportesComponent,
    canActivate: [AuthGuard],
    children: [
      { path: 'horas-trabajadas', component: HorasTrabajadasComponent },
      { path: 'colaborador-profile/:id', component: ColaboradorProfileComponent },
      { path: 'turnos-feriados', component: TurnosFeriadosComponent },
      { path: 'semana-normal', component: SemanaNormalComponent },
      { path: '', redirectTo: 'horas-trabajadas', pathMatch: 'full' }
    ]
  },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
