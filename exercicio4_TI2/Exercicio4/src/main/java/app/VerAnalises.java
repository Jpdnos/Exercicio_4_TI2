package app;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class VerAnalises {
    public static void main(String[] args) {
        listarAnalisesDetalhadas();
    }
    
    public static void listarAnalisesDetalhadas() {
        try {
            System.out.println("=== ANÁLISES SALVAS NO BANCO ===\n");
            
            URL url = new URL("http://localhost:6789/analise");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            
            int responseCode = con.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();
                
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                // Formatar a saída para melhor leitura
                String json = response.toString();
                System.out.println("📊 TOTAL DE ANÁLISES: " + contarAnalises(json));
                System.out.println("📄 DETALHES:\n" + formatarJSON(json));
                
            } else {
                System.out.println("❌ Erro ao buscar análises: " + responseCode);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }
    
    private static int contarAnalises(String json) {
        int count = 0;
        int index = 0;
        while ((index = json.indexOf("\"id\"", index)) != -1) {
            count++;
            index += 4;
        }
        return count;
    }
    
    private static String formatarJSON(String json) {
        // Substituir vírgulas e chaves por quebras de linha para melhor legibilidade
        return json.replace("{", "\n{")
                  .replace("},", "},\n")
                  .replace("\"id\"", "  🆔 ID")
                  .replace("\"textoOriginal\"", "  📝 Texto")
                  .replace("\"sentimento\"", "  😊 Sentimento")
                  .replace("\"scorePositivo\"", "  📈 Positivo")
                  .replace("\"scoreNeutro\"", "  📊 Neutro")
                  .replace("\"scoreNegativo\"", "  📉 Negativo")
                  .replace("\"idioma\"", "  🌐 Idioma");
    }
}
