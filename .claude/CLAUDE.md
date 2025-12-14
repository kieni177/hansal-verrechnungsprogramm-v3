# Hansal Verrechnungsprogramm - Project Instructions

## Project Overview

- **Backend**: Spring Boot 3.2.1, Java 21, PostgreSQL 16
- **Frontend**: Angular 20, Angular Material 20, TypeScript 5.7
- **Language**: German UI (Rechnungen, Bestellungen, Produkte, Schlachtungen)

## Database Migrations (Liquibase)

This project uses **Liquibase** for database schema management.

### When modifying JPA entity classes

When making changes to entity classes in `backend/src/main/java/com/hansal/verrechnungsprogramm/model/`:

1. **ALWAYS create a corresponding Liquibase migration** in `backend/src/main/resources/db/changelog/changes/`
2. **Name migrations sequentially**: `XXX-description.yaml` (e.g., `002-add-customer-email.yaml`)
3. **Add the new migration** to `db.changelog-master.yaml`
4. **Never rely on Hibernate auto-DDL** for schema changes (ddl-auto is set to `validate`)

### Migration File Template

```yaml
databaseChangeLog:
  - changeSet:
      id: XXX-description
      author: hansal
      changes:
        - addColumn:
            tableName: table_name
            columns:
              - column:
                  name: column_name
                  type: VARCHAR(255)
```

### Common Operations

- **Add column**: `addColumn`
- **Drop column**: `dropColumn`
- **Rename column**: `renameColumn`
- **Add index**: `createIndex`
- **Add foreign key**: `addForeignKeyConstraint`
- **Modify column type**: `modifyDataType`

### Existing Tables

- `products` - Product catalog
- `orders` - Customer orders
- `order_items` - Line items in orders
- `invoices` - Generated invoices
- `slaughters` - Meat slaughter records
- `meat_cuts` - Individual cuts from slaughters
- `users` - Admin users

### Testing Migrations

After creating a migration, test it by running:
```bash
docker-compose down -v  # Remove existing database
docker-compose up -d    # Start fresh with migrations
```

## Frontend Patterns

### Table Sorting with MatSort

When adding sorting to tables with `*ngIf`, use ViewChild setter pattern:

```typescript
@ViewChild(MatSort) set sort(sort: MatSort) {
  if (sort) {
    this.dataSource.sort = sort;
  }
}
```

This ensures sorting works even when the table is conditionally rendered.

### Custom Sorting for Nested Properties

For nested properties (e.g., `order.customerName`), add a custom sortingDataAccessor:

```typescript
@ViewChild(MatSort) set sort(sort: MatSort) {
  if (sort) {
    this.dataSource.sort = sort;
    this.dataSource.sortingDataAccessor = (item, property) => {
      switch (property) {
        case 'customerName':
          return item.order?.customerName?.toLowerCase() || '';
        default:
          return (item as any)[property];
      }
    };
  }
}
```

### Confirmation Dialogs

Use the existing `ConfirmationDialogComponent` for confirmations:

```typescript
import { ConfirmationDialogComponent, ConfirmationDialogData } from '../shared/confirmation-dialog/confirmation-dialog.component';

const dialogData: ConfirmationDialogData = {
  title: 'Title here',
  message: 'Message here',
  confirmText: 'Confirm',
  cancelText: 'Cancel'
};

this.dialog.open(ConfirmationDialogComponent, {
  width: '400px',
  data: dialogData
}).afterClosed().subscribe(confirmed => {
  if (confirmed) { /* ... */ }
});
```

### Autocomplete Pattern

For autocomplete with auto-fill:

```typescript
// In component
customers: Customer[] = [];
filteredCustomers$!: Observable<Customer[]>;

ngOnInit() {
  this.loadCustomers();
  this.filteredCustomers$ = this.form.get('customerName')!.valueChanges.pipe(
    startWith(''),
    map(value => this.filterCustomers(value || ''))
  );
}

filterCustomers(value: string): Customer[] {
  return this.customers.filter(c => c.name.toLowerCase().includes(value.toLowerCase()));
}

onCustomerSelected(customerName: string): void {
  const customer = this.customers.find(c => c.name === customerName);
  if (customer) {
    this.form.patchValue({
      customerPhone: customer.phone || '',
      customerAddress: customer.address || ''
    });
  }
}
```

## Backend Patterns

### DTO Classes

DTOs should be in `backend/src/main/java/com/hansal/verrechnungsprogramm/dto/`:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private String name;
    private String phone;
    private String address;
}
```

### PDF Generation

PDF generation uses iText 8. See `InvoiceService.generatePdf()` and `generateCombinedPdf()` for examples.

## Testing Changes

After making changes:

```bash
# Backend
cd backend && mvn compile

# Frontend
cd frontend && npm run build

# Docker (rebuilds both)
docker-compose up -d --build
```

## Project Structure Quick Reference

```
backend/src/main/java/com/hansal/verrechnungsprogramm/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Data access
├── model/           # JPA entities
├── dto/             # Data transfer objects
└── config/          # Configuration

frontend/src/app/
├── components/      # UI components (products, orders, invoices, slaughter)
├── services/        # API services
├── models/          # TypeScript interfaces
└── shared/          # Shared utilities and base classes
```
