import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { APP_CONSTANTS, DIALOG_DATA } from '../constants/app.constants';
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../../components/shared/confirmation-dialog/confirmation-dialog.component';

export class DialogUtils {
  static openConfirmation(
    dialog: MatDialog,
    data: Partial<ConfirmationDialogData> = {}
  ): MatDialogRef<ConfirmationDialogComponent> {
    return dialog.open(ConfirmationDialogComponent, {
      width: APP_CONSTANTS.DIALOG_WIDTH.SMALL,
      data: {
        title: data.title || DIALOG_DATA.DELETE_CONFIRMATION.TITLE,
        message: data.message || DIALOG_DATA.DELETE_CONFIRMATION.MESSAGE,
        confirmText: data.confirmText || DIALOG_DATA.DELETE_CONFIRMATION.CONFIRM,
        cancelText: data.cancelText || DIALOG_DATA.DELETE_CONFIRMATION.CANCEL
      }
    });
  }

  static confirmAction(dialog: MatDialog, data?: Partial<ConfirmationDialogData>): Observable<boolean> {
    return this.openConfirmation(dialog, data).afterClosed();
  }
}
