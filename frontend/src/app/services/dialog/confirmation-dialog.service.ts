import { Injectable, inject } from '@angular/core';
import { NgpDialogManager } from 'ng-primitives/dialog';
import { ConfirmationDialogComponent, ConfirmationDialogData, DialogResult, dialogResult$ } from '../../components/util/confirmation-dialog/confirmation-dialog.component';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ConfirmationDialogService {
  private dialogManager = inject(NgpDialogManager);

  /**
   * Opens a confirmation dialog and returns a promise that resolves to a DialogResult
   * @param options The dialog configuration options
   * @returns Promise<DialogResult>
   */
  async confirm(options: ConfirmationDialogData): Promise<DialogResult> {
    const dialogRef = this.dialogManager.open(ConfirmationDialogComponent, {
      data: options
    });

    try {
      const result = await firstValueFrom(dialogResult$);
      dialogRef.close('mouse');
      return result;
    } catch {
      return { confirmed: false };
    }
  }
}