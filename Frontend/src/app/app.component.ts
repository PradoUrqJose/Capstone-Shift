import { Component } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import NavbarComponent from './components/navbar/navbar.component';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'sportcenter-shift-manager';

  constructor(public  router: Router){}

  isLoginRoute(): boolean {
    return this.router.url === '/login';
  }
}
