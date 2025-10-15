import java.awt.*;
import java.sql.*;
import javax.swing.*;

//test ui con db — ia

    
public class LoginUserView extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeBox;

    public LoginUserView() {
        setTitle("Inicio de Sesión - Sistema de Usuarios");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- Title ---
        JLabel titleLabel = new JLabel("Inicio de Sesión", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        // --- Center Panel (Form) ---
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        formPanel.add(new JLabel("Correo institucional:"));
        emailField = new JTextField();
        formPanel.add(emailField);

        formPanel.add(new JLabel("Contraseña:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        formPanel.add(new JLabel("Tipo de usuario:"));
        userTypeBox = new JComboBox<>(new String[]{"Alumno", "Encargado"});
        formPanel.add(userTypeBox);

        JButton loginButton = new JButton("Iniciar sesión");
        formPanel.add(new JLabel()); // empty cell
        formPanel.add(loginButton);

        add(formPanel, BorderLayout.CENTER);

        // --- Button Action ---
        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String tipoUsuario = (String) userTypeBox.getSelectedItem();

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM UsuariosDB WHERE emailInstitucional = ? AND passwordUser = ? AND tipoUsuario = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, tipoUsuario);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String nombre = rs.getString("nombreUser");
                JOptionPane.showMessageDialog(this,
                        "✅ Bienvenido " + nombre + " (" + tipoUsuario + ")",
                        "Acceso concedido", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ Credenciales incorrectas o tipo de usuario inválido.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error en la conexión: " + e.getMessage(),
                    "Error de BD", JOptionPane.ERROR_MESSAGE);
        }
    }
}

