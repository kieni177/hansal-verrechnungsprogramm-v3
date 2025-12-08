import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { Order, OrderItem, OrderStatus } from '../../../models/order.model';
import { Product } from '../../../models/product.model';
import { OrderService } from '../../../services/order.service';
import { ProductService } from '../../../services/product.service';
import { MeatCutService, MeatCutAvailability } from '../../../services/meat-cut.service';
import { NotificationService } from '../../../services/notification.service';
import { FormUtils } from '../../../shared/utils/form.utils';

@Component({
  selector: 'app-order-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatToolbarModule,
    MatCardModule,
    MatTableModule,
    MatExpansionModule
  ],
  templateUrl: './order-form.component.html',
  styleUrls: ['./order-form.component.scss']
})
export class OrderFormComponent implements OnInit {
  form: FormGroup;
  title: string = 'Neue Bestellung erstellen';
  isEditMode: boolean = false;
  orderId?: number;
  loading: boolean = false;
  availableProducts: Product[] = [];
  displayedColumns = ['product', 'weight', 'unitPrice', 'subtotal', 'actions'];
  selectedProduct: Product | null = null;
  meatCutAvailability: MeatCutAvailability[] = [];
  showAvailability: boolean = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private orderService: OrderService,
    private productService: ProductService,
    private meatCutService: MeatCutService,
    private notificationService: NotificationService
  ) {
    this.form = this.createForm();
  }

  ngOnInit(): void {
    this.loadProducts();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.orderId = +id;
      this.title = 'Bestellung bearbeiten';
      this.loadOrder(this.orderId);
    }

    // Watch for product selection
    this.form.get('selectedProduct')?.valueChanges.subscribe(productId => {
      const product = this.availableProducts.find(p => p.id === productId);
      this.selectedProduct = product || null;
      if (product) {
        this.form.patchValue({
          quantity: 1,  // Default to 1 kg
          unitPrice: product.price
        });
        // Load availability information for this product
        this.loadProductAvailability(product.id!);
      } else {
        this.meatCutAvailability = [];
        this.showAvailability = false;
      }
    });

    // Update subtotal when quantity or price changes
    this.form.get('quantity')?.valueChanges.subscribe(() => this.updateSubtotal());
    this.form.get('unitPrice')?.valueChanges.subscribe(() => this.updateSubtotal());
  }

  loadProducts(): void {
    this.productService.getAll().subscribe({
      next: (products: Product[]) => {
        // Show all products with a valid price (amount > 0)
        // Products without stock will be visible but disabled in the dropdown
        this.availableProducts = products.filter(p => p.price && p.price > 0);
      },
      error: (error: any) => {
        this.notificationService.error('Produkte konnten nicht geladen werden');
      }
    });
  }

  loadProductAvailability(productId: number): void {
    this.meatCutService.getAvailabilityByProduct(productId).subscribe({
      next: (availability: MeatCutAvailability[]) => {
        this.meatCutAvailability = availability;
        this.showAvailability = availability.length > 0;
      },
      error: (error: any) => {
        this.meatCutAvailability = [];
        this.showAvailability = false;
      }
    });
  }

  loadOrder(id: number): void {
    this.loading = true;
    this.orderService.getById(id).subscribe({
      next: (order: Order) => {
        this.form.patchValue({
          customerName: order.customerName,
          customerPhone: order.customerPhone,
          customerAddress: order.customerAddress
        });

        // Add existing items
        if (order.items) {
          order.items.forEach((item: OrderItem) => {
            this.orderItems.push(this.createOrderItem(item));
          });
        }

        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.error('Bestellung konnte nicht geladen werden');
        this.loading = false;
        this.goBack();
      }
    });
  }

  get orderItems(): FormArray {
    return this.form.get('items') as FormArray;
  }

  get totalAmount(): number {
    return this.orderItems.controls.reduce((sum, item) => {
      return sum + (item.get('subtotal')?.value || 0);
    }, 0);
  }

  get currentSubtotal(): number {
    const quantityInKg = this.form.get('quantity')?.value || 0;
    const pricePerKg = this.form.get('unitPrice')?.value || 0;
    // Calculate: quantity_in_kg * price_per_kg
    return quantityInKg * pricePerKg;
  }

  getTotalAvailable(): number {
    return this.meatCutAvailability.reduce((sum, item) => sum + item.availableWeight, 0);
  }

  addItem(): void {
    const quantity = this.form.get('quantity')?.value;
    const unitPrice = this.form.get('unitPrice')?.value;

    if (!this.selectedProduct || !quantity || quantity <= 0) {
      return;
    }

    const item: OrderItem = {
      product: this.selectedProduct,
      weight: quantity,  // quantity is now in kg
      unitPrice: unitPrice,
      subtotal: quantity * unitPrice  // Calculate: kg * price_per_kg
    };

    this.orderItems.push(this.createOrderItem(item));

    // Force Angular change detection by creating a new array reference
    const items = this.form.get('items') as FormArray;
    items.updateValueAndValidity({ emitEvent: true });

    // Trigger view update
    this.orderItems.controls = [...this.orderItems.controls];

    // Reset item form - reset selectedProduct first to null, then reset the component state
    this.selectedProduct = null;
    this.form.patchValue({
      selectedProduct: null,
      quantity: 1,  // Default to 1 kg
      unitPrice: 0
    }, { emitEvent: false });

    // Force update the form controls to ensure proper state reset
    this.form.get('selectedProduct')?.reset(null);
    this.form.get('quantity')?.reset(1);
    this.form.get('unitPrice')?.reset(0);
  }

  removeItem(index: number): void {
    this.orderItems.removeAt(index);

    // Force Angular change detection by creating a new array reference
    const items = this.form.get('items') as FormArray;
    items.updateValueAndValidity({ emitEvent: true });

    // Trigger view update
    this.orderItems.controls = [...this.orderItems.controls];
  }

  updateItemSubtotal(index: number): void {
    const item = this.orderItems.at(index);
    if (item) {
      const weight = item.get('weight')?.value || 0;
      const unitPrice = item.get('unitPrice')?.value || 0;
      const subtotal = weight * unitPrice;
      item.patchValue({ subtotal: subtotal });
    }
  }

  save(): void {
    if (this.form.get('customerName')?.invalid) {
      FormUtils.markAllAsTouched(this.form);
      return;
    }

    if (this.orderItems.length === 0) {
      this.notificationService.error('Bitte fügen Sie mindestens einen Artikel zur Bestellung hinzu');
      return;
    }

    this.loading = true;
    const order: Order = {
      ...(this.isEditMode && this.orderId && { id: this.orderId }),
      customerName: this.form.get('customerName')?.value,
      customerPhone: this.form.get('customerPhone')?.value,
      customerAddress: this.form.get('customerAddress')?.value,
      items: this.orderItems.value,
      totalAmount: this.totalAmount,
      status: OrderStatus.PENDING
    };

    const operation = this.isEditMode && this.orderId
      ? this.orderService.update(this.orderId, order)
      : this.orderService.create(order);

    operation.subscribe({
      next: () => {
        this.notificationService.success(
          this.isEditMode ? 'Bestellung erfolgreich aktualisiert' : 'Bestellung erfolgreich erstellt'
        );
        this.loading = false;
        this.router.navigate(['/orders']);
      },
      error: (error: any) => {
        this.notificationService.error(
          this.isEditMode ? 'Bestellung konnte nicht aktualisiert werden' : 'Bestellung konnte nicht erstellt werden'
        );
        this.loading = false;
      }
    });
  }

  goBack(): void {
    this.location.back();
  }

  getError(fieldName: string): string {
    return FormUtils.getErrorMessage(this.form, fieldName);
  }

  private updateSubtotal(): void {
    const quantity = this.form.get('quantity')?.value || 0;
    const unitPrice = this.form.get('unitPrice')?.value || 0;
    // This is just for display, actual calculation happens when adding the item
  }

  private createForm(): FormGroup {
    return this.fb.group({
      customerName: ['', [Validators.required, Validators.minLength(2)]],
      customerPhone: [''],
      customerAddress: [''],
      items: this.fb.array([]),
      // Temporary fields for adding items
      selectedProduct: [null],
      quantity: [1, [Validators.min(0.01)]],  // Default to 1 kg, min 0.01 kg
      unitPrice: [0, [Validators.min(0)]]
    });
  }

  private createOrderItem(item: OrderItem): FormGroup {
    // Calculate subtotal if not provided
    const weight = item.weight || 0;
    const unitPrice = item.unitPrice || 0;
    const subtotal = item.subtotal || (weight * unitPrice);

    return this.fb.group({
      product: [item.product],
      weight: [weight],
      unitPrice: [unitPrice],
      subtotal: [subtotal]
    });
  }
}
