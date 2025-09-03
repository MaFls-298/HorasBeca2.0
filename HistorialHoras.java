/**
 * Autor: Estefanie 251610
 * Programa: Horas Beca
 * Fecha de creación: 31/08/2025
 */

public class HistorialHoras {
    public String correoAlumno;
    public String nombreActividad;
    public int horasAsignadas;
    public String encargado;
    public String fecha;

    public HistorialHoras(String correoAlumno, String nombreActividad, int horasAsignadas, String encargado,
            String fecha) {
        this.correoAlumno = correoAlumno;
        this.nombreActividad = nombreActividad;
        this.horasAsignadas = horasAsignadas;
        this.encargado = encargado;
        this.fecha = fecha;
    }
}