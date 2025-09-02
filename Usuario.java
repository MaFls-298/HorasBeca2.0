public class Usuario {
    private int carnetUser;
    private String nombreUser;
    private String emailInstitucional;
    private String passwordUser;
    private String tipoUsuario; 

    public Usuario(int carnetUser, String nombreUser, String emailInstitucional,
                   String passwordUser, String tipoUsuario) {
        this.carnetUser = carnetUser;
        this.nombreUser = nombreUser;
        this.emailInstitucional = emailInstitucional;
        this.passwordUser = passwordUser;
        this.tipoUsuario = tipoUsuario;
    }

    public boolean autenticar(String correo, String contrasenia) {
        return this.emailInstitucional.equals(correo) &&
               this.passwordUser.equals(contrasenia);
    }

    public void resetPassword(String newPassword) {
        this.passwordUser = newPassword;
    }

    public String getUserType() {
        return this.tipoUsuario;
    }

    public String getUserInfo() {
        return "Carnet: " + carnetUser + ", Nombre: " + nombreUser +
               ", Correo: " + emailInstitucional + ", Tipo: " + tipoUsuario;
    }
}