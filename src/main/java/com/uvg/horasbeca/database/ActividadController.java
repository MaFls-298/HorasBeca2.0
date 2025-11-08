package com.uvg.horasbeca.database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import com.uvg.horasbeca.model.Actividad;

public class ActividadController{

    public void insertarActividad(Actividad actividad) throws SQLException {
        String sql = "INSERT INTO Actividades (titulo, descripcion, horasOtorgadas, cupoMaximo, cupoUsado, fechaActividad, horaActividad, encargado_id, actividadState) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, actividad.getTitulo());
            stmt.setString(2, actividad.getDescripcion());
            stmt.setInt(3, actividad.getHorasOtorgadas());
            stmt.setInt(4, actividad.getCupoMaximo());
            stmt.setInt(5, actividad.getCupoUsado());
            stmt.setDate(6, Date.valueOf(actividad.getFechaActividad()));
            stmt.setTime(7, Time.valueOf(actividad.getHoraActividad()));
            stmt.setInt(8, actividad.getEncargadoId());
            stmt.setBoolean(9, actividad.isDisponible());

            stmt.executeUpdate();
        }
    }

    // Listar todas las actividades disponibles
    public List<Actividad> listarDisponibles() throws SQLException {
        List<Actividad> lista = new ArrayList<>();
        String sql = "SELECT * FROM Actividades WHERE actividadState = 1 AND cupoUsado < cupoMaximo";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Actividad a = new Actividad(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getInt("horasOtorgadas"),
                        rs.getInt("cupoMaximo"),
                        rs.getInt("cupoUsado"),
                        rs.getDate("fechaActividad").toLocalDate(),
                        rs.getTime("horaActividad").toLocalTime(),
                        rs.getInt("encargado_id"),
                        rs.getBoolean("actividadState")
                );
                lista.add(a);
            }
        }
        return lista;
    }

    // Inscribir alumno a activida
    public void inscribirAlumno(int actividadId) throws SQLException {
        String sql = "UPDATE Actividades SET cupoUsado = cupoUsado + 1 WHERE id = ? AND cupoUsado < cupoMaximo";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, actividadId);
            stmt.executeUpdate();
        }
    }

    // Cambiar estado de la actividad
    public void actualizarEstado(int actividadId, boolean estado) throws SQLException {
        String sql = "UPDATE Actividades SET actividadState = ? WHERE id = ?";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, estado);
            stmt.setInt(2, actividadId);
            stmt.executeUpdate();
        }
    }

    
    public Actividad obtenerPorId(int id) throws SQLException {
        String sql = "SELECT * FROM Actividades WHERE id = ?";
        try (Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Actividad(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getInt("horasOtorgadas"),
                        rs.getInt("cupoMaximo"),
                        rs.getInt("cupoUsado"),
                        rs.getDate("fechaActividad").toLocalDate(),
                        rs.getTime("horaActividad").toLocalTime(),
                        rs.getInt("encargado_id"),
                        rs.getBoolean("actividadState")
                );
            }
        }
        return null;
    }

    // Listar actividades por encargado
    public List<Actividad> listarPorEncargado(int encargadoId) throws SQLException {
        List<Actividad> lista = new ArrayList<>();
        String sql = "SELECT * FROM Actividades WHERE encargado_id = ?";

        try (Connection conn = DbConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, encargadoId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                lista.add(new Actividad(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("descripcion"),
                        rs.getInt("horasOtorgadas"),
                        rs.getInt("cupoMaximo"),
                        rs.getInt("cupoUsado"),
                        rs.getDate("fechaActividad").toLocalDate(),
                        rs.getTime("horaActividad").toLocalTime(),
                        rs.getInt("encargado_id"),
                        rs.getBoolean("actividadState")
                ));
            }
        }
        return lista;
    }
}
