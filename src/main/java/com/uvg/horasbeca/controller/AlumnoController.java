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

    public static Route getInscritosByActividad = (req, res) -> {
        System.out.println("Fetching inscritos for actividadId: " + req.params(":id"));
        res.type("application/json");
        int actividadId = Integer.parseInt(req.params(":id"));

        Connection conn = DbConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT u.carnetUser, u.nombreUser, u.emailInstitucional, i.id AS inscripcionId " +
            "FROM inscripciones i " +
            "JOIN usuarios u ON i.alumnoId = u.carnetUser " +
            "WHERE i.actividadId = ?"
        );

        stmt.setInt(1, actividadId);
        ResultSet rs = stmt.executeQuery();

        List<Map<String,Object>> inscritos = new ArrayList<>();

        while (rs.next()) {
            Map<String,Object> alumno = new HashMap<>();
            alumno.put("carnet", rs.getInt("carnetUser"));
            alumno.put("nombre", rs.getString("nombreUser"));
            alumno.put("email", rs.getString("emailInstitucional"));
            alumno.put("inscripcionId", rs.getInt("inscripcionId"));
            inscritos.add(alumno);
        }

        return gson.toJson(Map.of("success", true, "inscritos", inscritos));
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
    public static Route inscribirseActividad = (req, res) -> {
        res.type("application/json");
        Map<String, Object> body = new Gson().fromJson(req.body(), Map.class);
        int alumnoId = ((Double) body.get("alumnoId")).intValue();
        int actividadId = ((Double) body.get("actividadId")).intValue();

        try (Connection conn = DbConnection.getConnection()) {

            // 
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT COUNT(*) AS total FROM inscripciones WHERE alumnoId = ? AND actividadId = ?"
            );
            checkStmt.setInt(1, alumnoId);
            checkStmt.setInt(2, actividadId);
            ResultSet rsCheck = checkStmt.executeQuery();
            if (rsCheck.next() && rsCheck.getInt("total") > 0) {
                return new Gson().toJson(Map.of("success", false, "msg", "Ya estás inscrito en esta actividad"));
            }

            // Check if cupoMaximo
            PreparedStatement cupoStmt = conn.prepareStatement(
                "SELECT cupoUsado, cupoMaximo FROM actividades WHERE id = ?"
            );
            cupoStmt.setInt(1, actividadId);
            ResultSet rsCupo = cupoStmt.executeQuery();
            if (rsCupo.next() && rsCupo.getInt("cupoUsado") >= rsCupo.getInt("cupoMaximo")) {
                return new Gson().toJson(Map.of("success", false, "msg", "Cupo máximo alcanzado"));
            }

            // iinsertar inscripción
            PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO inscripciones (actividadId, alumnoId) VALUES (?, ?)"
            );
            insertStmt.setInt(1, actividadId);
            insertStmt.setInt(2, alumnoId);
            insertStmt.executeUpdate();

            // Update cupoUsado en actividades
            PreparedStatement updateCupo = conn.prepareStatement(
                "UPDATE actividades SET cupoUsado = cupoUsado + 1 WHERE id = ?"
            );
            updateCupo.setInt(1, actividadId);
            updateCupo.executeUpdate();

            return new Gson().toJson(Map.of("success", true, "msg", "Inscripción exitosa"));

        } catch (SQLException e) {
            e.printStackTrace();
            return new Gson().toJson(Map.of("success", false, "msg", "Error en la inscripción"));
        }
    };


    
}