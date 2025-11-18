package com.uvg.horasbeca.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.uvg.horasbeca.database.DbConnection;
import com.uvg.horasbeca.model.Actividad;

import spark.Route;

public class ActividadController {
    private static final Gson gson = new Gson();

    public static Route crearActividad = (req, res) -> {
        res.type("application/json");
        Actividad act = gson.fromJson(req.body(), Actividad.class);

        System.out.println("Received actividad: " + act.titulo + ", " + act.fechaActividad + ", " + act.horaActividad);

        try (Connection conn = DbConnection.getConnection()) {
            String sql = "INSERT INTO Actividades (titulo, descripcion, horasOtorgadas, cupoMaximo, cupoUsado, fechaActividad, horaActividad, encargado_id, actividadState) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, act.titulo);
            stmt.setString(2, act.descripcion);
            stmt.setInt(3, act.horasOtorgadas);
            stmt.setInt(4, act.cupoMaximo);
            stmt.setInt(5, act.cupoUsado);
            stmt.setString(6, act.fechaActividad);
            stmt.setString(7, act.horaActividad);
            stmt.setInt(8, act.encargadoId);
            stmt.setBoolean(9, act.actividadState);
            stmt.executeUpdate();
        } catch (SQLException e) {
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }

        res.status(201);
        return gson.toJson(Map.of("success", true));
    };


    // actividades disponibesl /////////////////////////////////////////////////////

    public static Route getActividadesDisponibles = (req, res) -> {
        res.type("application/json");
                
            try (Connection conn = DbConnection.getConnection()) {
        String sql = """
            SELECT a.id,
                    a.titulo,
                    a.descripcion,
                    a.horasOtorgadas,
                    a.cupoUsado,
                    a.cupoMaximo,
                    a.fechaActividad,
                    a.horaActividad,
                    a.encargado_id,
                    u.departamento
                    
            FROM actividades a
            LEFT JOIN usuarios u ON a.encargado_id = u.carnetUser
            WHERE a.actividadState = 1""";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> actividades = new ArrayList<>();
            
            while (rs.next()) {
                Map<String, Object> act = new HashMap<>();
                act.put("id", rs.getInt("id"));
                act.put("titulo", rs.getString("titulo"));
                act.put("descripcion", rs.getString("descripcion"));
                act.put("horasOtorgadas", rs.getInt("horasOtorgadas"));
                act.put("cupoMaximo", rs.getInt("cupoMaximo"));
                act.put("cupoUsado", rs.getInt("cupoUsado"));
                act.put("fechaActividad", rs.getString("fechaActividad"));
                act.put("horaActividad", rs.getString("horaActividad"));
                act.put("departamento", rs.getString("departamento"));
                act.put("encargado_id", rs.getInt("encargado_id"));
                actividades.add(act);
            }
            System.out.println("Actividades fetched: " + actividades);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("actividades", actividades);
            return gson.toJson(response);

        } catch (SQLException e) {
            e.printStackTrace();
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };


    public static Route getActividadesByEncargado = (req, res) -> {
        res.type("application/json");
        String encargadoId = req.params("id");

        System.out.println("Requested encargado ID: " + req.params(":id"));
                
            try (Connection conn = DbConnection.getConnection()) {
        String sql = """
            SELECT a.id,
                    a.titulo,
                    a.descripcion,
                    a.horasOtorgadas,
                    a.cupoUsado,
                    a.cupoMaximo,
                    a.fechaActividad,
                    a.horaActividad,
                    a.encargado_id,
                    u.carnetUser
                    
            FROM actividades a
            LEFT JOIN usuarios u ON a.encargado_id = u.carnetUser
            WHERE a.encargado_id = ?""";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, encargadoId); 
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> actividades = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                Map<String, Object> act = new HashMap<>();
                act.put("id", rs.getInt("id"));
                act.put("titulo", rs.getString("titulo"));
                act.put("descripcion", rs.getString("descripcion"));
                act.put("horasOtorgadas", rs.getInt("horasOtorgadas"));
                act.put("cupoMaximo", rs.getInt("cupoMaximo"));
                act.put("cupoUsado", rs.getInt("cupoUsado"));
                act.put("fechaActividad", rs.getString("fechaActividad"));
                act.put("horaActividad", rs.getString("horaActividad"));
                act.put("encargado_id", rs.getInt("encargado_id"));
                actividades.add(act);
                count++;
            }
            System.out.println("Fetched " + count + " actividades");

            System.out.println("Actividades fetched for encargado " + encargadoId + ": " + actividades.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("actividades", actividades);
            return gson.toJson(response);


            } catch (SQLException e) {
            e.printStackTrace();
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", "SQL Error: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", "Server Error: " + e.getMessage()));
        }
    };

    //cambiar disponibilidad /////////////////////////////////////////////////////////////////////////////

    public static Route toggleDisponibilidadActividad = (req, res) -> {
    res.type("application/json");

    try {
        Map<String, Object> body = new Gson().fromJson(req.body(), Map.class);
        if (body == null || !body.containsKey("actividadId")) {
            return new Gson().toJson(Map.of("success", false, "msg", "Faltan datos requeridos"));
        }

        int actividadId = ((Number) body.get("actividadId")).intValue();

        try (Connection conn = DbConnection.getConnection()) {
            // Get cupoUsado, cupoMaximo, actividadState
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT cupoUsado, cupoMaximo, actividadState FROM Actividades WHERE id = ?"
            );
            stmt.setInt(1, actividadId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return new Gson().toJson(Map.of("success", false, "msg", "Actividad no encontrada"));
            }

            int cupoUsado = rs.getInt("cupoUsado");
            int cupoMaximo = rs.getInt("cupoMaximo");
            boolean actividadState = rs.getBoolean("actividadState");

            boolean newState = cupoUsado >= cupoMaximo ? false : !actividadState;

            // Update
            PreparedStatement update = conn.prepareStatement(
                "UPDATE actividades SET actividadState = ? WHERE id = ?"
            );
            update.setBoolean(1, newState);
            update.setInt(2, actividadId);
            update.executeUpdate();

            return new Gson().toJson(Map.of(
                "success", true,
                "newState", newState,
                "msg", newState ? "Actividad disponible" : "Actividad no disponible"
            ));
        }

    } catch (Exception e) {
        e.printStackTrace();
        return new Gson().toJson(Map.of("success", false, "msg", "Error interno del servidor"));
    }
};



}

