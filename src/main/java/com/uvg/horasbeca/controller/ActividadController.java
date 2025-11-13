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

    // actividades disponibesl ///////////////////////
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
                    System.out.println("=== RAW DATABASE RESULTS ===");
        while (rs.next()) {
            Map<String, Object> act = new HashMap<>();
            act.put("id", rs.getInt("id"));
            act.put("titulo", rs.getString("titulo"));
            
            String departamento = rs.getString("departamento");
            
            int encargado_id = rs.getInt("encargado_id");
            
            // Debug print for each row
            System.out.println("Activity: " + rs.getString("titulo") + 
                            " | Encargado ID: " + encargado_id +
                            
                            " | Departamento: " + departamento);
            
            act.put("descripcion", rs.getString("descripcion"));
            act.put("horasOtorgadas", rs.getInt("horasOtorgadas"));
            act.put("cupoMaximo", rs.getInt("cupoMaximo"));
            act.put("cupoUsado", rs.getInt("cupoUsado"));
            act.put("fechaActividad", rs.getString("fechaActividad"));
            act.put("horaActividad", rs.getString("horaActividad"));
            act.put("departamento", departamento);
            act.put("encargado_id", encargado_id);
            actividades.add(act);
        }
        System.out.println("=== END DATABASE RESULTS ===");
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


}

