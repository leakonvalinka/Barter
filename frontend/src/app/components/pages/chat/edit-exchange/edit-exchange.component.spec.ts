import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditExchangeComponent } from './edit-exchange.component';

describe('EditExchangeComponent', () => {
  let component: EditExchangeComponent;
  let fixture: ComponentFixture<EditExchangeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditExchangeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditExchangeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
