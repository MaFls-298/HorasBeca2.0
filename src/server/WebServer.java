package server;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.sql.*;
import database.DbConnection;

public class WebServer {

    public static void main(String[] args) {
        port(8080); 


        staticFiles.externalLocation("web");

        post("/login", (req, res) -> {
            res.type("application/json");
            Gson gson = new Gson();

            LoginRequest body = gson.fromJson(req.body(), LoginRequest.class);

            boolean success = false;
            String nombre = "";
            String tipoUsuario = "";

            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT nombreUser, tipoUsuario FROM usuarios WHERE emailInstitucional=? AND passwordUser=? AND tipoUsuario=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, body.email);
                stmt.setString(2, body.password);
                stmt.setString(3, body.tipoUsuario);
                

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    success = true;
                    nombre = rs.getString("nombreUser");
                    tipoUsuario = rs.getString("tipoUsuario");
                }
            } catch (SQLException e) {
                System.out.println("Error en la database: " + e.getMessage());
            }

            return gson.toJson(new LoginResponse(success, nombre, tipoUsuario));
        });
    }

    static class LoginRequest {
        String email;
        String password;
        String tipoUsuario;
    }

    static class LoginResponse {
        boolean success;
        String nombre;
        String tipoUsuario;
        LoginResponse(boolean success, String nombre, String tipoUsuario) {
            this.success = success;
            this.nombre = nombre;
            this.tipoUsuario = tipoUsuario;
        }
    }
}
