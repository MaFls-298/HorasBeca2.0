public class Alumno {
    private int carnetUser;
    private String nombreUser;
    private String emailInstitucional;
    private String passwordUser;

    private int horasBecaPendiente;
    private int horasAcumuladas;

    public Alumno(int carnetUser, String nombreUser, String emailInstitucional,
                  String passwordUser, int horasBecaPendiente) {
        this.carnetUser = carnetUser;
        this.nombreUser = nombreUser;
        this.emailInstitucional = emailInstitucional;
        this.passwordUser = passwordUser;
        this.horasBecaPendiente = horasBecaPendiente;
        this.horasAcumuladas = 0;
    }

    public boolean autenticar(String correo, String contrasenia) {
        return this.emailInstitucional.equals(correo) &&
               this.passwordUser.equals(contrasenia);
    }

    public void agregarHoras(int horas) {
        this.horasAcumuladas += horas;
        this.horasBecaPendiente -= horas;
    }

    public int calcularHorasPendientes() {
        return horasBecaPendiente;
    }

    public String getUserInfo() {
        return "Carnet: " + carnetUser + ", Nombre: " + nombreUser +
               ", Correo: " + emailInstitucional + ", Horas pendientes: " +
               horasBecaPendiente + ", Horas acumuladas: " + horasAcumuladas;
    }
}