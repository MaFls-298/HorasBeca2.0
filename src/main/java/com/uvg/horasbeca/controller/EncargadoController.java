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



}
