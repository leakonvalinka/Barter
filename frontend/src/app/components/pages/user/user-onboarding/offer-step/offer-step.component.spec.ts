import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OfferStepComponent } from './offer-step.component';

describe('OfferStepComponent', () => {
  let component: OfferStepComponent;
  let fixture: ComponentFixture<OfferStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OfferStepComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OfferStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
