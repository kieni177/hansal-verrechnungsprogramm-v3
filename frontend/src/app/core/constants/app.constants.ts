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
  LOADING: 'Loading...',
  SUCCESS: {
    CREATE: 'Created successfully',
    UPDATE: 'Updated successfully',
    DELETE: 'Deleted successfully'
  },
  ERROR: {
    LOAD: 'Failed to load data',
    CREATE: 'Failed to create',
    UPDATE: 'Failed to update',
    DELETE: 'Failed to delete',
    SEARCH: 'Search failed'
  }
} as const;

export const DIALOG_DATA = {
  DELETE_CONFIRMATION: {
    TITLE: 'Confirm Delete',
    MESSAGE: 'Are you sure you want to delete this item? This action cannot be undone.',
    CONFIRM: 'Delete',
    CANCEL: 'Cancel'
  }
} as const;
