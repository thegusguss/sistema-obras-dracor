package pe.dracor.sgsao.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/** Exporta un reporte a Excel (.xlsx). */
public final class ExportadorExcel {

    private ExportadorExcel() {
    }

    public static byte[] generar(ReporteTabla tabla) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");

            Font negrita = libro.createFont();
            negrita.setBold(true);
            negrita.setFontHeightInPoints((short) 14);
            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(negrita);

            Font blanca = libro.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());
            CellStyle estiloCab = libro.createCellStyle();
            estiloCab.setFont(blanca);
            estiloCab.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            estiloCab.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloCab.setAlignment(HorizontalAlignment.CENTER);
            estiloCab.setBorderBottom(BorderStyle.THIN);

            CellStyle estiloCelda = libro.createCellStyle();
            estiloCelda.setBorderBottom(BorderStyle.HAIR);

            Row r0 = hoja.createRow(0);
            Cell c0 = r0.createCell(0);
            c0.setCellValue("DRACOR - " + tabla.getTitulo());
            c0.setCellStyle(estiloTitulo);
            hoja.createRow(1).createCell(0).setCellValue(tabla.getSubtitulo());

            Row cab = hoja.createRow(3);
            List<String> cabeceras = tabla.getCabeceras();
            for (int i = 0; i < cabeceras.size(); i++) {
                Cell c = cab.createCell(i);
                c.setCellValue(cabeceras.get(i));
                c.setCellStyle(estiloCab);
            }
            int fila = 4;
            for (List<String> datos : tabla.getFilas()) {
                Row r = hoja.createRow(fila++);
                for (int i = 0; i < datos.size(); i++) {
                    Cell c = r.createCell(i);
                    c.setCellValue(datos.get(i) == null ? "" : datos.get(i));
                    c.setCellStyle(estiloCelda);
                }
            }
            for (int i = 0; i < cabeceras.size(); i++) {
                hoja.autoSizeColumn(i);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo generar el Excel: " + e.getMessage(), e);
        }
    }
}
