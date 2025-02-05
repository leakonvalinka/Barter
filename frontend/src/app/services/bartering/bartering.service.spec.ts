import { TestBed } from '@angular/core/testing';
import {BarteringService} from './bartering.service';


describe('RegistrationService', () => {
  let service: BarteringService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BarteringService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
