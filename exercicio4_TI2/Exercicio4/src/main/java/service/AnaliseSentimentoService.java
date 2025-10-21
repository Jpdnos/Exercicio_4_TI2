package service;

import dao.AnaliseSentimentoDAO;
import model.AnaliseSentimento;
import spark.Request;
import spark.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnaliseSentimentoService {
    private AnaliseSentimentoDAO analiseDAO;
    private Gson gson;
    private static final String AZURE_CONTAINER_URL = "http://localhost:5000";

    public AnaliseSentimentoService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.analiseDAO = new AnaliseSentimentoDAO();
    }

    public Object adicionarAnalise(Request request, Response response) {
        try {
            String texto = request.queryParams("texto");
            String idioma = request.queryParams("idioma");
            String documentId = request.queryParams("documentId");
            
            if (texto == null || texto.trim().isEmpty()) {
                response.status(400);
                return "{\"erro\": \"Texto não pode estar vazio\"}";
            }
            
            if (idioma == null) idioma = "pt";
            if (documentId == null) documentId = "doc_" + System.currentTimeMillis();
            
            // Analisar sentimento no container Azure
            Map<String, Object> resultadoAnalise = analisarSentimentoAzure(texto, idioma, documentId);
            
            if (resultadoAnalise == null) {
                response.status(500);
                return "{\"erro\": \"Erro ao analisar sentimento - verifique se o container está rodando\"}";
            }
            
            // Criar e salvar análise
            AnaliseSentimento analise = new AnaliseSentimento();
            analise.setDocumentId(documentId);
            analise.setTextoOriginal(texto);
            analise.setIdioma(idioma);
            analise.setSentimento((String) resultadoAnalise.get("sentiment"));
            
            Map<String, Double> scores = (Map<String, Double>) resultadoAnalise.get("confidenceScores");
            analise.setScorePositivo(scores.get("positive"));
            analise.setScoreNeutro(scores.get("neutral"));
            analise.setScoreNegativo(scores.get("negative"));
            analise.setVersaoModelo((String) resultadoAnalise.get("modelVersion"));
            
            boolean sucesso = analiseDAO.adicionarAnalise(analise);
            
            if (sucesso) {
                response.status(201);
                response.type("application/json");
                
                // Retornar um mapa simples em vez do objeto diretamente
                Map<String, Object> resposta = new HashMap<>();
                resposta.put("id", analise.getId());
                resposta.put("documentId", analise.getDocumentId());
                resposta.put("textoOriginal", analise.getTextoOriginal());
                resposta.put("idioma", analise.getIdioma());
                resposta.put("sentimento", analise.getSentimento());
                resposta.put("scorePositivo", analise.getScorePositivo());
                resposta.put("scoreNeutro", analise.getScoreNeutro());
                resposta.put("scoreNegativo", analise.getScoreNegativo());
                resposta.put("versaoModelo", analise.getVersaoModelo());
                resposta.put("mensagem", "Análise salva com sucesso");
                
                return gson.toJson(resposta);
            } else {
                response.status(500);
                return "{\"erro\": \"Falha ao salvar no banco de dados\"}";
            }
            
        } catch (Exception e) {
            response.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object buscarAnalise(Request request, Response response) {
        try {
            int id = Integer.parseInt(request.params(":id"));
            AnaliseSentimento analise = analiseDAO.buscarPorId(id);
            
            if (analise != null) {
                response.type("application/json");
                
                // Retornar mapa simples
                Map<String, Object> analiseMap = new HashMap<>();
                analiseMap.put("id", analise.getId());
                analiseMap.put("documentId", analise.getDocumentId());
                analiseMap.put("textoOriginal", analise.getTextoOriginal());
                analiseMap.put("idioma", analise.getIdioma());
                analiseMap.put("sentimento", analise.getSentimento());
                analiseMap.put("scorePositivo", analise.getScorePositivo());
                analiseMap.put("scoreNeutro", analise.getScoreNeutro());
                analiseMap.put("scoreNegativo", analise.getScoreNegativo());
                analiseMap.put("versaoModelo", analise.getVersaoModelo());
                
                return gson.toJson(analiseMap);
            } else {
                response.status(404);
                return "{\"erro\": \"Análise " + id + " não encontrada\"}";
            }
        } catch (Exception e) {
            response.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object atualizarAnalise(Request request, Response response) {
        try {
            int id = Integer.parseInt(request.params(":id"));
            AnaliseSentimento analise = analiseDAO.buscarPorId(id);
            
            if (analise != null) {
                if (request.queryParams("texto") != null) {
                    analise.setTextoOriginal(request.queryParams("texto"));
                }
                if (request.queryParams("idioma") != null) {
                    analise.setIdioma(request.queryParams("idioma"));
                }
                if (request.queryParams("sentimento") != null) {
                    analise.setSentimento(request.queryParams("sentimento"));
                }
                if (request.queryParams("scorePositivo") != null) {
                    analise.setScorePositivo(Double.parseDouble(request.queryParams("scorePositivo")));
                }
                if (request.queryParams("scoreNeutro") != null) {
                    analise.setScoreNeutro(Double.parseDouble(request.queryParams("scoreNeutro")));
                }
                if (request.queryParams("scoreNegativo") != null) {
                    analise.setScoreNegativo(Double.parseDouble(request.queryParams("scoreNegativo")));
                }
                
                boolean sucesso = analiseDAO.atualizarAnalise(analise);
                
                if (sucesso) {
                    response.type("application/json");
                    
                    Map<String, Object> resposta = new HashMap<>();
                    resposta.put("id", analise.getId());
                    resposta.put("mensagem", "Análise atualizada com sucesso");
                    
                    return gson.toJson(resposta);
                } else {
                    response.status(500);
                    return "{\"erro\": \"Falha ao atualizar análise\"}";
                }
            } else {
                response.status(404);
                return "{\"erro\": \"Análise não encontrada\"}";
            }
        } catch (Exception e) {
            response.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object removerAnalise(Request request, Response response) {
        try {
            int id = Integer.parseInt(request.params(":id"));
            boolean sucesso = analiseDAO.removerAnalise(id);
            
            if (sucesso) {
                response.status(200);
                return "{\"mensagem\": \"Análise " + id + " removida com sucesso\"}";
            } else {
                response.status(404);
                return "{\"erro\": \"Análise não encontrada\"}";
            }
        } catch (Exception e) {
            response.status(500);
            return "{\"erro\": \"" + e.getMessage() + "\"}";
        }
    }

    public Object listarAnalises(Request request, Response response) {
        try {
            response.type("application/json");
            
            // Buscar análises do DAO
            List<AnaliseSentimento> analises = analiseDAO.listarTodas();
            
            // Criar uma lista de mapas simples para serialização
            List<Map<String, Object>> analisesSimples = new ArrayList<>();
            
            for (AnaliseSentimento analise : analises) {
                Map<String, Object> analiseMap = new HashMap<>();
                analiseMap.put("id", analise.getId());
                analiseMap.put("documentId", analise.getDocumentId());
                analiseMap.put("textoOriginal", analise.getTextoOriginal());
                analiseMap.put("idioma", analise.getIdioma());
                analiseMap.put("sentimento", analise.getSentimento());
                analiseMap.put("scorePositivo", analise.getScorePositivo());
                analiseMap.put("scoreNeutro", analise.getScoreNeutro());
                analiseMap.put("scoreNegativo", analise.getScoreNegativo());
                analiseMap.put("versaoModelo", analise.getVersaoModelo());
                
                analisesSimples.add(analiseMap);
            }
            
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("analises", analisesSimples);
            resultado.put("total", analises.size());
            resultado.put("mensagem", "Análises recuperadas com sucesso");
            
            return gson.toJson(resultado);
            
        } catch (Exception e) {
            response.status(500);
            return "{\"erro\": \"Erro ao listar análises: " + e.getMessage() + "\"}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> analisarSentimentoAzure(String texto, String idioma, String documentId) {
        try {
            URL url = new URL(AZURE_CONTAINER_URL + "/text/analytics/v3.1/sentiment");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            
            String jsonPayload = String.format(
                "{\"documents\":[{\"id\":\"%s\",\"text\":\"%s\",\"language\":\"%s\"}]}",
                documentId, texto.replace("\"", "\\\""), idioma
            );
            
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                reader.close();
                
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonArray documents = jsonResponse.getAsJsonArray("documents");
                
                if (documents.size() > 0) {
                    JsonObject firstDocument = documents.get(0).getAsJsonObject();
                    Map<String, Object> result = new HashMap<>();
                    
                    result.put("sentiment", firstDocument.get("sentiment").getAsString());
                    result.put("modelVersion", jsonResponse.get("modelVersion").getAsString());
                    
                    JsonObject scores = firstDocument.getAsJsonObject("confidenceScores");
                    Map<String, Double> confidenceScores = new HashMap<>();
                    confidenceScores.put("positive", scores.get("positive").getAsDouble());
                    confidenceScores.put("neutral", scores.get("neutral").getAsDouble());
                    confidenceScores.put("negative", scores.get("negative").getAsDouble());
                    
                    result.put("confidenceScores", confidenceScores);
                    return result;
                }
            } else {
                System.out.println("❌ ERRO Azure - Código: " + responseCode);
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            System.out.println("❌ ERRO na análise Azure: " + e.getMessage());
        }
        return null;
    }
}