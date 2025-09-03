
import java.util.ArrayList;

public class ActividadController {
    private ArrayList<Actividad> actividades = new ArrayList<>();

    public void agregarActividad(String nombre, String descripcion, int horasOtorgadas, int cupoMaximo) {
        Actividad nueva = new Actividad(nombre, descripcion, horasOtorgadas, cupoMaximo);
        actividades.add(nueva);
    }

    public Actividad buscarActividad(String nombre) {
        for (Actividad act : actividades) {
            if (act.nombre.equals(nombre)) {
                return act;
            }
        }
        return null;
    }

    public boolean inscribirAlumno(String nombreActividad) {
        Actividad act = buscarActividad(nombreActividad);
        if (act != null && act.tieneCupoDisponible()) {
            act.registrarAlumno();
            return true;
        }
        return false;
    }

    public ArrayList<Actividad> obtenerActividadesDisponibles() {
        ArrayList<Actividad> disponibles = new ArrayList<>();
        for (Actividad act : actividades) {
            if (act.tieneCupoDisponible()) {
                disponibles.add(act);
            }
        }
        return disponibles;
    }
}