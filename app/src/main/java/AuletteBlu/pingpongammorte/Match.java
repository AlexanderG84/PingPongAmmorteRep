package AuletteBlu.pingpongammorte;

import java.io.Serializable;

public class Match implements Serializable {
    private String winner;
    private String loser;

    public int scoreWinner=-1;
    public int scoreLoser=-1;

    public String id_timestamp="";
    private String date;

    public String type="PP1v1";

    public String hour="";

    public Match(String winner, String loser, String date, String type, int scoreWin, int scorLos,String _hour,String id) {
        this.winner = winner;
        this.loser = loser;
        this.date = date;
        this.type=type;
        this.scoreWinner=scoreWin;
        this.scoreLoser=scorLos;
        this.hour=_hour;
        this.id_timestamp=id;
    }

    public String getHour() {
        return hour;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getLoser() {
        return loser;
    }

    public void setLoser(String loser) {
        this.loser = loser;
    }

    public String getOpponent(String currentPlayer) {
        // Restituisce l'avversario in un match dato un giocatore
        if (currentPlayer.equals(winner)) {
            return loser;
        } else {
            return winner;
        }
    }
}
