package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AnaliseSentimento implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String documentId;
    private String textoOriginal;
    private String idioma;
    private String sentimento;
    private double scorePositivo;
    private double scoreNeutro;
    private double scoreNegativo;
    private String versaoModelo;
    private LocalDateTime dataAnalise;
    
    public AnaliseSentimento() {
        this.id = -1;
        this.documentId = "";
        this.textoOriginal = "";
        this.idioma = "pt";
        this.sentimento = "neutral";
        this.scorePositivo = 0.0;
        this.scoreNeutro = 0.0;
        this.scoreNegativo = 0.0;
        this.versaoModelo = "";
        this.dataAnalise = LocalDateTime.now();
    }
    
    public AnaliseSentimento(int id, String documentId, String textoOriginal, String idioma, 
                           String sentimento, double scorePositivo, double scoreNeutro, 
                           double scoreNegativo, String versaoModelo) {
        this.id = id;
        this.documentId = documentId;
        this.textoOriginal = textoOriginal;
        this.idioma = idioma;
        this.sentimento = sentimento;
        this.scorePositivo = scorePositivo;
        this.scoreNeutro = scoreNeutro;
        this.scoreNegativo = scoreNegativo;
        this.versaoModelo = versaoModelo;
        this.dataAnalise = LocalDateTime.now();
    }
    
    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    
    public String getTextoOriginal() { return textoOriginal; }
    public void setTextoOriginal(String textoOriginal) { 
        if (textoOriginal != null && textoOriginal.length() >= 1)
            this.textoOriginal = textoOriginal; 
    }
    
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
    
    public String getSentimento() { return sentimento; }
    public void setSentimento(String sentimento) { this.sentimento = sentimento; }
    
    public double getScorePositivo() { return scorePositivo; }
    public void setScorePositivo(double scorePositivo) { 
        if (scorePositivo >= 0 && scorePositivo <= 1)
            this.scorePositivo = scorePositivo; 
    }
    
    public double getScoreNeutro() { return scoreNeutro; }
    public void setScoreNeutro(double scoreNeutro) { 
        if (scoreNeutro >= 0 && scoreNeutro <= 1)
            this.scoreNeutro = scoreNeutro; 
    }
    
    public double getScoreNegativo() { return scoreNegativo; }
    public void setScoreNegativo(double scoreNegativo) { 
        if (scoreNegativo >= 0 && scoreNegativo <= 1)
            this.scoreNegativo = scoreNegativo; 
    }
    
    public String getVersaoModelo() { return versaoModelo; }
    public void setVersaoModelo(String versaoModelo) { this.versaoModelo = versaoModelo; }
    
    public LocalDateTime getDataAnalise() { return dataAnalise; }
    public void setDataAnalise(LocalDateTime dataAnalise) { this.dataAnalise = dataAnalise; }
    
    @Override
    public String toString() {
        return "Análise ID: " + id + 
               " | Texto: " + (textoOriginal.length() > 30 ? textoOriginal.substring(0, 30) + "..." : textoOriginal) +
               " | Sentimento: " + sentimento + 
               " | Scores: P=" + String.format("%.2f", scorePositivo) + 
               " N=" + String.format("%.2f", scoreNeutro) + 
               " N=" + String.format("%.2f", scoreNegativo);
    }
    
    @Override
    public boolean equals(Object obj) {
        return (this.getId() == ((AnaliseSentimento) obj).getId());
    }
}