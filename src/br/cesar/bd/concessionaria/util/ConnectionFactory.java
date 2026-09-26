package br.cesar.bd.concessionaria.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final String BANCO = "concessionaria";

    private static final String URL = "jdbc:mysql://localhost:3306/" + BANCO
            + "?useSSL=false"
            + "&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo";

    private static final String USER = "root";
    private static final String PASS = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /** Nome do banco configurado (usado nas mensagens de erro da interface). */
    public static String getNomeBanco() {
        return BANCO;
    }

    /** Abre e fecha uma conexao apenas para conferir se o banco responde. */
    public static void testar() throws SQLException {
        try (Connection conn = getConnection()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Nao foi possivel abrir a conexao.");
            }
        }
    }
}