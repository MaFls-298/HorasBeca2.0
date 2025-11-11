package com.uvg.horasbeca.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.uvg.horasbeca.controller.ActividadController;
import com.uvg.horasbeca.database.DbConnection;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class WebServer {

    public static void main(String[] args) {
        port(8080); 

        staticFiles.location("/web");
        Gson gson = new Gson();

        post("/login", (req, res) -> {
            res.type("application/json");

            LoginRequest body = gson.fromJson(req.body(), LoginRequest.class);

            boolean success = false;
            String nombre = "";
            String tipoUsuario = "";

            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT nombreUser, tipoUsuario FROM usuarios WHERE emailInstitucional=? AND passwordUser=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, body.email);
                stmt.setString(2, body.password);
                
                System.out.println("Login attempt:");
                System.out.println("Email: " + body.email);
                System.out.println("Password: " + body.password);
                System.out.println("TipoUsuario: " + body.tipoUsuario);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    success = true;
                    nombre = rs.getString("nombreUser");
                    tipoUsuario = rs.getString("tipoUsuario");
                                        
                }
            } catch (SQLException e) {
                System.out.println("Error en sqlite: " + e.getMessage());
            }

            return gson.toJson(new LoginResponse(success, nombre, tipoUsuario));
        });

        post("/actividades", ActividadController.crearActividad);
        //get("/actividades", ActividadController.listarActividades);
        //get("/actividades/:id", ActividadController.obtenerActividadPorId);
        //get("/actividades/:id/alumnos", ActividadController.obtenerAlumnosInscritos);
        //put("/actividades/:id/validar/:alumnoId", ActividadController.validarAsistencia);
        //delete("/actividades/:id", ActividadController.eliminarActividad);

        System.out.println("Running at http://localhost:8080");
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
