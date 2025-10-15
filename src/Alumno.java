import java.util.ArrayList;
import java.util.List;

public class Alumno extends Usuario{
    private int horasBecaPendiente;
    private int horasAcumuladas;
    private List<Actividad> actividadesRealizadasList;

    public Alumno(int carnetUser, String nombreUser, String emailInstitucional, String passwordUser, int horasBecaPendiente){
        super(carnetUser, nombreUser, emailInstitucional, passwordUser);
        this.actividadesRealizadasList = new ArrayList<>();
        this.horasAcumuladas = 0;
        this.horasBecaPendiente = horasBecaPendiente;
        
    }

    public void agregarActividad(Actividad actividad) {
        actividadesRealizadasList.add(actividad);
        
    }
    public void agregarHoras(Actividad actividad) {
        horasAcumuladas += actividad.getHorasOtorgadasActividad();
        horasBecaPendiente -= actividad.getHorasOtorgadasActividad();
    }

    public int getHorasPendientes(){
        return horasBecaPendiente;
    }

    public int getHorasAcumuladas(){
        return horasAcumuladas;
    }

    public List<Actividad> getActivRealizadasList(){
        return actividadesRealizadasList;
    }

    public String getUserInfo() {
        return "Carnet: " + carnetUser + ", Nombre: " + nombreUser +
            ", Correo: " + emailInstitucional + ", Horas pendientes: " +
            horasBecaPendiente + ", Horas acumuladas: " + horasAcumuladas;
    }
}