package com.yugiohduel.api;

import com.yugiohduel.model.Card;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Cliente para consumir la API de YGOProDeck
 */
public class YgoApiClient {
    private static final String API_URL = "https://db.ygoprodeck.com/api/v7/randomcard.php";
    private final HttpClient httpClient;

    public YgoApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    /**
     * Obtiene una carta Monster aleatoria desde la API
     * @return Card objeto con los datos de la carta
     * @throws Exception si hay error de red o la carta no es un monstruo
     */
    public Card getRandomMonsterCard() throws Exception {
        int maxAttempts = 10;

        for (int i = 0; i < maxAttempts; i++) {
            Card card = fetchRandomCard();

            if (card.isMonster()) {
                return card;
            }

            System.out.println("Carta obtenida no es monstruo: " + card.getName() +
                    " (Tipo: " + card.getType() + "). Reintentando...");
        }

        throw new Exception("No se pudo obtener una carta Monster después de " +
                maxAttempts + " intentos");
    }

    /**
     * Realiza la petición HTTP y parsea la respuesta JSON
     * @return Card objeto con los datos parseados
     * @throws Exception si hay error de red o parsing
     */
    private Card fetchRandomCard() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("HTTP Status: " + response.statusCode());
        System.out.println("Response body: " + response.body().substring(0, Math.min(200, response.body().length())));

        if (response.statusCode() != 200) {
            throw new Exception("Error HTTP: " + response.statusCode());
        }

        return parseCardFromJson(response.body());
    }

    /**
     * Convierte el JSON de respuesta en un objeto Card
     * @param jsonString String JSON de la respuesta
     * @return Card objeto parseado
     */
    private Card parseCardFromJson(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);

            // Verificar si hay un error en la respuesta
            if (json.has("error")) {
                throw new RuntimeException("API Error: " + json.getString("error"));
            }

            // La API devuelve los datos dentro de un array "data"
            if (!json.has("data")) {
                throw new RuntimeException("La respuesta no contiene el campo 'data'");
            }

            JSONArray dataArray = json.getJSONArray("data");
            if (dataArray.length() == 0) {
                throw new RuntimeException("El array 'data' está vacío");
            }

            // Obtener el primer elemento del array
            JSONObject cardData = dataArray.getJSONObject(0);

            String name = cardData.getString("name");
            String type = cardData.getString("type");

            int atk = cardData.optInt("atk", 0);
            int def = cardData.optInt("def", 0);

            // Verificar que exista el array de imágenes
            if (!cardData.has("card_images")) {
                throw new RuntimeException("La respuesta no contiene imágenes de la carta");
            }

            JSONArray cardImages = cardData.getJSONArray("card_images");
            if (cardImages.length() == 0) {
                throw new RuntimeException("El array de imágenes está vacío");
            }

            String imageUrl = cardImages.getJSONObject(0).getString("image_url");

            return new Card(name, atk, def, imageUrl, type);

        } catch (Exception e) {
            System.err.println("Error parseando JSON: " + e.getMessage());
            throw new RuntimeException("Error al parsear la carta: " + e.getMessage());
        }
    }
}