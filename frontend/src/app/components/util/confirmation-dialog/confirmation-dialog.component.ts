import { Component } from '@angular/core';
import { NgpDialog, NgpDialogDescription, NgpDialogOverlay, NgpDialogTitle } from 'ng-primitives/dialog';
import { injectDialogRef } from 'ng-primitives/dialog';
import { Subject } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string | false;
  showTextField?: boolean;
  textFieldLabel?: string;
  textFieldPlaceholder?: string;
  callback?: (result: DialogResult) => void;
}

export interface DialogResult {
  confirmed: boolean;
  text?: string;
}

export const dialogResult$ = new Subject<DialogResult>();

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [
    NgpDialog,
    NgpDialogOverlay,
    NgpDialogTitle,
    NgpDialogDescription,
    FormsModule,
    CommonModule
  ],
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent {
  protected readonly dialogRef = injectDialogRef<ConfirmationDialogData>();
  protected userText: string = '';

  protected isConfirmEnabled(): boolean {
    if (this.dialogRef.data?.showTextField) {
      return this.userText.trim().length > 0;
    }
    return true;
  }

  close(confirmed: boolean) {
    if (confirmed && this.dialogRef.data?.callback) {
      this.dialogRef.data.callback({
        confirmed,
        text: this.dialogRef.data?.showTextField ? this.userText.trim() : undefined
      });
    }
    dialogResult$.next({
      confirmed,
      text: this.dialogRef.data?.showTextField ? this.userText.trim() : undefined
    });
    this.dialogRef.close('mouse');
  }
}
