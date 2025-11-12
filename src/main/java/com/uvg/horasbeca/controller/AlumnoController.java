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

import spark.Route;

public class AlumnoController {
    private static final Gson gson = new Gson();

    //get horas del alumno///////////////////

    public static Route getHorasAlumno = (req, res) -> {
    res.type("application/json");
    String alumnoId = req.params("id");

    try (Connection conn = DbConnection.getConnection()) {
        String sql = "SELECT horasBecaPendiente, horasAcumuladas FROM usuarios WHERE carnetUser = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(alumnoId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("horasPendientes", rs.getInt("horasBecaPendiente"));
                    result.put("horasAcumuladas", rs.getInt("horasAcumuladas"));
                    return new Gson().toJson(result);
                } else {
                    res.status(404);
                    return new Gson().toJson(Map.of("success", false, "error", "Alumno no encontrado"));
                }
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        res.status(500);
        return new Gson().toJson(Map.of("success", false, "error", e.getMessage()));
    } catch (Exception e) {
        e.printStackTrace();
        res.status(500);
        return new Gson().toJson(Map.of("success", false, "error", "Error inesperado: " + e.getMessage()));
    }
};

    // actividades disponibesl ///////////////////////
    public static Route getActividadesDisponibles = (req, res) -> {
        res.type("application/json");
        
        System.out.println("Getting available activities");
        
        try (Connection conn = DbConnection.getConnection()) {
            
            String sql = "SELECT a.*, u.departamento FROM Actividades a " +
                        "JOIN Usuarios u ON a.encargado_id = u.carnetUser " + // CHANGED: encargado_id references carnetUser
                        "WHERE a.actividadState = 1 AND a.cupoUsado < a.cupoMaximo " +
                        "ORDER BY a.fechaActividad, a.horaActividad";
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
                actividad.put("departamento", rs.getString("departamento"));
                actividades.add(actividad);
            }
            
            return gson.toJson(Map.of("success", true, "actividades", actividades));
        } catch (SQLException e) {
            System.err.println("Error getting available activities: " + e.getMessage());
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // Get historial del alumno ///////////////////////////////
    public static Route getHistorialAlumno = (req, res) -> {
        res.type("application/json");
        String alumnoId = req.params("id");
        
        System.out.println("Getting history for student: " + alumnoId);
        
        try (Connection conn = DbConnection.getConnection()) {
            
            String sql = "SELECT i.*, a.titulo as tituloActividad, a.fechaActividad, a.horaActividad, a.horasOtorgadas, " +
                        "CASE WHEN a.fechaActividad < date('now') THEN 1 ELSE 0 END as actividadCompletada " +
                        "FROM inscripciones i " +
                        "JOIN Actividades a ON i.actividadId = a.id " +
                        "WHERE i.alumnoId = ? " + 
                        "ORDER BY a.fechaActividad DESC, i.fechaInscripcion DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, alumnoId);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, Object>> inscripciones = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> inscripcion = new HashMap<>();
                inscripcion.put("id", rs.getInt("id"));
                inscripcion.put("tituloActividad", rs.getString("tituloActividad"));
                inscripcion.put("fechaActividad", rs.getString("fechaActividad"));
                inscripcion.put("horaActividad", rs.getString("horaActividad"));
                inscripcion.put("horasOtorgadas", rs.getInt("horasOtorgadas"));
                inscripcion.put("asistenciaValidada", rs.getBoolean("asistenciaValidada"));
                inscripcion.put("actividadCompletada", rs.getBoolean("actividadCompletada"));
                inscripcion.put("fechaInscripcion", rs.getString("fechaInscripcion"));
                inscripciones.add(inscripcion);
            }
            
            return gson.toJson(Map.of("success", true, "inscripciones", inscripciones));
        } catch (SQLException e) {
            System.err.println("Error getting student history: " + e.getMessage());
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // inscribirse en actiividad ///////////////////////////////////////////
    public static Route inscribirEnActividad = (req, res) -> {
        res.type("application/json");
        Map<String, Object> body = gson.fromJson(req.body(), Map.class);
        int actividadId = ((Double) body.get("actividadId")).intValue();
        String alumnoId = body.get("alumnoId").toString(); 
        
        System.out.println("Enrolling student " + alumnoId + " in activity " + actividadId);
        
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Check if inscrito
            String checkSql = "SELECT id FROM inscripciones WHERE actividadId = ? AND alumnoId = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, actividadId);
            checkStmt.setString(2, alumnoId); 
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                res.status(400);
                return gson.toJson(Map.of("success", false, "error", "Ya estás inscrito en esta actividad"));
            }
            
            // check if cupo
            String activitySql = "SELECT cupoUsado, cupoMaximo FROM Actividades WHERE id = ?";
            PreparedStatement activityStmt = conn.prepareStatement(activitySql);
            activityStmt.setInt(1, actividadId);
            ResultSet activityRs = activityStmt.executeQuery();
            
            if (activityRs.next()) {
                int cupoUsado = activityRs.getInt("cupoUsado");
                int cupoMaximo = activityRs.getInt("cupoMaximo");
                
                if (cupoUsado >= cupoMaximo) {
                    res.status(400);
                    return gson.toJson(Map.of("success", false, "error", "El cupo para esta actividad está lleno"));
                }
                
                // inscribir
                String enrollSql = "INSERT INTO inscripciones (actividadId, alumnoId, fechaInscripcion) VALUES (?, ?, date('now'))";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, actividadId);
                enrollStmt.setString(2, alumnoId); 
                enrollStmt.executeUpdate();
                
                // Update cuenta 
                String updateSql = "UPDATE Actividades SET cupoUsado = cupoUsado + 1 WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, actividadId);
                updateStmt.executeUpdate();
                
                conn.commit();
                return gson.toJson(Map.of("success", true));
            }
            
            res.status(404);
            return gson.toJson(Map.of("success", false, "error", "Actividad no encontrada"));
            
        } catch (SQLException e) {
            System.err.println("Error enrolling student: " + e.getMessage());
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // Get departmentos
    public static Route getDepartamentos = (req, res) -> {
        res.type("application/json");
        
        System.out.println("Getting departments");
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT DISTINCT departamento FROM Usuarios WHERE departamento IS NOT NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            List<String> departamentos = new ArrayList<>();
            while (rs.next()) {
                departamentos.add(rs.getString("departamento"));
            }
            
            return gson.toJson(Map.of("success", true, "departamentos", departamentos));
        } catch (SQLException e) {
            System.err.println("Error getting departments: " + e.getMessage());
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };
}