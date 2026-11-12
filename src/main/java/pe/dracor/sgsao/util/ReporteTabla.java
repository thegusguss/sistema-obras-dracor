package pe.dracor.sgsao.util;

import java.util.ArrayList;
import java.util.List;

/** Tabla generica de un reporte; se muestra en pantalla y se exporta a PDF o Excel. */
public class ReporteTabla {

    private final String titulo;
    private final String subtitulo;
    private final List<String> cabeceras;
    private final List<List<String>> filas = new ArrayList<>();

    public ReporteTabla(String titulo, String subtitulo, List<String> cabeceras) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.cabeceras = cabeceras;
    }

    public void agregar(List<String> fila) {
        filas.add(fila);
    }

    public String getTitulo() {
        return titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public List<String> getCabeceras() {
        return cabeceras;
    }

    public List<List<String>> getFilas() {
        return filas;
    }
}
