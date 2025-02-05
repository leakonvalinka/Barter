import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { ownPageGuard } from './own-page.guard';

describe('ownPageGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => ownPageGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
