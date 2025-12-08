import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from '../services/notification.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const notificationService = inject(NotificationService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Ein Fehler ist aufgetreten';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Fehler: ${error.error.message}`;
      } else {
        // Server-side error - check for structured error response
        if (error.error && typeof error.error === 'object') {
          // Handle structured error response from backend
          if (error.error.message) {
            errorMessage = error.error.message;
          }
          // If there are field-specific errors, show them
          if (error.error.errors && Array.isArray(error.error.errors)) {
            const fieldErrors = error.error.errors
              .map((e: any) => e.message || e.field)
              .join(', ');
            if (fieldErrors) {
              errorMessage = fieldErrors;
            }
          }
        } else {
          // Fallback to status-based messages
          switch (error.status) {
            case 400:
              errorMessage = 'Ungültige Eingabe: Bitte überprüfen Sie Ihre Daten';
              break;
            case 401:
              errorMessage = 'Nicht autorisiert: Bitte melden Sie sich an';
              break;
            case 403:
              errorMessage = 'Zugriff verweigert: Sie haben keine Berechtigung';
              break;
            case 404:
              errorMessage = 'Nicht gefunden: Die Ressource existiert nicht';
              break;
            case 500:
              errorMessage = 'Serverfehler: Bitte versuchen Sie es später erneut';
              break;
            default:
              errorMessage = `Fehler ${error.status}: ${error.message}`;
          }
        }
      }

      // Show notification with the error message
      notificationService.error(errorMessage);
      console.error('HTTP Error:', error);

      return throwError(() => error);
    })
  );
};
