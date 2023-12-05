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

    public class PlayerWithDate {
        private Player player;
        private String date;

        public PlayerWithDate(Player player, String date) {
            this.player = player;
            this.date = date;
        }

        public Player getPlayer() {
            return player;
        }

        public String getDate() {
            return date;
        }
    }


    private Context context;
    private List<Player> players;

    public void setPlayers(List<Player> newPlayers) {
        this.players = newPlayers;
    }

    public PlayerAdapterAlbo(Context context, List<Player> players) {
        this.context = context;
        this.players = players;
    }

    @Override
    public int getCount() {
        if(players.size()==0)
            Log.i("pingpong NOOOOO", "NOOOO");
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

        Player player = players.get(position);

        // Setta l'immagine del giocatore (esempio: R.drawable.alex)
        int imageId = context.getResources().getIdentifier(player.getName().toLowerCase(), "drawable", context.getPackageName());
        playerImage.setImageResource(imageId);

        playerName.setText(player.getName());
        // Calcola i dettagli del giocatore come numero di vittorie o percentuale
        String details = "Vittorie: " + player.score+ " - Percentuale: " + player.victoryPercentage + "%";
        playerDetails.setText(details);

        return convertView;
    }
}
