import { Product } from './product.model';

export interface OrderItem {
  id?: number;
  product?: Product;
  meatCut?: any;
  quantity?: number;
  weight?: number;
  unitPrice: number;
  subtotal?: number;
  itemName?: string;
}

export interface Order {
  id?: number;
  customerName: string;
  customerPhone?: string;
  customerAddress?: string;
  items: OrderItem[];
  totalAmount?: number;
  status: OrderStatus;
  orderDate?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}
