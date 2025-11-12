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

    // Get horas
    public static Route getHorasAlumno = (req, res) -> {
        res.type("application/json");
        int alumnoId = Integer.parseInt(req.params("id"));
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT horasBecaPendiente, horasAcumuladas FROM Usuarios WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("horasPendientes", rs.getInt("horasBecaPendiente"));
                response.put("horasAcumuladas", rs.getInt("horasAcumuladas"));
                return gson.toJson(response);
            }
            
            res.status(404);
            return gson.toJson(Map.of("success", false, "error", "Alumno no encontrado"));
        } catch (SQLException e) {
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // Get actividades
    public static Route getActividadesDisponibles = (req, res) -> {
        res.type("application/json");
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT a.*, u.departamento FROM Actividades a " +
                        "JOIN Usuarios u ON a.encargado_id = u.id " +
                        "WHERE a.actividadState = true AND a.cupoUsado < a.cupoMaximo " +
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
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // Get historial
    public static Route getHistorialAlumno = (req, res) -> {
        res.type("application/json");
        int alumnoId = Integer.parseInt(req.params("id"));
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT i.*, a.titulo as tituloActividad, a.fechaActividad, a.horaActividad, a.horasOtorgadas, " +
                        "CASE WHEN a.fechaActividad < CURDATE() THEN true ELSE false END as actividadCompletada " +
                        "FROM InscripcionesAlumnos i " +
                        "JOIN Actividades a ON i.actividadId = a.id " +
                        "WHERE i.alumnoId = ? " +
                        "ORDER BY a.fechaActividad DESC, i.fechaInscripcion DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, alumnoId);
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
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // inscribir
    public static Route inscribirEnActividad = (req, res) -> {
        res.type("application/json");
        Map<String, Object> body = gson.fromJson(req.body(), Map.class);
        int actividadId = ((Double) body.get("actividadId")).intValue();
        int alumnoId = ((Double) body.get("alumnoId")).intValue();
        
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Check if inscrito
            String checkSql = "SELECT id FROM InscripcionesAlumnos WHERE actividadId = ? AND alumnoId = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, actividadId);
            checkStmt.setInt(2, alumnoId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                res.status(400);
                return gson.toJson(Map.of("success", false, "error", "Ya estás inscrito en esta actividad"));
            }
            
            // Check disponible
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
                
                // inscribir db
                String enrollSql = "INSERT INTO InscripcionesAlumnos (actividadId, alumnoId, fechaInscripcion) VALUES (?, ?, CURDATE())";
                PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
                enrollStmt.setInt(1, actividadId);
                enrollStmt.setInt(2, alumnoId);
                enrollStmt.executeUpdate();
                
                
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
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };

    // Get departaments
    public static Route getDepartamentos = (req, res) -> {
        res.type("application/json");
        
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
            res.status(500);
            return gson.toJson(Map.of("success", false, "error", e.getMessage()));
        }
    };
}