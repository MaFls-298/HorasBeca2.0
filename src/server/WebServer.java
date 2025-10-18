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

            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT nombreUser FROM usuarios WHERE emailInstitucional=? AND passwordUser=? AND tipoUsuario=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, body.email);
                stmt.setString(2, body.password);
                stmt.setString(3, body.tipoUsuario);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    success = true;
                    nombre = rs.getString("nombreUser");
                }
            } catch (SQLException e) {
                System.out.println("Error en la database: " + e.getMessage());
            }

            return gson.toJson(new LoginResponse(success, nombre));
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
        LoginResponse(boolean success, String nombre) {
            this.success = success;
            this.nombre = nombre;
        }
    }
}
