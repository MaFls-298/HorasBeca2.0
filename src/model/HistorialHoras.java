package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class HistorialHoras {

    private Alumno alumno;
    private List<RegistroActividad> historial;

    public HistorialHoras(Alumno alumno) {
        this.alumno = alumno;
        this.historial = new ArrayList<>();
    }

 
    public void agregarRegistro(Actividad actividad, int horasAsignadas) {
        RegistroActividad registro = new RegistroActividad(actividad, horasAsignadas, new Date());
        historial.add(registro);
        alumno.setHorasAcumuladas(alumno.getHorasAcumuladas() + horasAsignadas);
        alumno.setHorasBecaPendiente(Math.max(0, alumno.getHorasBecaPendiente() - horasAsignadas));
    }


    public void mostrarHistorial() {
        System.out.println("----- Historial de Horas Beca -----");
        System.out.println("Alumno: " + alumno.getNombreUser());
        for (RegistroActividad r : historial) {
            System.out.println("Actividad: " + r.getActividad().getTitulo() +
                               " | Horas: " + r.getHorasAsignadas() +
                               " | Fecha: " + r.getFechaAsignacion());
        }
        System.out.println("Total acumulado: " + alumno.getHorasAcumuladas() + " horas");
        System.out.println("----------------------------------");
    }

    
    public List<RegistroActividad> getHistorial() {
        return historial;
    }
}
