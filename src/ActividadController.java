import java.util.ArrayList;

public class ActividadController {
    private ArrayList<Actividad> actividadesList;

    public ActividadController() {
        this.actividadesList = new ArrayList<>();
    }


    public void crearActividad(Actividad actividad) {
        actividadesList.add(actividad);
    }

    public Actividad searchActividadName(String tituloActividad) {
        for (Actividad actividad : actividadesList) {
            if (actividad.getTituloActividad() == tituloActividad) {
                return actividad;
            }
        }
        return null;
    }

    public Actividad searchActividadId(int idActividad) {
        for (Actividad actividad : actividadesList) {
            if (actividad.getIdActividad() == idActividad) {
                return actividad;
            }
        }
        return null;
    }

    public boolean inscribirAlumno(Alumno alumno, String tituloActividad) {
        Actividad act = searchActividadName(tituloActividad);
        if (act != null && act.verStateActividad()){
            act.inscribirAlumno(alumno);
            alumno.agregarActividad(act);
            return true;
        }
        return false;
    }

    public boolean validarHorasAlumno(Alumno alumno, int idActividad) {
        Actividad act = searchActividadId(idActividad);
        if (act != null && alumno.getActivRealizadasList().contains(act)) {
            return true;
        }
        return false;
    }

    public ArrayList<Actividad> getTodasActividades() {
        return actividadesList;
    }

        /*public ArrayList<Actividad> getActividadesDisponibles() {
        ArrayList<Actividad> disponibles = new ArrayList<>();
        for (Actividad act : actividades) {
            if (act.tieneCupoDisponible()) {
                disponibles.add(act);
            }
        }
        return disponibles;
    }*/
}