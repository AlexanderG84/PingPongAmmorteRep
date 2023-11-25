package AuletteBlu.pingpongammorte;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private String name;
    private long filteredScore;  // Punteggio filtrato per la visualizzazione nella leaderboard

    public String player1="";
    public String player2="";

    private List<Match> matches = new ArrayList<>();
    private List<String> victoryDates = new ArrayList<>();

    private List<String> victoriesAgainst;
    public int score;

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public Player deepClone() {
        Gson gson = new Gson();
        String jsonString = gson.toJson(this);
        return gson.fromJson(jsonString, Player.class);
    }

    public void addMatch(Match match) {
        if (matches == null) {
            matches = new ArrayList<>();
        }
        matches.add(match);
    }

    public long getMatchesFiltered() {
        return filteredScore;
    }

    public void setFilteredScore(long score) {
        this.filteredScore = score;
    }

    public List<String> getVictoryDates() {
        return victoryDates;
    }
    public void addVictory(String date) {
        // ... incrementa il punteggio o qualsiasi altra logica necessaria
        victoryDates.add(date);
    }



    // Aggiunge metodi utili per ottenere il numero di vittorie, perdite, ecc.
    public int getVictories() {
        int victories = 0;
        for (Match match : matches) {
            if (match.getWinner().equals(this)) {
                victories++;
            }
        }
        return victories;
    }

    public Player(String name, int score) {
        this.name = name;
        this.score = score;
        this.victoriesAgainst = new ArrayList<>();
    }
    public List<String> getVictoriesAgainst() {
        return victoriesAgainst;
    }
    public boolean playedOnDate(String date) {
        for (Match match : matches) {
            if (match.getDate().equals(date)) {
                return true;  // Il giocatore ha giocato una partita in questa data.
            }
        }
        return false;  // Il giocatore non ha giocato nessuna partita in questa data.
    }

    public ArrayList<Match> getMatchesOnDateWithOpponents(List<String> opponentNames, String date, String type) {
        int count = 0;
    ArrayList<Match> founded=new ArrayList<>();
        for (Match match : matches) {
            if (match.getWinner().equals(this.name)&&match.type.equals(type) &&
                    opponentNames.contains(match.getLoser()) &&
                    !match.getLoser().equals(this.name) &&
                    (date.equals("Tutte le Date") || match.getDate().equals(date))) {
                founded.add(match);
                count++;
            }
        }
        score=count;

        for (Match match : matches) {
            if (match.getLoser().equals(this.name)&&match.type.equals(type) &&
                    opponentNames.contains(match.getWinner()) &&
                    !match.getWinner().equals(this.name) &&
                    (date.equals("Tutte le Date") || match.getDate().equals(date))) {
                founded.add(match);
                count++;
            }
        }

        filteredScore=count;
         victoryPercentage = filteredScore > 0 ? (score * 100) / filteredScore : 0;

        return founded;
    }

    public long victoryPercentage;
    public void addVictoryAgainst(String opponentName) {
        this.victoriesAgainst.add(opponentName);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {

        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return name;
    }
}

