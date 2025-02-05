import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ComponentExamplesComponent } from './component-examples.component';

describe('ComponentExamplesComponent', () => {
  let component: ComponentExamplesComponent;
  let fixture: ComponentFixture<ComponentExamplesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ComponentExamplesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ComponentExamplesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
