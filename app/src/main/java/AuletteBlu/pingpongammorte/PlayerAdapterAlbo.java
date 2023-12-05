package AuletteBlu.pingpongammorte;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class PlayerAdapterAlbo extends BaseAdapter {


    private boolean isAlboDoroView; // True per Albo d'Oro, false per Classifica

    private Context context;
    private List<PlayerWithDate> players;

    public void setPlayersWithDate(List<PlayerWithDate> newPlayersWithDate) {
        this.players = newPlayersWithDate;
    }

    public void setIsAlboDoroView(boolean isAlboDoroView) {
        this.isAlboDoroView = isAlboDoroView;
    }

    public void setPlayers(List<PlayerWithDate> newPlayers) {
        this.players = newPlayers;
    }

    public PlayerAdapterAlbo(Context context, List<PlayerWithDate> players) {
        this.context = context;
        this.players = players;
    }

    @Override
    public int getCount() {
        return players.size();
    }

    @Override
    public Object getItem(int position) {
        return players.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d("PlayerAdapter", "pingpong getView called for position: " + position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.player_list_item, parent, false);
        }

        ImageView playerImage = convertView.findViewById(R.id.playerImage);
        TextView playerName = convertView.findViewById(R.id.playerName);
        TextView playerDetails = convertView.findViewById(R.id.playerDetails);
        TextView playerDate = convertView.findViewById(R.id.playerDate); // Aggiungi un TextView per la data nel tuo layout XML

        Player player = players.get(position).getPlayer();

        // Setta l'immagine del giocatore (esempio: R.drawable.alex)
        int imageId = context.getResources().getIdentifier(player.getName().toLowerCase(), "drawable", context.getPackageName());
        playerImage.setImageResource(imageId);
        String date = players.get(position).getDate();

        playerName.setText(player.getName());
        // Calcola i dettagli del giocatore come numero di vittorie o percentuale
        String details = "Vittorie: " + player.score+ " - Percentuale: " + player.victoryPercentage + "%";
        playerDetails.setText(details);
        if (isAlboDoroView) {
            // Visualizza la data
            date = players.get(position).getDate();
        } else {
            // Visualizza il conteggio
            date =Integer.toString(players.get(position).getCount());
        }

        playerDate.setText(date);
        return convertView;
    }
}
