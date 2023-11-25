package AuletteBlu.pingpongammorte;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MatchUtils {

    // Classe interna per tenere traccia delle vittorie
    public static class VictoryCount {
        public int victoriesPlayer1;
        public int victoriesPlayer2;

        public VictoryCount(int victoriesPlayer1, int victoriesPlayer2) {
            this.victoriesPlayer1 = victoriesPlayer1;
            this.victoriesPlayer2 = victoriesPlayer2;
        }
    }

    public static VictoryCount countVictories(List<Match> matches, String player1Name, String player2Name) {
        int victoriesPlayer1 = 0;
        int victoriesPlayer2 = 0;

        for (Match match : matches) {
            if (player1Name.equals("Tutti") || match.getWinner().equals(player1Name)) {
                victoriesPlayer1++;
            }
            if (player2Name.equals("Tutti") || match.getWinner().equals(player2Name)) {
                victoriesPlayer2++;
            }
        }

        return new VictoryCount(victoriesPlayer1, victoriesPlayer2);
    }

    public static List<Match> distinctMatch(List<Match> matches) {
        Set<String> seenTimestamps = new HashSet<>();
        List<Match> distinctMatches = new ArrayList<>();

        for (Match match : matches) {
            String timestamp = match.id_timestamp;

            // Aggiungi il match se il campo id_timestamp è vuoto o non è stato visto prima
            if (timestamp.isEmpty() || seenTimestamps.add(timestamp)) {
                distinctMatches.add(match);
            }
        }

        return distinctMatches;
    }

    public static List<Match> removeMatchesWithId(List<Match> matches, String idToRemove) {
        Iterator<Match> iterator = matches.iterator();

        while (iterator.hasNext()) {
            Match match = iterator.next();

            // Rimuovi il match se il campo id_timestamp è uguale a idToRemove e non è vuoto
            if (idToRemove.equals(match.id_timestamp) && !idToRemove.isEmpty()) {
                iterator.remove();
            }
        }
        return matches;
    }
}
