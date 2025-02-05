import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserOnboardingComponent } from './user-onboarding.component';

describe('UserOnboardingComponent', () => {
  let component: UserOnboardingComponent;
  let fixture: ComponentFixture<UserOnboardingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserOnboardingComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UserOnboardingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
