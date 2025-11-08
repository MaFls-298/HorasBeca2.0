package com.uvg.horasbeca.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.Gson;
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

        /*get("/actividades", (req, res) -> {
            res.type("application/json");

            try (Connection conn = DbConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Actividades WHERE cupoUsado < cupoMaximo");

            var actividades = new java.util.ArrayList<>();
            while (rs.next()) {
                var act = new java.util.HashMap<String, Object>();
                act.put("id", rs.getInt("id"));
                act.put("titulo", rs.getString("titulo"));
                act.put("descripcion", rs.getString("descripcion"));
                act.put("horasOtorgadas", rs.getInt("horasOtorgadas"));
                act.put("cupoMaximo", rs.getInt("cupoMaximo"));
                act.put("cupoUsado", rs.getInt("cupoUsado"));
                act.put("fechaActividad", rs.getString("fechaActividad"));
                act.put("horaActividad", rs.getString("horaActividad"));
                actividades.add(act);
                }
                return gson.toJson(actividades);
            } catch (SQLException e) {
                res.status(500);
                return gson.toJson("Error: " + e.getMessage());
            }
        });
    

        post("/actividades", (req, res) -> {
    res.type("application/json");

    
    .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
        public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsString());
        }
    })
    .registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
        public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalTime.parse(json.getAsString());
        }
    })
    .create();


    // Deserialize the incoming JSON to your full Actividad class
    Actividad body = gson.fromJson(req.body(), Actividad.class);

    String sql = """
        INSERT INTO Actividades(titulo, descripcion, horasOtorgadas, cupoMaximo, cupoUsado, fechaActividad, horaActividad, encargado_id)
        VALUES (?, ?, ?, ?, 0, ?, ?, ?)
        """;

    try (Connection conn = DbConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, body.titulo);
        stmt.setString(2, body.descripcion);
        stmt.setInt(3, body.horasOtorgadas);
        stmt.setInt(4, body.cupoMaximo);
        stmt.setString(5, body.fechaActividad.toString());
        stmt.setString(6, body.horaActividad.toString());
        stmt.setInt(7, body.encargadoId);
        stmt.executeUpdate();

        return gson.toJson(Map.of("success", true));
    } catch (SQLException e) {
        res.status(500);
        return gson.toJson(Map.of("error", e.getMessage()));
    }
});

        post("/actividades/:id/inscribir", (req, res) -> {
            Gson gson = new Gson();
            res.type("application/json");
            int actividadId = Integer.parseInt(req.params("id"));
            var body = gson.fromJson(req.body(), java.util.Map.class);
            int alumnoId = ((Double) body.get("alumnoId")).intValue();

            String sql = "UPDATE Actividades SET cupoUsado = cupoUsado + 1 WHERE id = ? AND cupoUsado < cupoMaximo";

            try (Connection conn = DbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, actividadId);
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    // Optionally add record in 'asignaciones'
                    return gson.toJson(Map.of("success", true));
                } else {
                    res.status(400);
                    return gson.toJson(Map.of("success", false, "message", "No quedan cupos disponibles"));
                }
            } catch (SQLException e) {
                res.status(500);
                return gson.toJson(Map.of("error", e.getMessage()));
            }
        });*/
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
