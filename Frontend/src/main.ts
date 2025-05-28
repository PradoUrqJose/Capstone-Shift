import { configurarNotiflix } from './app/config/notiflix-config';
import { AppComponent } from './app/app.component';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config'; // Importar appConfig
import './styles.css';

configurarNotiflix();
bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
