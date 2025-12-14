import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatTooltipModule } from '@angular/material/tooltip';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { takeUntil } from 'rxjs';

import { BaseCrudComponent } from '../../shared/base/base-crud.component';
import { SlaughterService } from '../../services/slaughter.service';
import { NotificationService } from '../../services/notification.service';
import { Slaughter } from '../../models/slaughter.model';
import { API_MESSAGES } from '../../shared/constants/app.constants';

@Component({
  selector: 'app-slaughter',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatTooltipModule
  ],
  templateUrl: './slaughter.component.html',
  styleUrls: ['./slaughter.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'))
    ])
  ]
})
export class SlaughterComponent extends BaseCrudComponent<Slaughter> implements OnInit {
  displayedColumns = ['id', 'cowTag', 'slaughterDate', 'totalWeight', 'meatCutsCount', 'actions'];
  dataSource = new MatTableDataSource<Slaughter>([]);
  searchTerm = '';
  expandedElement: Slaughter | null = null;
  editingElement: Slaughter | null = null;
  editedSlaughter: Slaughter | null = null;

  @ViewChild(MatSort) set sort(sort: MatSort) {
    if (sort) {
      this.dataSource.sort = sort;
    }
  }

  constructor(
    private slaughterService: SlaughterService,
    private notification: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    super();
  }

  ngOnInit(): void {
    // Load data initially
    this.loadAll();

    // Reload data whenever route params change (including when navigating back)
    this.route.params
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.loadAll();
      });
  }

  applyFilter(): void {
    this.dataSource.filter = this.searchTerm.trim().toLowerCase();
  }

  loadAll(): void {
    this.setLoading(true);
    this.slaughterService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items) => {
          // Calculate total weight for each slaughter record
          this.items = items.map(item => ({
            ...item,
            totalWeight: this.calculateTotalWeight(item)
          }));
          this.dataSource.data = this.items;
          this.setLoading(false);
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.LOAD);
          this.handleError(error);
        }
      });
  }

  add(): void {
    this.router.navigate(['/slaughter/create']);
  }

  viewDetails(slaughter: Slaughter): void {
    this.router.navigate(['/slaughter', slaughter.id]);
  }

  edit(slaughter: Slaughter): void {
    this.router.navigate(['/slaughter/edit', slaughter.id]);
  }

  delete(id: number): void {
    if (confirm('Sind Sie sicher, dass Sie diesen Schlachtdatensatz löschen möchten?')) {
      this.slaughterService.delete(id)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.notification.success(API_MESSAGES.SUCCESS.DELETE);
            this.loadAll();
          },
          error: (error) => {
            this.notification.error(API_MESSAGES.ERROR.DELETE);
            console.error('Delete error:', error);
          }
        });
    }
  }

  getMeatCutsCount(slaughter: Slaughter): number {
    return slaughter.meatCuts?.length || 0;
  }

  private calculateTotalWeight(slaughter: Slaughter): number {
    if (!slaughter.meatCuts || slaughter.meatCuts.length === 0) {
      return 0;
    }
    return slaughter.meatCuts.reduce((sum, cut) => sum + (cut.totalWeight || 0), 0);
  }

  toggleExpand(slaughter: Slaughter): void {
    if (this.editingElement === slaughter) {
      return; // Don't collapse while editing
    }
    this.expandedElement = this.expandedElement === slaughter ? null : slaughter;
  }

  startEdit(slaughter: Slaughter): void {
    this.editingElement = slaughter;
    this.editedSlaughter = { ...slaughter };
    this.expandedElement = slaughter; // Ensure row is expanded
  }

  cancelEdit(): void {
    this.editingElement = null;
    this.editedSlaughter = null;
  }

  saveEdit(): void {
    if (!this.editedSlaughter || !this.editedSlaughter.id) {
      return;
    }

    this.slaughterService.update(this.editedSlaughter.id, this.editedSlaughter)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success(API_MESSAGES.SUCCESS.UPDATE);
          this.editingElement = null;
          this.editedSlaughter = null;
          this.loadAll();
        },
        error: (error) => {
          this.notification.error(API_MESSAGES.ERROR.UPDATE);
          console.error('Update error:', error);
        }
      });
  }

  isEditing(slaughter: Slaughter): boolean {
    return this.editingElement === slaughter;
  }
}
