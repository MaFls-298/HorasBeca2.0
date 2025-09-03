public class Encargado {
    private int carnetUser;
    private String nombreUser;
    private String emailInstitucional;
    private String passwordUser;

    
    public Encargado(int carnetUser, String nombreUser, String emailInstitucional,
                    String passwordUser) {
        this.carnetUser = carnetUser;
        this.nombreUser = nombreUser;
        this.emailInstitucional = emailInstitucional;
        this.passwordUser = passwordUser;
    }

    
    public boolean autenticar(String correo, String contrasenia) {
        return this.emailInstitucional.equals(correo) &&
            this.passwordUser.equals(contrasenia);
    }

    public void publicarActividad(String titulo) {
        System.out.println("Actividad publicada: " + titulo);
    }

    public void validarAsistencia(Alumno alumno, int horas) {
        alumno.agregarHoras(horas);
        System.out.println("Se agregaron " + horas + " horas a " + alumno.getUserInfo());
    }
}