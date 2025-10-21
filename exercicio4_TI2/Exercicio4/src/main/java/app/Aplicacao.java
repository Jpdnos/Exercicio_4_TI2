package app;

import static spark.Spark.*;

import service.AnaliseSentimentoService;

public class Aplicacao {
    
    private static AnaliseSentimentoService analiseService = new AnaliseSentimentoService();
    
    public static void main(String[] args) {
        port(6789);

        // Habilitar CORS
        enableCORS();

        // Rotas da API - COM OS NOVOS NOMES DOS MÉTODOS
        post("/analise", (request, response) -> analiseService.adicionarAnalise(request, response));

        get("/analise/:id", (request, response) -> analiseService.buscarAnalise(request, response));

        put("/analise/update/:id", (request, response) -> analiseService.atualizarAnalise(request, response));

        delete("/analise/delete/:id", (request, response) -> analiseService.removerAnalise(request, response));

        get("/analise", (request, response) -> analiseService.listarAnalises(request, response));
        
        // Rota de health check
        get("/health", (req, res) -> "{\"status\": \"API de Análise de Sentimentos funcionando!\"}");
        
        // Rota raiz
        get("/", (req, res) -> "{\"mensagem\": \"Bem-vindo à API de Análise de Sentimentos\"}");
        
        // Rota para verificar status do banco
        get("/status", (req, res) -> {
            return "{\"status\": \"API Online\", \"banco\": \"PostgreSQL Azure\"}";
        });
    }
    
    // Configuração CORS para permitir requisições de diferentes origens
    private static void enableCORS() {
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            response.type("application/json");
        });
    }
}