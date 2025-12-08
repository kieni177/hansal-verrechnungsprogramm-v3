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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { Slaughter, MeatCut } from '../../../models/slaughter.model';
import { Product } from '../../../models/product.model';
import { SlaughterService } from '../../../services/slaughter.service';
import { ProductService } from '../../../services/product.service';
import { NotificationService } from '../../../services/notification.service';
import { FormUtils } from '../../../shared/utils/form.utils';

@Component({
  selector: 'app-slaughter-form',
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
    MatDatepickerModule,
    MatNativeDateModule
  ],
  templateUrl: './slaughter-form.component.html',
  styleUrls: ['./slaughter-form.component.scss']
})
export class SlaughterFormComponent implements OnInit {
  form: FormGroup;
  title: string = 'Schlachtung erfassen';
  isEditMode: boolean = false;
  slaughterId?: number;
  loading: boolean = false;
  loadingProducts: boolean = false;
  displayedColumns = ['product', 'weight', 'pricePerKg', 'actions'];
  products: Product[] = [];
  selectedProduct: Product | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private slaughterService: SlaughterService,
    private productService: ProductService,
    private notificationService: NotificationService
  ) {
    this.form = this.createForm();
  }

  ngOnInit(): void {
    this.loadProducts();
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.slaughterId = +id;
      this.title = 'Schlachtdatensatz bearbeiten';
      this.loadSlaughter(this.slaughterId);
    }
  }

  loadProducts(): void {
    this.loadingProducts = true;
    this.productService.getAll().subscribe({
      next: (products) => {
        this.products = products;
        this.loadingProducts = false;
      },
      error: (error) => {
        this.notificationService.error('Fehler beim Laden der Produkte');
        this.loadingProducts = false;
      }
    });
  }

  loadSlaughter(id: number): void {
    this.loading = true;
    this.slaughterService.getById(id).subscribe({
      next: (slaughter: Slaughter) => {
        this.form.patchValue({
          cowTag: slaughter.cowTag,
          cowId: slaughter.cowId,
          slaughterDate: slaughter.slaughterDate,
          notes: slaughter.notes
        });

        // Add existing meat cuts - convert kg to grams
        if (slaughter.meatCuts) {
          slaughter.meatCuts.forEach((cut: MeatCut) => {
            const cutInGrams = {
              ...cut,
              totalWeight: (cut.totalWeight || 0) * 1000,  // Convert kg to grams
              availableWeight: (cut.availableWeight || 0) * 1000  // Convert kg to grams
            };
            this.meatCuts.push(this.createMeatCutFormGroup(cutInGrams));
          });
        }

        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.error('Failed to load slaughter record');
        this.loading = false;
        this.goBack();
      }
    });
  }

  get meatCuts(): FormArray {
    return this.form.get('meatCuts') as FormArray;
  }

  get totalWeight(): number {
    return this.meatCuts.controls.reduce((sum, cut) => {
      return sum + (cut.get('totalWeight')?.value || 0);
    }, 0);
  }

  onProductChange(): void {
    const productId = this.form.get('selectedProductId')?.value;
    this.selectedProduct = this.products.find(p => p.id === productId) || null;

    // Auto-fill price if product has one
    if (this.selectedProduct && this.selectedProduct.price) {
      this.form.patchValue({
        cutPricePerKg: this.selectedProduct.price
      });
    }
  }

  addMeatCut(): void {
    const productId = this.form.get('selectedProductId')?.value;
    const weight = this.form.get('cutWeight')?.value;
    const pricePerKg = this.form.get('cutPricePerKg')?.value;

    if (!this.selectedProduct || !weight || weight <= 0) {
      this.notificationService.error('Bitte wählen Sie ein Produkt aus und geben Sie ein gültiges Gewicht ein');
      return;
    }

    const meatCut: MeatCut = {
      product: this.selectedProduct,
      productId: this.selectedProduct.id,
      totalWeight: weight,
      availableWeight: weight,
      pricePerKg: pricePerKg || undefined
    };

    this.meatCuts.push(this.createMeatCutFormGroup(meatCut));

    // Reset the add meat cut form - reset selectedProduct first to null, then reset the component state
    this.selectedProduct = null;
    this.form.patchValue({
      selectedProductId: null,
      cutWeight: 0,
      cutPricePerKg: 0
    }, { emitEvent: false });

    // Force update the form controls to ensure proper state reset
    this.form.get('selectedProductId')?.reset(null);
    this.form.get('cutWeight')?.reset(0);
    this.form.get('cutPricePerKg')?.reset(0);
  }

  removeMeatCut(index: number): void {
    this.meatCuts.removeAt(index);
  }

  save(): void {
    if (this.form.get('cowTag')?.invalid) {
      FormUtils.markAllAsTouched(this.form);
      return;
    }

    if (this.meatCuts.length === 0) {
      this.notificationService.error('Please add at least one meat cut');
      return;
    }

    this.loading = true;

    // Convert grams to kg for backend
    const meatCutsInKg = this.meatCuts.value.map((cut: any) => ({
      ...cut,
      totalWeight: cut.totalWeight / 1000,  // Convert grams to kg
      availableWeight: cut.availableWeight / 1000  // Convert grams to kg
    }));

    const slaughter: Slaughter = {
      ...(this.isEditMode && this.slaughterId && { id: this.slaughterId }),
      cowTag: this.form.get('cowTag')?.value,
      cowId: this.form.get('cowId')?.value,
      slaughterDate: this.form.get('slaughterDate')?.value,
      notes: this.form.get('notes')?.value,
      meatCuts: meatCutsInKg,
      totalWeight: this.totalWeight / 1000  // Convert grams to kg
    };

    const operation = this.isEditMode && this.slaughterId
      ? this.slaughterService.update(this.slaughterId, slaughter)
      : this.slaughterService.create(slaughter);

    operation.subscribe({
      next: () => {
        this.notificationService.success(
          this.isEditMode ? 'Slaughter record updated successfully' : 'Slaughter record created successfully'
        );
        this.loading = false;
        this.router.navigate(['/slaughter']);
      },
      error: (error: any) => {
        this.notificationService.error(
          this.isEditMode ? 'Failed to update slaughter record' : 'Failed to create slaughter record'
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
      cowTag: ['', [Validators.required]],
      cowId: [''],
      slaughterDate: [new Date(), [Validators.required]],
      notes: [''],
      meatCuts: this.fb.array([]),
      // Temporary fields for adding meat cuts
      selectedProductId: [null],
      cutWeight: [0, [Validators.min(0)]],
      cutPricePerKg: [0, [Validators.min(0)]]
    });
  }

  private createMeatCutFormGroup(cut: MeatCut): FormGroup {
    return this.fb.group({
      id: [cut.id],
      product: [cut.product],
      productId: [cut.productId || cut.product?.id],
      totalWeight: [cut.totalWeight],
      availableWeight: [cut.availableWeight],
      pricePerKg: [cut.pricePerKg]
    });
  }
}
