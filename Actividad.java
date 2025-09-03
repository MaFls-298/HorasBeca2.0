/**
 * Autor: Estefanie 251610
 * Programa: Horas Beca
 * Fecha de creación: 31/08/2025
 */

public class Actividad {
    public String nombre;
    public String descripcion;
    public int cupoMaximo;
    public int cupoActual;

    public Actividad(String nombre, String descripcion, int cupoMaximo, int cupoActual) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.cupoMaximo = cupoMaximo;
        this.cupoActual = 0;
    }

    public boolean tieneCupoDisponible() {
        return cupoActual < cupoMaximo;
    }

    public void registrarAlumno() {
        if (tieneCupoDisponible()) {
            cupoActual++;
        }
    }
}