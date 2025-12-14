package com.hansal.verrechnungsprogramm.service;

import com.hansal.verrechnungsprogramm.model.*;
import com.hansal.verrechnungsprogramm.repository.InvoiceRepository;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderService orderService;

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with id: " + id));
    }

    public Invoice getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new RuntimeException("Invoice not found with number: " + invoiceNumber));
    }

    public Optional<Invoice> getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.findByOrderId(orderId);
    }

    public Invoice createInvoiceFromOrder(Long orderId) {
        Order order = orderService.getOrderById(orderId);

        Invoice invoice = new Invoice();
        invoice.setOrder(order);
        invoice.setTotalAmount(order.getTotalAmount());
        invoice.setTaxRate(BigDecimal.valueOf(10)); // 10% USt for agricultural products
        invoice.setCreatedBy("Administrator"); // TODO: Get from authenticated user context
        invoice.calculateTotals();

        return invoiceRepository.save(invoice);
    }

    public Invoice updateInvoice(Long id, Invoice invoiceDetails) {
        Invoice invoice = getInvoiceById(id);
        invoice.setIssueDate(invoiceDetails.getIssueDate());
        invoice.setDueDate(invoiceDetails.getDueDate());
        invoice.setTaxRate(invoiceDetails.getTaxRate());
        invoice.setNotes(invoiceDetails.getNotes());
        invoice.setStatus(invoiceDetails.getStatus());
        invoice.calculateTotals();
        return invoiceRepository.save(invoice);
    }

    public void deleteInvoice(Long id) {
        Invoice invoice = getInvoiceById(id);
        invoiceRepository.delete(invoice);
    }

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = getInvoiceById(invoiceId);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Set margins
            document.setMargins(40, 50, 40, 50);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            // Header with logo on left and company info on right
            Table headerTable = new Table(new float[]{1, 1.5f});
            headerTable.setWidth(UnitValue.createPercentValue(100));
            headerTable.setBorder(Border.NO_BORDER);

            // Logo cell
            Cell logoCell = new Cell();
            logoCell.setBorder(Border.NO_BORDER);
            logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            try {
                InputStream logoStream = getClass().getResourceAsStream("/static/logo.jpg");
                if (logoStream != null) {
                    byte[] logoBytes = logoStream.readAllBytes();
                    logoStream.close();
                    Image logo = new Image(ImageDataFactory.create(logoBytes));
                    logo.scaleToFit(120, 120);
                    logoCell.add(logo);
                }
            } catch (Exception e) {
                System.err.println("Could not load logo: " + e.getMessage());
            }
            headerTable.addCell(logoCell);

            // Company info cell (italic style)
            Cell companyCell = new Cell();
            companyCell.setBorder(Border.NO_BORDER);
            companyCell.setTextAlignment(TextAlignment.LEFT);
            companyCell.setPaddingLeft(20);
            companyCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
            companyCell.add(new Paragraph("Biohof Hansal")
                    .setFontSize(14)
                    .setBold()
                    .setItalic()
                    .setMarginBottom(2));
            companyCell.add(new Paragraph("Tanja und Andreas Kienegger")
                    .setFontSize(10)
                    .setItalic()
                    .setMarginBottom(1));
            companyCell.add(new Paragraph("Hohenau 17/2")
                    .setFontSize(10)
                    .setItalic()
                    .setMarginBottom(1));
            companyCell.add(new Paragraph("A-8241 Dechantskirchen")
                    .setFontSize(10)
                    .setItalic()
                    .setMarginBottom(6));
            companyCell.add(new Paragraph("\u260E 0650 8831093")
                    .setFontSize(10)
                    .setItalic()
                    .setMarginBottom(1));
            companyCell.add(new Paragraph("\u2709 info@biohofhansal.at")
                    .setFontSize(10)
                    .setItalic());
            headerTable.addCell(companyCell);

            document.add(headerTable);

            // Separator line (single line)
            document.add(new Paragraph("_".repeat(85))
                    .setFontSize(10)
                    .setMarginTop(10)
                    .setMarginBottom(20));

            // Title
            document.add(new Paragraph("Verkaufsbeleg für BIO-Rindfleisch")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(20));

            // Customer and date info
            document.add(new Paragraph("Verkauf an: " + invoice.getOrder().getCustomerName())
                    .setFontSize(11)
                    .setMarginBottom(2));
            document.add(new Paragraph("Datum: ")
                    .setFontSize(11)
                    .add(new com.itextpdf.layout.element.Text(invoice.getIssueDate().format(dateFormatter)).setItalic())
                    .setMarginBottom(15));

            // Belegnummer (Invoice number) - format as DDMYY based on date
            String belegnummer = generateBelegnummer(invoice);
            document.add(new Paragraph()
                    .add(new com.itextpdf.layout.element.Text("Belegnummer: ").setBold())
                    .add(new com.itextpdf.layout.element.Text(belegnummer))
                    .setFontSize(11)
                    .setMarginBottom(15));

            // Items table
            float[] columnWidths = {3, 1.5f, 1.5f, 1.5f};
            Table table = new Table(columnWidths);
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginBottom(20);

            // Header row
            addSimpleTableHeader(table, "Bezeichnung", TextAlignment.LEFT);
            addSimpleTableHeader(table, "Menge", TextAlignment.CENTER);
            addSimpleTableHeader(table, "Preis/Einheit", TextAlignment.CENTER);
            addSimpleTableHeader(table, "Summe", TextAlignment.RIGHT);

            // Items
            Order order = invoice.getOrder();
            for (OrderItem item : order.getItems()) {
                // Product name
                Cell productCell = new Cell();
                productCell.add(new Paragraph(item.getItemName()).setFontSize(11));
                productCell.setBorder(Border.NO_BORDER);
                productCell.setPaddingTop(8);
                productCell.setPaddingBottom(8);
                table.addCell(productCell);

                // Quantity/Weight - all amounts are in kg
                String quantityStr;
                String unitPriceStr;
                if (item.getMeatCut() != null && item.getWeight() != null) {
                    // For meat cuts: weight is already in kg
                    quantityStr = String.format("%.2f kg", item.getWeight()).replace(".", ",");
                } else if (item.getWeight() != null) {
                    // For products with weight: show in kg
                    quantityStr = String.format("%.2f kg", item.getWeight()).replace(".", ",");
                } else {
                    // Fallback: convert quantity to kg (assuming quantity might be in grams)
                    BigDecimal weightInKg = BigDecimal.valueOf(item.getQuantity()).divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP);
                    quantityStr = String.format("%.2f kg", weightInKg).replace(".", ",");
                }
                // Unit price is per kg
                unitPriceStr = String.format("%.0f €", item.getUnitPrice());

                Cell qtyCell = new Cell();
                qtyCell.add(new Paragraph(quantityStr).setFontSize(11).setTextAlignment(TextAlignment.CENTER));
                qtyCell.setBorder(Border.NO_BORDER);
                qtyCell.setPaddingTop(8);
                qtyCell.setPaddingBottom(8);
                table.addCell(qtyCell);

                // Unit price
                Cell priceCell = new Cell();
                priceCell.add(new Paragraph(unitPriceStr).setFontSize(11).setTextAlignment(TextAlignment.CENTER));
                priceCell.setBorder(Border.NO_BORDER);
                priceCell.setPaddingTop(8);
                priceCell.setPaddingBottom(8);
                table.addCell(priceCell);

                // Subtotal
                Cell subtotalCell = new Cell();
                String subtotalStr = formatEuroAmount(item.getSubtotal());
                subtotalCell.add(new Paragraph(subtotalStr)
                        .setFontSize(11)
                        .setTextAlignment(TextAlignment.RIGHT));
                subtotalCell.setBorder(Border.NO_BORDER);
                subtotalCell.setPaddingTop(8);
                subtotalCell.setPaddingBottom(8);
                table.addCell(subtotalCell);
            }

            document.add(table);

            // Total amount (right-aligned, double underlined) - use totalAmount since prices include tax
            String totalStr = formatEuroAmountWithSpace(invoice.getTotalAmount());
            Paragraph totalParagraph = new Paragraph(totalStr)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setUnderline(1, -1)    // First underline
                    .setUnderline(1, -3)    // Second underline (offset further down)
                    .setMarginBottom(3);
            document.add(totalParagraph);

            // Tax info
            document.add(new Paragraph("(inkl. 10% USt)")
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(20));

            // Payment received message
            document.add(new Paragraph("Betrag dankend erhalten!")
                    .setFontSize(11)
                    .setBold()
                    .setItalic()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginBottom(50));

            // Footer with bank details
            Table footerTable = new Table(1);
            footerTable.setWidth(UnitValue.createPercentValue(100));
            footerTable.setBorder(Border.NO_BORDER);
            footerTable.setFixedPosition(50, 40, pdf.getDefaultPageSize().getWidth() - 100);

            Cell footerCell = new Cell();
            footerCell.setBorder(Border.NO_BORDER);
            footerCell.add(new Paragraph("Andreas und Tanja Kienegger")
                    .setFontSize(9)
                    .setMarginBottom(1));
            footerCell.add(new Paragraph("Raiffeisenbank Wechselland")
                    .setFontSize(9)
                    .setMarginBottom(1));
            footerCell.add(new Paragraph("IBAN: AT24 3802 3000 0120 0369")
                    .setFontSize(9)
                    .setMarginBottom(1));
            footerCell.add(new Paragraph("Betriebsnummer: 3139310")
                    .setFontSize(9));
            footerTable.addCell(footerCell);

            document.add(footerTable);

            // Page number
            int totalPages = pdf.getNumberOfPages();
            Paragraph pageNumber = new Paragraph("Seite 1/" + totalPages)
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER);
            pageNumber.setFixedPosition(
                    (pdf.getDefaultPageSize().getWidth() - 50) / 2,
                    25,
                    50);
            document.add(pageNumber);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    public byte[] generateCombinedPdf(List<Long> invoiceIds) {
        List<Invoice> invoices = invoiceIds.stream()
                .map(this::getInvoiceById)
                .toList();

        if (invoices.isEmpty()) {
            throw new RuntimeException("No invoices found for the given IDs");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(40, 50, 40, 50);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (int i = 0; i < invoices.size(); i++) {
                Invoice invoice = invoices.get(i);

                // Add page break before each invoice (except the first one)
                if (i > 0) {
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }

                // Generate invoice content (same as single PDF)
                addInvoiceContent(document, pdf, invoice, dateFormatter);
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating combined PDF: " + e.getMessage(), e);
        }
    }

    private void addInvoiceContent(Document document, PdfDocument pdf, Invoice invoice, DateTimeFormatter dateFormatter) {
        // Header with logo on left and company info on right
        Table headerTable = new Table(new float[]{1, 1.5f});
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(Border.NO_BORDER);

        // Logo cell
        Cell logoCell = new Cell();
        logoCell.setBorder(Border.NO_BORDER);
        logoCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        try {
            InputStream logoStream = getClass().getResourceAsStream("/static/logo.jpg");
            if (logoStream != null) {
                byte[] logoBytes = logoStream.readAllBytes();
                logoStream.close();
                Image logo = new Image(ImageDataFactory.create(logoBytes));
                logo.scaleToFit(120, 120);
                logoCell.add(logo);
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
        headerTable.addCell(logoCell);

        // Company info cell (italic style)
        Cell companyCell = new Cell();
        companyCell.setBorder(Border.NO_BORDER);
        companyCell.setTextAlignment(TextAlignment.LEFT);
        companyCell.setPaddingLeft(20);
        companyCell.setVerticalAlignment(VerticalAlignment.MIDDLE);
        companyCell.add(new Paragraph("Biohof Hansal")
                .setFontSize(14)
                .setBold()
                .setItalic()
                .setMarginBottom(2));
        companyCell.add(new Paragraph("Tanja und Andreas Kienegger")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(1));
        companyCell.add(new Paragraph("Hohenau 17/2")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(1));
        companyCell.add(new Paragraph("A-8241 Dechantskirchen")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(6));
        companyCell.add(new Paragraph("\u260E 0650 8831093")
                .setFontSize(10)
                .setItalic()
                .setMarginBottom(1));
        companyCell.add(new Paragraph("\u2709 info@biohofhansal.at")
                .setFontSize(10)
                .setItalic());
        headerTable.addCell(companyCell);

        document.add(headerTable);

        // Separator line
        document.add(new Paragraph("_".repeat(85))
                .setFontSize(10)
                .setMarginTop(10)
                .setMarginBottom(20));

        // Title
        document.add(new Paragraph("Verkaufsbeleg für BIO-Rindfleisch")
                .setFontSize(14)
                .setBold()
                .setMarginBottom(20));

        // Customer and date info
        document.add(new Paragraph("Verkauf an: " + invoice.getOrder().getCustomerName())
                .setFontSize(11)
                .setMarginBottom(2));
        document.add(new Paragraph("Datum: ")
                .setFontSize(11)
                .add(new com.itextpdf.layout.element.Text(invoice.getIssueDate().format(dateFormatter)).setItalic())
                .setMarginBottom(15));

        // Belegnummer
        String belegnummer = generateBelegnummer(invoice);
        document.add(new Paragraph()
                .add(new com.itextpdf.layout.element.Text("Belegnummer: ").setBold())
                .add(new com.itextpdf.layout.element.Text(belegnummer))
                .setFontSize(11)
                .setMarginBottom(15));

        // Items table
        float[] columnWidths = {3, 1.5f, 1.5f, 1.5f};
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        // Header row
        addSimpleTableHeader(table, "Bezeichnung", TextAlignment.LEFT);
        addSimpleTableHeader(table, "Menge", TextAlignment.CENTER);
        addSimpleTableHeader(table, "Preis/Einheit", TextAlignment.CENTER);
        addSimpleTableHeader(table, "Summe", TextAlignment.RIGHT);

        // Items
        Order order = invoice.getOrder();
        for (OrderItem item : order.getItems()) {
            // Product name
            Cell productCell = new Cell();
            productCell.add(new Paragraph(item.getItemName()).setFontSize(11));
            productCell.setBorder(Border.NO_BORDER);
            productCell.setPaddingTop(8);
            productCell.setPaddingBottom(8);
            table.addCell(productCell);

            // Quantity/Weight
            String quantityStr;
            if (item.getMeatCut() != null && item.getWeight() != null) {
                quantityStr = String.format("%.2f kg", item.getWeight()).replace(".", ",");
            } else if (item.getWeight() != null) {
                quantityStr = String.format("%.2f kg", item.getWeight()).replace(".", ",");
            } else {
                BigDecimal weightInKg = BigDecimal.valueOf(item.getQuantity()).divide(BigDecimal.valueOf(1000), 2, BigDecimal.ROUND_HALF_UP);
                quantityStr = String.format("%.2f kg", weightInKg).replace(".", ",");
            }
            String unitPriceStr = String.format("%.0f €", item.getUnitPrice());

            Cell qtyCell = new Cell();
            qtyCell.add(new Paragraph(quantityStr).setFontSize(11).setTextAlignment(TextAlignment.CENTER));
            qtyCell.setBorder(Border.NO_BORDER);
            qtyCell.setPaddingTop(8);
            qtyCell.setPaddingBottom(8);
            table.addCell(qtyCell);

            Cell priceCell = new Cell();
            priceCell.add(new Paragraph(unitPriceStr).setFontSize(11).setTextAlignment(TextAlignment.CENTER));
            priceCell.setBorder(Border.NO_BORDER);
            priceCell.setPaddingTop(8);
            priceCell.setPaddingBottom(8);
            table.addCell(priceCell);

            Cell subtotalCell = new Cell();
            String subtotalStr = formatEuroAmount(item.getSubtotal());
            subtotalCell.add(new Paragraph(subtotalStr)
                    .setFontSize(11)
                    .setTextAlignment(TextAlignment.RIGHT));
            subtotalCell.setBorder(Border.NO_BORDER);
            subtotalCell.setPaddingTop(8);
            subtotalCell.setPaddingBottom(8);
            table.addCell(subtotalCell);
        }

        document.add(table);

        // Total amount
        String totalStr = formatEuroAmountWithSpace(invoice.getTotalAmount());
        Paragraph totalParagraph = new Paragraph(totalStr)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT)
                .setUnderline(1, -1)
                .setUnderline(1, -3)
                .setMarginBottom(3);
        document.add(totalParagraph);

        // Tax info
        document.add(new Paragraph("(inkl. 10% USt)")
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(20));

        // Payment received message
        document.add(new Paragraph("Betrag dankend erhalten!")
                .setFontSize(11)
                .setBold()
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(50));

        // Footer with bank details
        Table footerTable = new Table(1);
        footerTable.setWidth(UnitValue.createPercentValue(100));
        footerTable.setBorder(Border.NO_BORDER);
        footerTable.setFixedPosition(50, 40, pdf.getDefaultPageSize().getWidth() - 100);

        Cell footerCell = new Cell();
        footerCell.setBorder(Border.NO_BORDER);
        footerCell.add(new Paragraph("Andreas und Tanja Kienegger")
                .setFontSize(9)
                .setMarginBottom(1));
        footerCell.add(new Paragraph("Raiffeisenbank Wechselland")
                .setFontSize(9)
                .setMarginBottom(1));
        footerCell.add(new Paragraph("IBAN: AT24 3802 3000 0120 0369")
                .setFontSize(9)
                .setMarginBottom(1));
        footerCell.add(new Paragraph("Betriebsnummer: 3139310")
                .setFontSize(9));
        footerTable.addCell(footerCell);

        document.add(footerTable);
    }

    private String generateBelegnummer(Invoice invoice) {
        // Generate Belegnummer in format DDMYY-XXXX (date + sequential number)
        java.time.LocalDate date = invoice.getIssueDate();
        String datePart = String.format("%d%d%02d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear() % 100);
        // Use invoice ID as the sequential number
        String sequentialPart = String.format("%04d", invoice.getId() != null ? invoice.getId() : 0);
        return datePart + "-" + sequentialPart;
    }

    private String formatEuroAmount(BigDecimal amount) {
        // Format amount with comma as decimal separator and space before € (German format)
        if (amount == null) return "0 €";
        String formatted = String.format("%.2f", amount);
        // Replace dot with comma for German locale
        return formatted.replace(".", ",") + " €";
    }

    private String formatEuroAmountWithSpace(BigDecimal amount) {
        // Format amount with comma as decimal separator and space before € (German format)
        if (amount == null) return "0 €";
        String formatted = String.format("%.2f", amount);
        // Replace dot with comma for German locale
        return formatted.replace(".", ",") + " €";
    }

    private void addSimpleTableHeader(Table table, String text, TextAlignment alignment) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
                .setBold()
                .setFontSize(11)
                .setTextAlignment(alignment));
        cell.setBorder(Border.NO_BORDER);
        cell.setBorderBottom(new SolidBorder(ColorConstants.BLACK, 0.5f));
        cell.setPaddingBottom(5);
        table.addHeaderCell(cell);
    }

    private void addDetailRow(Table table, String label, String value) {
        Cell labelCell = new Cell();
        labelCell.setBorder(Border.NO_BORDER);
        labelCell.add(new Paragraph(label)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        valueCell.setBorder(Border.NO_BORDER);
        valueCell.add(new Paragraph(value)
                .setFontSize(8)
                .setBold()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginBottom(2));
        table.addCell(valueCell);
    }

    private void addTableHeader(Table table, String text, DeviceRgb color) {
        Cell cell = new Cell();
        cell.add(new Paragraph(text)
                .setBold()
                .setFontSize(9)
                .setFontColor(ColorConstants.WHITE));
        cell.setBackgroundColor(color);
        cell.setPadding(6);
        cell.setTextAlignment(TextAlignment.CENTER);
        cell.setBorder(new SolidBorder(color, 0.5f));
        table.addHeaderCell(cell);
    }

    private void addTotalRow(Table table, String label, String value, com.itextpdf.kernel.colors.Color bgColor, boolean isBold) {
        Cell labelCell = new Cell();
        Paragraph labelPara = new Paragraph(label)
                .setFontSize(isBold ? 10 : 9)
                .setTextAlignment(TextAlignment.RIGHT);
        if (isBold) {
            labelPara.setBold();
        }
        labelCell.add(labelPara);
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(5);
        labelCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(labelCell);

        Cell valueCell = new Cell();
        Paragraph valuePara = new Paragraph(value)
                .setFontSize(isBold ? 10 : 9)
                .setTextAlignment(TextAlignment.RIGHT);
        if (isBold) {
            valuePara.setBold();
        }
        valueCell.add(valuePara);
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(5);
        valueCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
        table.addCell(valueCell);
    }
}
