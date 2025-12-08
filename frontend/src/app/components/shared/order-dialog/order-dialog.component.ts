import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormArray } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { Order, OrderItem, OrderStatus } from '../../../models/order.model';
import { Product } from '../../../models/product.model';
import { FormUtils } from '../../../shared/utils/form.utils';

export interface OrderDialogData {
  order?: Order;
  mode: 'create' | 'edit';
  availableProducts: Product[];
}

@Component({
  selector: 'app-order-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatTableModule
  ],
  templateUrl: './order-dialog.component.html',
  styleUrls: ['./order-dialog.component.scss']
})
export class OrderDialogComponent implements OnInit {
  form: FormGroup;
  title: string;
  displayedColumns = ['product', 'weight', 'unitPrice', 'subtotal', 'actions'];
  selectedProduct: Product | null = null;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<OrderDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: OrderDialogData
  ) {
    this.title = data.mode === 'create' ? 'Neue Bestellung erstellen' : 'Bestellung bearbeiten';
    this.form = this.createForm();
  }

  ngOnInit(): void {
    if (this.data.mode === 'edit' && this.data.order) {
      this.form.patchValue({
        customerName: this.data.order.customerName,
        customerPhone: this.data.order.customerPhone,
        customerAddress: this.data.order.customerAddress
      });

      // Add existing items
      if (this.data.order.items) {
        this.data.order.items.forEach(item => {
          this.orderItems.push(this.createOrderItem(item));
        });
      }
    }

    // Watch for product selection
    this.form.get('selectedProduct')?.valueChanges.subscribe(productId => {
      const product = this.data.availableProducts.find(p => p.id === productId);
      this.selectedProduct = product || null;
      if (product) {
        this.form.patchValue({
          quantity: 1,
          unitPrice: product.price
        });
      }
    });

    // Update subtotal when quantity or price changes
    this.form.get('quantity')?.valueChanges.subscribe(() => this.updateSubtotal());
    this.form.get('unitPrice')?.valueChanges.subscribe(() => this.updateSubtotal());
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
    // Calculate: kg * price_per_kg
    return quantityInKg * pricePerKg;
  }

  addItem(): void {
    const quantity = this.form.get('quantity')?.value;
    const unitPrice = this.form.get('unitPrice')?.value;

    if (!this.selectedProduct || !quantity || quantity <= 0) {
      return;
    }

    const item: OrderItem = {
      product: this.selectedProduct,
      weight: quantity,  // quantity is in kg
      unitPrice: unitPrice,
      subtotal: quantity * unitPrice  // kg * price_per_kg
    };

    this.orderItems.push(this.createOrderItem(item));

    // Reset item form
    this.form.patchValue({
      selectedProduct: null,
      quantity: 1,
      unitPrice: 0
    });
    this.selectedProduct = null;
  }

  removeItem(index: number): void {
    this.orderItems.removeAt(index);
  }

  save(): void {
    if (this.form.get('customerName')?.invalid) {
      FormUtils.markAllAsTouched(this.form);
      return;
    }

    if (this.orderItems.length === 0) {
      return;
    }

    const order: Order = {
      ...this.data.order,
      customerName: this.form.get('customerName')?.value,
      customerPhone: this.form.get('customerPhone')?.value,
      customerAddress: this.form.get('customerAddress')?.value,
      items: this.orderItems.value,
      totalAmount: this.totalAmount,
      status: this.data.order?.status || OrderStatus.PENDING
    };

    this.dialogRef.close(order);
  }

  cancel(): void {
    this.dialogRef.close();
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
      quantity: [1, [Validators.min(1)]],
      unitPrice: [0, [Validators.min(0)]]
    });
  }

  private createOrderItem(item: OrderItem): FormGroup {
    return this.fb.group({
      product: [item.product],
      weight: [item.weight],
      unitPrice: [item.unitPrice],
      subtotal: [item.subtotal]
    });
  }
}
