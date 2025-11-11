package com.uvg.horasbeca.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

}

