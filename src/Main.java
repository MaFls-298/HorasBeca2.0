import ui.LoginUserView;

public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            System.out.println("path conectado:  " + new java.io.File("data/UsuariosDB.db").getAbsolutePath());
            new LoginUserView().setVisible(true);
            
        });
    }
}
