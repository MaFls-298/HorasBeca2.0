import java.util.ArrayList;

public class UsuarioController {
    private ArrayList<Usuario> usuarios;

    public UsuarioController() {
        this.usuarios = new ArrayList<>();
    }

    
    public void registrarUsuario(Usuario usuario) { 
        usuarios.add(usuario);
    }

    
    public Usuario login(String email, String password) {
        for (Usuario u : usuarios) {
            if (u.verifLogin(email, password)) {
                return u;
            }
        }
        return null;
    }

    public Usuario searchByCarnet(int carnetUser) {
        for (Usuario user : usuarios) {
            if (user.getCarnetUser() == carnetUser) {
                return user;
            }
        }
        return null;
    }

    public boolean cambiarContrasena(int carnetUser, String nuevaContrasena) {
        Usuario user = searchByCarnet(carnetUser);
        if (user != null) {
            user.setPasswordUser(nuevaContrasena);
            return true;
        }
        return false;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }
    
    /* public void mostrarUsuarios() {
        for (Usuario u : usuarios) {
            System.out.println(u.getUserInfo()); 
                    }
    } */
}