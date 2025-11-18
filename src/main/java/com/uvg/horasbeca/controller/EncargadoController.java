package com.uvg.horasbeca.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.uvg.horasbeca.database.DbConnection;

import spark.Route;

public class EncargadoController {
    private static final Gson gson = new Gson();

    // Get departmentos
    public static Route getDepartamentos = (req, res) -> {
        res.type("application/json");
        
        System.out.println("Getting departmentos");
        
        try (Connection conn = DbConnection.getConnection()) {
            String sql = "SELECT DISTINCT departamento FROM usuarios WHERE departamento IS NOT NULL";
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

    //validar horas ///////////////////////////////////////////////////////////////////////

    public static Route validarHorasAlumno = (req, res) -> {
        res.type("application/json");

        try {
            Map<String, Object> body = new Gson().fromJson(req.body(), Map.class);
            if (body == null || !body.containsKey("inscripcionId") || !body.containsKey("actividadId")) {
                return new Gson().toJson(Map.of("success", false, "msg", "Faltan datos requeridos"));
            }

            int inscripcionId = ((Number) body.get("inscripcionId")).intValue();
            int actividadId = ((Number) body.get("actividadId")).intValue();

            try (Connection conn = DbConnection.getConnection()) {

                // Get horasOtorgadas and alumnoId
                PreparedStatement stmtAct = conn.prepareStatement(
                    "SELECT a.horasOtorgadas, i.alumnoId " +
                    "FROM actividades a " +
                    "JOIN inscripciones i ON a.id = i.actividadId " +
                    "WHERE i.id = ?"
                );
                stmtAct.setInt(1, inscripcionId);
                ResultSet rs = stmtAct.executeQuery();

                if (!rs.next()) {
                    return new Gson().toJson(Map.of("success", false, "msg", "Actividad/Inscripción no encontrada"));
                }

                int horasOtorgadas = rs.getInt("horasOtorgadas");
                int alumnoId = rs.getInt("alumnoId");

                // Update usuario
                PreparedStatement stmtUsr = conn.prepareStatement(
                    "UPDATE usuarios SET horasAcumuladas = horasAcumuladas + ?, " +
                    "horasBecaPendiente = CASE WHEN horasBecaPendiente - ? < 0 THEN 0 ELSE horasBecaPendiente - ? END " +
                    "WHERE carnetUser = ?"
                );
                stmtUsr.setInt(1, horasOtorgadas);
                stmtUsr.setInt(2, horasOtorgadas);
                stmtUsr.setInt(3, horasOtorgadas);
                stmtUsr.setInt(4, alumnoId);
                stmtUsr.executeUpdate();


                // Update inscripcion
                PreparedStatement stmtIns = conn.prepareStatement(
                    "UPDATE inscripciones SET asistenciaValidada = TRUE WHERE id = ?"
                );
                stmtIns.setInt(1, inscripcionId);
                stmtIns.executeUpdate();

                return new Gson().toJson(Map.of("success", true));

            }

        } catch (Exception e) {
            e.printStackTrace(); // VERY IMPORTANT: check server logs
            return new Gson().toJson(Map.of("success", false, "msg", "Error interno del servidor"));
        }
    };







}
