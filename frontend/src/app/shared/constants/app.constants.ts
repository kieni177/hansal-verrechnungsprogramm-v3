export const APP_CONSTANTS = {
  DIALOG_WIDTH: {
    SMALL: '500px',
    MEDIUM: '700px',
    LARGE: '1100px',
    XLARGE: '1400px'
  },
  NOTIFICATION_DURATION: {
    SHORT: 3000,
    MEDIUM: 4000,
    LONG: 5000
  }
} as const;

export const API_MESSAGES = {
  LOADING: 'Lädt...',
  SUCCESS: {
    CREATE: 'Erfolgreich erstellt',
    UPDATE: 'Erfolgreich aktualisiert',
    DELETE: 'Erfolgreich gelöscht'
  },
  ERROR: {
    LOAD: 'Fehler beim Laden der Daten',
    CREATE: 'Fehler beim Erstellen',
    UPDATE: 'Fehler beim Aktualisieren',
    DELETE: 'Fehler beim Löschen',
    SEARCH: 'Suche fehlgeschlagen'
  }
} as const;

export const DIALOG_DATA = {
  DELETE_CONFIRMATION: {
    TITLE: 'Löschen bestätigen',
    MESSAGE: 'Sind Sie sicher, dass Sie dieses Element löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden.',
    CONFIRM: 'Löschen',
    CANCEL: 'Abbrechen'
  }
} as const;
