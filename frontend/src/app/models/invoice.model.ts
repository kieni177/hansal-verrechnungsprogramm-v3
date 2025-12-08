import { Order } from './order.model';

export interface Invoice {
  id?: number;
  invoiceNumber?: string;
  order: Order;
  issueDate?: string;
  dueDate?: string;
  totalAmount?: number;
  taxRate?: number;
  taxAmount?: number;
  grandTotal?: number;
  notes?: string;
  status: InvoiceStatus;
  createdBy?: string;
  createdAt?: string;
  updatedAt?: string;
}

export enum InvoiceStatus {
  UNPAID = 'UNPAID',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}
