import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Product } from '../../../models/product.model';
import { FormUtils } from '../../../shared/utils/form.utils';

export interface ProductDialogData {
  product?: Product;
  mode: 'create' | 'edit';
}

@Component({
  selector: 'app-product-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule
  ],
  templateUrl: './product-dialog.component.html',
  styleUrls: ['./product-dialog.component.scss']
})
export class ProductDialogComponent implements OnInit {
  form: FormGroup;
  title: string;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<ProductDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProductDialogData
  ) {
    this.title = data.mode === 'create' ? 'Neues Produkt hinzufügen' : 'Produkt bearbeiten';
    this.form = this.createForm();
  }

  ngOnInit(): void {
    if (this.data.mode === 'edit' && this.data.product) {
      this.form.patchValue(this.data.product);
    }
  }

  save(): void {
    if (this.form.invalid) {
      FormUtils.markAllAsTouched(this.form);
      return;
    }

    this.dialogRef.close({
      ...this.data.product,
      ...this.form.value
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }

  getError(fieldName: string): string {
    return FormUtils.getErrorMessage(this.form, fieldName);
  }

  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]]
    });
  }
}
