import { Directive, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';

@Directive()
export abstract class BaseCrudComponent<T> implements OnDestroy {
  protected destroy$ = new Subject<void>();
  items: T[] = [];
  isLoading = false;

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  protected setLoading(loading: boolean): void {
    this.isLoading = loading;
  }

  protected handleError(error: any, customMessage?: string): void {
    console.error(customMessage || 'Error:', error);
    this.setLoading(false);
  }
}
