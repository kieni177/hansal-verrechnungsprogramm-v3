import { Product } from './product.model';

export interface MeatCut {
  id?: number;
  product: Product;     // Reference to Product instead of cutType string
  productId?: number;   // Product ID for API calls
  totalWeight: number;  // weight in kg (renamed from 'weight')
  availableWeight?: number;  // Available weight for orders
  pricePerKg?: number;  // optional price per kg
}

export interface Slaughter {
  id?: number;
  cowId?: string;              // Identifier for the cow
  cowTag?: string;             // Ear tag or identifier
  slaughterDate: Date | string;
  totalWeight?: number;        // Total weight of all meat cuts
  meatCuts: MeatCut[];        // Array of different meat cuts
  notes?: string;
  createdAt?: Date | string;
}

// MeatCutType enum removed - now using Product references instead
