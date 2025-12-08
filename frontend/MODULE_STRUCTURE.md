# Angular Application Module Structure

This document describes the modular architecture of the application.

## Overview

The application follows a feature-based modular architecture using Angular standalone components with lazy-loaded routes. Each feature has its own module with dedicated routing.

## Directory Structure

```
src/app/
├── shared/                          # Shared module with common functionality
│   ├── base/                        # Base classes for CRUD operations
│   │   ├── base.service.ts          # Generic CRUD service
│   │   ├── base-crud.component.ts   # Generic CRUD component
│   │   └── index.ts                 # Barrel export
│   ├── utils/                       # Utility functions
│   │   ├── dialog.utils.ts          # Dialog helpers
│   │   ├── download.utils.ts        # File download helpers
│   │   ├── form.utils.ts            # Form validation helpers
│   │   └── index.ts                 # Barrel export
│   ├── constants/                   # Application constants
│   │   ├── app.constants.ts         # Global constants
│   │   └── index.ts                 # Barrel export
│   ├── index.ts                     # Main barrel export
│   └── shared.module.ts             # Shared module definition
│
├── components/
│   ├── products/                    # Products feature module
│   │   ├── products.component.ts    # Product list
│   │   ├── product-detail/          # Product detail view
│   │   ├── product-form/            # Product create/edit form
│   │   ├── products.routes.ts       # Feature routes (deprecated)
│   │   └── products.module.ts       # Feature module with routing
│   │
│   ├── orders/                      # Orders feature module
│   │   ├── orders.component.ts      # Order list
│   │   ├── order-detail/            # Order detail view
│   │   ├── order-form/              # Order create/edit form
│   │   ├── orders.routes.ts         # Feature routes (deprecated)
│   │   └── orders.module.ts         # Feature module with routing
│   │
│   ├── slaughter/                   # Slaughter feature module
│   │   ├── slaughter.component.ts   # Slaughter record list
│   │   ├── slaughter-detail/        # Slaughter detail view (NEW)
│   │   ├── slaughter-form/          # Slaughter record create/edit form
│   │   ├── slaughter.routes.ts      # Feature routes (deprecated)
│   │   └── slaughter.module.ts      # Feature module with routing
│   │
│   ├── invoices/                    # Invoices feature module
│   │   ├── invoices.component.ts    # Invoice list
│   │   ├── invoices.routes.ts       # Feature routes (deprecated)
│   │   └── invoices.module.ts       # Feature module with routing
│   │
│   ├── dashboard/                   # Dashboard component
│   └── login/                       # Login component
│
├── services/                        # Application services
│   ├── product.service.ts           # Product API service
│   ├── order.service.ts             # Order API service
│   ├── slaughter.service.ts         # Slaughter API service
│   ├── invoice.service.ts           # Invoice API service
│   ├── notification.service.ts      # Notification service
│   └── loading.service.ts           # Loading indicator service
│
├── models/                          # Data models
│   ├── product.model.ts
│   ├── order.model.ts
│   ├── slaughter.model.ts
│   └── invoice.model.ts
│
└── app.routes.ts                    # Main application routing
```

## Module Breakdown

### 1. Shared Module (`src/app/shared/`)

Contains reusable code shared across features:

- **Base Classes**: Generic CRUD operations
  - `BaseService<T>`: Abstract service with CRUD methods
  - `BaseCrudComponent<T>`: Abstract component with common CRUD logic

- **Utils**: Helper functions
  - `DialogUtils`: Confirmation dialogs
  - `DownloadUtils`: File downloads
  - `FormUtils`: Form validation helpers

- **Constants**: Application-wide constants
  - `API_MESSAGES`: Success/error messages
  - `APP_CONSTANTS`: Configuration values

**Usage:**
```typescript
import { BaseService, FormUtils, API_MESSAGES } from '../../shared';
```

### 2. Feature Modules

Each feature has its own module with:
- Routing configuration
- Component declarations
- Feature-specific logic

#### Products Module (`components/products/`)
- List, create, edit, and view product details
- Product search functionality

#### Orders Module (`components/orders/`)
- Order management
- Order item tracking
- Customer information

#### Slaughter Module (`components/slaughter/`)
- Cow slaughter record tracking
- Meat cut breakdown
- **NEW**: Detail view with clickable table rows
- Weight and pricing tracking

#### Invoices Module (`components/invoices/`)
- Invoice listing
- PDF generation and download
- Payment status tracking

## Routing Strategy

The application uses lazy-loaded feature modules for better performance:

```typescript
// app.routes.ts
{
  path: 'products',
  loadChildren: () => import('./components/products/products.module')
    .then(m => m.PRODUCTS_ROUTES)
}
```

## Key Features

### 1. Lazy Loading
All feature modules are lazy-loaded, reducing initial bundle size.

### 2. Standalone Components
Uses Angular standalone components (no NgModules required for components).

### 3. Barrel Exports
Index files provide clean import paths:
```typescript
// Instead of:
import { BaseService } from '../../shared/base/base.service';
import { FormUtils } from '../../shared/utils/form.utils';

// Use:
import { BaseService, FormUtils } from '../../shared';
```

### 4. Type Safety
All services and components use TypeScript generics for type safety.

## Navigation Flow

### Slaughter Records (Example)
1. **List View** (`/slaughter`) - Table with all records
2. **Click Row** → Navigate to detail view (`/slaughter/:id`)
3. **Detail View** - Shows:
   - Cow information
   - Slaughter date
   - Total weight
   - Meat cuts breakdown table
   - Edit/delete actions

## Migration Notes

### Old Structure → New Structure

| Old Import | New Import |
|------------|------------|
| `'../../core/base/base.service'` | `'../../shared/base/base.service'` |
| `'../../core/utils/form.utils'` | `'../../shared/utils/form.utils'` |
| `'../../core/constants/app.constants'` | `'../../shared/constants/app.constants'` |

### Deprecated Files
- `*.routes.ts` files are deprecated in favor of `*.module.ts` files
- Old `core/` directory structure (kept for reference, use `shared/` instead)

## Best Practices

1. **Keep features isolated**: Each feature module should be self-contained
2. **Use shared module**: Common functionality goes in `shared/`
3. **Lazy load features**: Use `loadChildren` in routes
4. **Type everything**: Leverage TypeScript's type system
5. **Barrel exports**: Create index files for clean imports

## Future Enhancements

- Add authentication guard module
- Create reusable UI component library
- Implement state management (NgRx/Akita)
- Add feature-specific services in feature modules
