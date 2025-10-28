package model;

public class Alerta {

    private Alumno alumno;
    private String mensaje;
    private boolean leido;

    public Alerta(Alumno alumno, String mensaje) {
        this.alumno = alumno;
        this.mensaje = mensaje;
        this.leido = false;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isLeido() {
        return leido;
    }

    public void marcarComoLeido() {
        this.leido = true;
    }

    public static Alerta generarAlertaHoras(Alumno alumno) {
        String mensaje = "Te faltan " + alumno.getHorasBecaPendiente() + " horas para completar tu beca.";
        return new Alerta(alumno, mensaje);
    }


    public static Alerta generarAlertaActividad(Actividad actividad) {
        String mensaje = "Nueva actividad disponible: " + actividad.getTitulo() + 
                         " (" + actividad.getHorasOtorgadas() + " horas).";
        return new Alerta(null, mensaje);
    }
}
