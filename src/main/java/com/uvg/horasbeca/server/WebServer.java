package com.uvg.horasbeca.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.uvg.horasbeca.controller.ActividadController;
import com.uvg.horasbeca.controller.AlumnoController;
import com.uvg.horasbeca.controller.EncargadoController;
import com.uvg.horasbeca.database.DbConnection;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class WebServer {

    public static void main(String[] args) {
        port(8080); 

        staticFiles.location("/web");

        //actividades
        post("/actividades", ActividadController.crearActividad);
        get("/actividades/disponibles", ActividadController.getActividadesDisponibles);
        get("/actividades/encargado/:id", ActividadController.getActividadesByEncargado);
        post("/actividades/toggleDisponibilidad", ActividadController.toggleDisponibilidadActividad);



        //alumnos
        get("/alumnos/:id/horas", AlumnoController.getHorasAlumno);
        get("/actividades/inscritos/:id", AlumnoController.getInscritosByActividad);
        post("/actividades/inscribirse", AlumnoController.inscribirseActividad);

        //get("/alumnos/:id/historial", AlumnoController.getHistorialAlumno);
        

        //encargados
        get("/departamentos", EncargadoController.getDepartamentos);  
        post("/inscripcion/validarHoras", EncargadoController.validarHorasAlumno);
    

        System.out.println("Server running on http://localhost:8080");

        Gson gson = new Gson();

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin,");
        });

        ///////////

        post("/login", (req, res) -> {
            res.type("application/json");

            LoginRequest body = gson.fromJson(req.body(), LoginRequest.class);

            boolean success = false;
            String nombre = "";
            String tipoUsuario = "";

            try (Connection conn = DbConnection.getConnection()) {
                String sql = "SELECT carnetUser, nombreUser, tipoUsuario, emailInstitucional FROM usuarios WHERE emailInstitucional=? AND passwordUser=?";

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
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("id", rs.getInt("carnetUser"));  
                    result.put("nombre", rs.getString("nombreUser"));
                    result.put("tipoUsuario", rs.getString("tipoUsuario"));
                    result.put("email", rs.getString("emailInstitucional"));
                    return new Gson().toJson(result);

                }
            } catch (SQLException e) {
                System.out.println("Error en sqlite: " + e.getMessage());
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
