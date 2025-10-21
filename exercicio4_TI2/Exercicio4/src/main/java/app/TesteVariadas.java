package app;

public class TesteVariadas {
    public static void main(String[] args) {
        testarFrase("Java é incrível para desenvolvimento!", "pt");
        testarFrase("Estou muito bravo com esses bugs!", "pt");
        testarFrase("Python também é uma boa linguagem", "pt");
        testarFrase("I absolutely love this application!", "en");
        testarFrase("This code needs serious refactoring", "en");
    }
    
    public static void testarFrase(String texto, String idioma) {
        try {
            String url = "http://localhost:6789/analise?texto=" + 
                java.net.URLEncoder.encode(texto, "UTF-8") + "&idioma=" + idioma;
            
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) 
                new java.net.URL(url).openConnection();
            con.setRequestMethod("POST");
            
            if (con.getResponseCode() == 201) {
                System.out.println("✅ \"" + texto + "\"");
            }
        } catch (Exception e) {
            System.out.println("❌ Erro: " + e.getMessage());
        }
    }
}