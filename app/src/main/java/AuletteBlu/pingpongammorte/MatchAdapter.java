package AuletteBlu.pingpongammorte;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import AuletteBlu.pingpongammorte.Match;

public class MatchAdapter extends ArrayAdapter<Match> {

    private final List<Match> matches;
    private final Context context;



    public MatchAdapter(Context context, List<Match> matches) {
        super(context, R.layout.match_item, matches);
        this.context = context;
        this.matches = matches;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Ottiene l'oggetto Match per questa posizione specifica
        Match match = getItem(position);

        // Verifica se una vista esistente viene riutilizzata, altrimenti infila una nuova vista
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.match_item, parent, false);
        }

        // Trova le viste di testo nell'elemento match
        TextView winnerView = convertView.findViewById(R.id.winner);
        TextView loserView = convertView.findViewById(R.id.loser);
        TextView winnerScoreView = convertView.findViewById(R.id.winnerScore);
        TextView loserScoreView = convertView.findViewById(R.id.loserScore);
        TextView dateView = convertView.findViewById(R.id.date);
        TextView hourView = convertView.findViewById(R.id.hour);

        // Popola i campi con i dati estratti dall'oggetto Match
        winnerView.setText(match.getWinner());
        loserView.setText(match.getLoser());
        winnerScoreView.setText(String.valueOf(match.scoreWinner));
        loserScoreView.setText(String.valueOf(match.scoreLoser));
        dateView.setText(match.getDate());
        hourView.setText(match.getHour());
/*
        if (position == matches.size() - 1) {
            TextView player1WinsView = convertView.findViewById(R.id.player1Wins);
            TextView player2WinsView = convertView.findViewById(R.id.player2Wins);

            // Supponendo che player1 e player2 siano i nomi dei giocatori
            String player1 = matches.get(0).getWinner();
            String player2 = matches.get(0).getLoser();

            MatchUtils.VictoryCount victoryCount = MatchUtils.countVictories(matches, player1, player2);

// Ora hai il conteggio delle vittorie di entrambi i giocatori
            int victoriesPlayer1 = victoryCount.victoriesPlayer1;
            int victoriesPlayer2 = victoryCount.victoriesPlayer2;


            player1WinsView.setText(String.format("Vittorie %s: %d", player1, victoriesPlayer1));
            player2WinsView.setText(String.format("Vittorie %s: %d", player2, victoriesPlayer2));
        } */

        // Ritorna la vista completata per visualizzarla sullo schermo
        return convertView;
    }
}
