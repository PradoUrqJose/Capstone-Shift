import {
  Component,
  Input,
  Output,
  EventEmitter,
  ViewChild,
  ElementRef,
  AfterViewInit
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Observable } from 'rxjs';
import { Tienda } from '../../../services/tienda.service';
import { TurnoService } from '../../../services/turno.service';
import { CalendarMultiselectComponent } from '../calendar-multiselect/calendar-multiselect.component';

import tippy from 'tippy.js';
import 'tippy.js/dist/tippy.css';
import 'tippy.js/animations/shift-away-extreme.css';
import 'tippy.js/themes/light.css';

@Component({
  selector: 'app-turnos-masa-modal',
  standalone: true,
  templateUrl: './turnos-masa-modal.component.html',
  styleUrls: ['./turnos-masa-modal.component.css'],
  imports: [
    CommonModule,
    FormsModule,
    CalendarMultiselectComponent
  ],
})
export class TurnosMasaModalComponent implements AfterViewInit {
  @Input() mostrarModal: boolean = false;
  @Input() isModalVisible: boolean = false;
  @Input() colaboradorId: number | null = null;
  @Input() tiendas$: Observable<Tienda[]> = new Observable<Tienda[]>();
  @Input() mesActual: number = new Date().getMonth(); // 0-based
  @Input() anioActual: number = new Date().getFullYear();

  @Output() cerrarModalEvent = new EventEmitter<void>();
  @Output() turnosGuardados = new EventEmitter<void>();

  @ViewChild('errorTooltipRef') errorTooltipRef!: ElementRef;

  fechasSeleccionadas: string[] = [];
  horaInicio: string = '09:00';
  horaFin: string = '17:00';
  tiendaId: number | null = null;
  isSubmitting: boolean = false;

  constructor(private turnoService: TurnoService) {}

  ngAfterViewInit(): void {
    // Opcional: inicialización diferida si se requiere
  }

  cerrarModal(): void {
    this.resetFormulario();
    this.cerrarModalEvent.emit();
  }

  private resetFormulario(): void {
    this.fechasSeleccionadas = [];
    this.horaInicio = '09:00';
    this.horaFin = '17:00';
    this.tiendaId = null;
  }

  private mostrarErrorTooltip(mensaje: string): void {
    if (!this.errorTooltipRef) return;

    tippy(this.errorTooltipRef.nativeElement, {
      content: mensaje,
      showOnCreate: true,
      trigger: 'manual',
      theme: 'light',
      animation: 'shift-away-extreme',
      placement: 'top',
      arrow: true,
      onHidden(instance) {
        instance.destroy();
      },
    });
  }

  guardarTurnosMasa(): void {
    if (!this.colaboradorId || !this.tiendaId || !this.horaInicio || !this.horaFin || this.fechasSeleccionadas.length === 0) {
      return;
    }

    this.isSubmitting = true;

    const payload = {
      colaboradorId: this.colaboradorId,
      tiendaId: Number(this.tiendaId),
      fechas: this.fechasSeleccionadas,
      horaInicio: this.horaInicio,
      horaFin: this.horaFin
    };

    console.log('Turnos en Masa', payload);

    this.turnoService.crearTurnosMasa(payload).subscribe({
      next: () => {
        this.turnosGuardados.emit();
        this.cerrarModal();
        this.isSubmitting = false;
      },
      error: (err) => {
        const msg = err?.error?.error || 'Error al guardar turnos en masa';
        console.error('Error backend:', msg);
        this.mostrarErrorTooltip(msg);
        this.isSubmitting = false;
      }
    });
  }
}
