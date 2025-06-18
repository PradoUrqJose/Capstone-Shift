import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeriadoService, Feriado } from '../../../services/feriado.service';

@Component({
  selector: 'app-calendar-multiselect',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar-multiselect.component.html',
})
export class CalendarMultiselectComponent implements OnInit {
  @Input() year: number = new Date().getFullYear();
  @Input() month: number = new Date().getMonth(); // 0-based
  @Input() selectedDates: string[] = [];

  @Output() selectedDatesChange = new EventEmitter<string[]>();

  feriados: Set<string> = new Set();

  diasSemana = ['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'];

  constructor(private feriadoService: FeriadoService) {}

  ngOnInit(): void {
    this.cargarFeriados();
  }

  cargarFeriados(): void {
    this.feriadoService.getFeriados().subscribe({
      next: (data: Feriado[]) => {
        this.feriados = new Set(data.map(f => f.fecha));
      },
      error: (err) => {
        console.warn('No se pudieron cargar los feriados:', err.message);
      }
    });
  }

  get daysInMonth(): Date[] {
    const days: Date[] = [];
    const firstDay = new Date(this.year, this.month, 1);
    const lastDay = new Date(this.year, this.month + 1, 0);

    for (let d = new Date(firstDay); d <= lastDay; d.setDate(d.getDate() + 1)) {
      days.push(new Date(d));
    }

    return days;
  }

  get startWeekdayOffset(): number {
    const firstDay = new Date(this.year, this.month, 1);
    // Ajuste para que la semana comience en LUNES (0 = lunes)
    return (firstDay.getDay() + 6) % 7;
  }

  toDateStr(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  toggleDate(date: Date): void {
    const str = this.toDateStr(date);
    const index = this.selectedDates.indexOf(str);
    if (index >= 0) {
      this.selectedDates.splice(index, 1);
    } else {
      this.selectedDates.push(str);
    }
    this.selectedDatesChange.emit([...this.selectedDates]);
  }

  isSelected(date: Date): boolean {
    return this.selectedDates.includes(this.toDateStr(date));
  }

  isFeriado(date: Date): boolean {
    return this.feriados.has(this.toDateStr(date));
  }

  anteriorMes(): void {
    if (this.month === 0) {
      this.month = 11;
      this.year -= 1;
    } else {
      this.month--;
    }
  }

  siguienteMes(): void {
    if (this.month === 11) {
      this.month = 0;
      this.year += 1;
    } else {
      this.month++;
    }
  }

  get nombreMes(): string {
    const meses = ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
                   'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'];
    return `${meses[this.month]} ${this.year}`;
  }
}
