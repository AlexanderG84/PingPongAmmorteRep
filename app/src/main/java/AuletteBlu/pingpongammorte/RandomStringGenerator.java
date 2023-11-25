package AuletteBlu.pingpongammorte;

import java.util.Random;

public class RandomStringGenerator {
    public static String generateRandomString() {
        long timestamp = System.currentTimeMillis(); // Ottieni il timestamp attuale
        Random random = new Random(timestamp); // Inizializza l'oggetto Random con il timestamp

        // Genera una stringa casuale
        int length = 16; // Puoi regolare la lunghezza della stringa a tuo piacimento
        StringBuilder randomString = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char randomChar = (char) ('a' + random.nextInt(26)); // Genera una lettera minuscola casuale
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

    public static void main(String[] args) {
        // Esempio di utilizzo
        String randomString = generateRandomString();
        System.out.println("Stringa casuale: " + randomString);
    }
}

