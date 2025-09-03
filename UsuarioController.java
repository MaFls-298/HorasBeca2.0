import java.util.ArrayList;

public class UsuarioController {
    private ArrayList<Usuario> usuarios;

    public UsuarioController() {
        usuarios = new ArrayList<>();
    }

    
    public void registrarUsuario(String correo, String contrasena, String tipo, 
                                int carnet, String nombre) {
        Usuario nuevo = new Usuario(carnet, nombre, correo, contrasena, tipo);
        usuarios.add(nuevo);
        System.out.println("Usuario registrado: " + nombre + " (" + tipo + ")");
    }

    
    public Usuario login(String correo, String contrasena) {
        for (Usuario u : usuarios) {
            if (u.autenticar(correo, contrasena)) {
                System.out.println("Login exitoso: " + u.getUserInfo());
                return u;
            }
        }
        System.out.println("Error: correo o contraseña incorrectos");
        return null;
    }

    
    public void mostrarUsuarios() {
        for (Usuario u : usuarios) {
            System.out.println(u.getUserInfo());
        }
    }
}