package pe.dracor.sgsao.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Exporta un reporte a PDF (A4 horizontal). */
public final class ExportadorPdf {

    private ExportadorPdf() {
    }

    public static byte[] generar(ReporteTabla tabla) {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate(), 28, 28, 30, 30);
            PdfWriter.getInstance(doc, salida);
            doc.open();

            Font empresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(120, 120, 120));
            Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(24, 54, 97));
            Font sub = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(90, 90, 90));
            Font cab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
            Font cuerpo = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            doc.add(new Paragraph("DRACOR - Sistema de Gestión y Seguimiento del Avance de Obras", empresa));
            doc.add(new Paragraph(tabla.getTitulo(), titulo));
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph s = new Paragraph(tabla.getSubtitulo() + "  |  Generado el " + fecha + "  |  Total de registros: "
                    + tabla.getFilas().size(), sub);
            s.setSpacingAfter(10);
            doc.add(s);

            int n = tabla.getCabeceras().size();
            PdfPTable t = new PdfPTable(n);
            t.setWidthPercentage(100);
            t.setHeaderRows(1);
            for (String c : tabla.getCabeceras()) {
                PdfPCell cell = new PdfPCell(new Phrase(c, cab));
                cell.setBackgroundColor(new Color(24, 54, 97));
                cell.setPadding(5);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                t.addCell(cell);
            }
            boolean par = false;
            for (List<String> fila : tabla.getFilas()) {
                for (String v : fila) {
                    PdfPCell cell = new PdfPCell(new Phrase(v == null ? "" : v, cuerpo));
                    cell.setPadding(4);
                    if (par) {
                        cell.setBackgroundColor(new Color(243, 246, 251));
                    }
                    t.addCell(cell);
                }
                par = !par;
            }
            doc.add(t);
            doc.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF: " + e.getMessage(), e);
        }
    }
}
