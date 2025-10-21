package dao;

import model.AnaliseSentimento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnaliseSentimentoDAO {
    private Connection connection;
    
    // Configurações do Azure PostgreSQL
    private final String URL = "jdbc:postgresql://postgre-exercicio4.postgres.database.azure.com:5432/sentiment_db?sslmode=require";
    private final String USUARIO = "adm_exe4";
    private final String SENHA = "@Canelas2";

    public AnaliseSentimentoDAO() {
        conectar();
    }

    private void conectar() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USUARIO, SENHA);
            System.out.println("✅ Conectado ao Azure PostgreSQL - sentiment_db");
        } catch (Exception e) {
            System.out.println("❌ ERRO na conexão: " + e.getMessage());
        }
    }

    public boolean adicionarAnalise(AnaliseSentimento analise) {
        String sql = "INSERT INTO sentiment_analyses (document_id, original_text, language_code, overall_sentiment, " +
                    "positive_score, neutral_score, negative_score, model_version) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, analise.getDocumentId());
            stmt.setString(2, analise.getTextoOriginal());
            stmt.setString(3, analise.getIdioma());
            stmt.setString(4, analise.getSentimento());
            stmt.setDouble(5, analise.getScorePositivo());
            stmt.setDouble(6, analise.getScoreNeutro());
            stmt.setDouble(7, analise.getScoreNegativo());
            stmt.setString(8, analise.getVersaoModelo());
            
            int linhasAfetadas = stmt.executeUpdate();
            
            if (linhasAfetadas > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        analise.setId(generatedKeys.getInt(1));
                        System.out.println("✅ Análise salva - ID: " + analise.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao salvar análise: " + e.getMessage());
        }
        return false;
    }

    public AnaliseSentimento buscarPorId(int id) {
        String sql = "SELECT * FROM sentiment_analyses WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapearResultado(rs);
            }
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao buscar análise: " + e.getMessage());
        }
        return null;
    }

    public boolean atualizarAnalise(AnaliseSentimento analise) {
        String sql = "UPDATE sentiment_analyses SET document_id=?, original_text=?, language_code=?, " +
                    "overall_sentiment=?, positive_score=?, neutral_score=?, negative_score=?, model_version=? " +
                    "WHERE id=?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, analise.getDocumentId());
            stmt.setString(2, analise.getTextoOriginal());
            stmt.setString(3, analise.getIdioma());
            stmt.setString(4, analise.getSentimento());
            stmt.setDouble(5, analise.getScorePositivo());
            stmt.setDouble(6, analise.getScoreNeutro());
            stmt.setDouble(7, analise.getScoreNegativo());
            stmt.setString(8, analise.getVersaoModelo());
            stmt.setInt(9, analise.getId());
            
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✅ Análise atualizada - ID: " + analise.getId());
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao atualizar análise: " + e.getMessage());
        }
        return false;
    }

    public boolean removerAnalise(int id) {
        String sql = "DELETE FROM sentiment_analyses WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int linhasAfetadas = stmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("✅ Análise removida - ID: " + id);
                return true;
            }
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao remover análise: " + e.getMessage());
        }
        return false;
    }

    public List<AnaliseSentimento> listarTodas() {
        List<AnaliseSentimento> analises = new ArrayList<>();
        String sql = "SELECT * FROM sentiment_analyses ORDER BY analysis_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                analises.add(mapearResultado(rs));
            }
            System.out.println("📊 Total de análises: " + analises.size());
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao listar análises: " + e.getMessage());
        }
        return analises;
    }

    private AnaliseSentimento mapearResultado(ResultSet rs) throws SQLException {
        AnaliseSentimento analise = new AnaliseSentimento();
        analise.setId(rs.getInt("id"));
        analise.setDocumentId(rs.getString("document_id"));
        analise.setTextoOriginal(rs.getString("original_text"));
        analise.setIdioma(rs.getString("language_code"));
        analise.setSentimento(rs.getString("overall_sentiment"));
        analise.setScorePositivo(rs.getDouble("positive_score"));
        analise.setScoreNeutro(rs.getDouble("neutral_score"));
        analise.setScoreNegativo(rs.getDouble("negative_score"));
        analise.setVersaoModelo(rs.getString("model_version"));
        
        Timestamp timestamp = rs.getTimestamp("analysis_date");
        if (timestamp != null) {
            analise.setDataAnalise(timestamp.toLocalDateTime());
        }
        
        return analise;
    }

    public void fecharConexao() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Conexão fechada");
            }
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao fechar conexão: " + e.getMessage());
        }
    }

    // Método para verificar se a tabela existe (útil para testes)
    public boolean verificarTabela() {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet tables = meta.getTables(null, null, "sentiment_analyses", null);
            return tables.next();
        } catch (SQLException e) {
            System.out.println("❌ ERRO ao verificar tabela: " + e.getMessage());
            return false;
        }
    }
}