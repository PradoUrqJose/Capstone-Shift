import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarMultiselectComponent } from './calendar-multiselect.component';

describe('CalendarMultiselectComponent', () => {
  let component: CalendarMultiselectComponent;
  let fixture: ComponentFixture<CalendarMultiselectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarMultiselectComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CalendarMultiselectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
