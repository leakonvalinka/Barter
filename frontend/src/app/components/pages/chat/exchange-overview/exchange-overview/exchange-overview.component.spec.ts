import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExchangeOverviewComponent } from './exchange-overview.component';

describe('ExchangeOverviewComponent', () => {
  let component: ExchangeOverviewComponent;
  let fixture: ComponentFixture<ExchangeOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExchangeOverviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ExchangeOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
