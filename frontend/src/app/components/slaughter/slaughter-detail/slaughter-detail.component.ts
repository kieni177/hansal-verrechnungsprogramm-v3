import { Component, OnInit } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { Slaughter } from '../../../models/slaughter.model';
import { SlaughterService } from '../../../services/slaughter.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-slaughter-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatChipsModule,
    MatTableModule
  ],
  templateUrl: './slaughter-detail.component.html',
  styleUrls: ['./slaughter-detail.component.scss']
})
export class SlaughterDetailComponent implements OnInit {
  slaughter?: Slaughter;
  loading: boolean = true;
  slaughterId?: number;
  displayedColumns = ['cutType', 'weight', 'pricePerKg'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    private slaughterService: SlaughterService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.slaughterId = +id;
      this.loadSlaughter(this.slaughterId);
    }
  }

  loadSlaughter(id: number): void {
    this.loading = true;
    this.slaughterService.getById(id).subscribe({
      next: (slaughter: Slaughter) => {
        this.slaughter = {
          ...slaughter,
          totalWeight: this.calculateTotalWeight(slaughter)
        };
        this.loading = false;
      },
      error: (error: any) => {
        this.notificationService.error('Failed to load slaughter record');
        this.loading = false;
        this.goBack();
      }
    });
  }

  calculateTotalWeight(slaughter: Slaughter): number {
    if (!slaughter.meatCuts || slaughter.meatCuts.length === 0) {
      return 0;
    }
    return slaughter.meatCuts.reduce((sum, cut) => sum + (cut.totalWeight || 0), 0);
  }

  getMeatCutsCount(): number {
    return this.slaughter?.meatCuts?.length || 0;
  }

  editSlaughter(): void {
    this.router.navigate(['/slaughter/edit', this.slaughterId]);
  }

  goBack(): void {
    this.location.back();
  }
}
