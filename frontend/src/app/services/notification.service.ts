import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { APP_CONSTANTS } from '../shared/constants/app.constants';

type NotificationType = 'success' | 'error' | 'info' | 'warning';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private readonly defaultConfig: MatSnackBarConfig = {
    horizontalPosition: 'end',
    verticalPosition: 'top'
  };

  constructor(private snackBar: MatSnackBar) {}

  show(message: string, type: NotificationType = 'info'): void {
    const config: MatSnackBarConfig = {
      ...this.defaultConfig,
      duration: this.getDuration(type),
      panelClass: [`${type}-snackbar`]
    };

    this.snackBar.open(message, 'Close', config);
  }

  success(message: string): void {
    this.show(message, 'success');
  }

  error(message: string): void {
    this.show(message, 'error');
  }

  info(message: string): void {
    this.show(message, 'info');
  }

  warning(message: string): void {
    this.show(message, 'warning');
  }

  private getDuration(type: NotificationType): number {
    return type === 'error'
      ? APP_CONSTANTS.NOTIFICATION_DURATION.LONG
      : APP_CONSTANTS.NOTIFICATION_DURATION.SHORT;
  }
}
