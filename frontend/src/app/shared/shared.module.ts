/**
 * Shared Module
 *
 * This module contains shared functionality used across the application:
 * - Base classes for CRUD operations (BaseService, BaseCrudComponent)
 * - Utility functions (DialogUtils, DownloadUtils, FormUtils)
 * - Application constants
 *
 * Components and services can import from this module using:
 * import { BaseService, BaseCrudComponent } from '@app/shared';
 */

// Re-export all shared functionality
export * from './base';
export * from './utils';
export * from './constants';
