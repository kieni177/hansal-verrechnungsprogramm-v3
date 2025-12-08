import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';
import { NotificationService } from '../../../services/notification.service';
import { FormUtils } from '../../../shared/utils/form.utils';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatToolbarModule,
    MatCardModule
  ],
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.scss']
})
export class ProductFormComponent implements OnInit {
  form: FormGroup;
  title: string = 'Neues Produkt hinzufügen';
  isEditMode: boolean = false;
  productId?: number;
  loading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private productService: ProductService,
    private notificationService: NotificationService
  ) {
    this.form = this.createForm();
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.productId = +id;
      this.title = 'Produkt bearbeiten';
      this.loadProduct(this.productId);
    }
  }

  loadProduct(id: number): void {
    this.loading = true;
    this.productService.getById(id).subscribe({
      next: (product: Product) => {
        this.form.patchValue(product);
        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.error('Produkt konnte nicht geladen werden');
        this.loading = false;
        this.goBack();
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      FormUtils.markAllAsTouched(this.form);
      return;
    }

    this.loading = true;
    const productData: Product = {
      ...this.form.value,
      ...(this.isEditMode && { id: this.productId })
    };

    const operation = this.isEditMode
      ? this.productService.update(this.productId!, productData)
      : this.productService.create(productData);

    operation.subscribe({
      next: () => {
        this.notificationService.success(
          this.isEditMode ? 'Produkt erfolgreich aktualisiert' : 'Produkt erfolgreich erstellt'
        );
        this.loading = false;
        this.router.navigate(['/products']);
      },
      error: (error: any) => {
        this.notificationService.error(
          this.isEditMode ? 'Produkt konnte nicht aktualisiert werden' : 'Produkt konnte nicht erstellt werden'
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

  private createForm(): FormGroup {
    return this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      description: [''],
      price: [0, [Validators.required, Validators.min(0)]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]]
    });
  }
}
