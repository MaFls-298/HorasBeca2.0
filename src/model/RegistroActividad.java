package modelo;

import java.util.Date;

public class RegistroActividad {
    private Actividad actividad;
    private int horasAsignadas;
    private Date fechaAsignacion;

    public RegistroActividad(Actividad actividad, int horasAsignadas, Date fechaAsignacion) {
        this.actividad = actividad;
        this.horasAsignadas = horasAsignadas;
        this.fechaAsignacion = fechaAsignacion;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public int getHorasAsignadas() {
        return horasAsignadas;
    }

    public Date getFechaAsignacion() {
        return fechaAsignacion;
    }
}
