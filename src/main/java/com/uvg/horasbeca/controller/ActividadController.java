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

    public static Route getActividades = (req, res) -> {
        res.type("application/json");
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT * FROM Actividades ORDER BY fechaActividad DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> actividades = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> actividad = new HashMap<>();
                actividad.put("id", rs.getInt("id"));
                actividad.put("titulo", rs.getString("titulo"));
                actividad.put("descripcion", rs.getString("descripcion"));
                actividad.put("horasOtorgadas", rs.getInt("horasOtorgadas"));
                actividad.put("cupoMaximo", rs.getInt("cupoMaximo"));
                actividad.put("cupoUsado", rs.getInt("cupoUsado"));
                actividad.put("fechaActividad", rs.getString("fechaActividad"));
                actividad.put("horaActividad", rs.getString("horaActividad"));
                actividad.put("encargadoId", rs.getInt("encargadoId"));
                actividades.add(actividad);
            }
            
            return gson.toJson(actividades);
        } catch (SQLException e) {
            res.status(500);
            return gson.toJson(Map.of("error", "Database error: " + e.getMessage()));
        }
    };

}

