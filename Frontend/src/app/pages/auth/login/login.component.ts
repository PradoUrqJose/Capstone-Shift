import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import Notiflix from 'notiflix';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  loginForm: FormGroup = this.fb.group({
    username: ['', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(20),
      Validators.pattern(/^[a-zA-Z0-9_]+$/)
    ]],
    password: ['', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(20)
    ]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      console.log('🚫 Formulario inválido:', this.loginForm.value);
      this.loginForm.markAllAsTouched();
      return;
    }

    const { username, password } = this.loginForm.value;
    console.log('🔐 Intentando login con:', { username, password });

    this.authService.login(username, password).subscribe({
      next: (response) => {
        console.log('🎉 Login exitoso:', response);
        console.log('🎉 Redirigiendo a /turnos');
        Notiflix.Notify.success('Inicio de sesión exitoso');
        this.router.navigate(['/turnos']);
      },
      error: (err) => {
        console.error('🚨 Error de login:', err);
        console.error('🚨 Mensaje de error:', err.message);
        Notiflix.Notify.failure('Error al iniciar sesión');
      },
      complete: () => console.log('🏁 Suscripción completada')
    });
  }
}
