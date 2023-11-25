package AuletteBlu.pingpongammorte;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class PlayerAdapter extends ArrayAdapter<Player> {
    private Context context;
    private List<Player> players;

    public PlayerAdapter(@NonNull Context context, int resource, @NonNull List<Player> players) {
        super(context, resource, players);
        this.context = context;
        this.players = players;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_player, parent, false);
        }

        TextView playerNameTextView = convertView.findViewById(R.id.playerNameTextView);
        TextView playerScoreTextView = convertView.findViewById(R.id.playerScoreTextView);
        TextView matchesCountTextView = convertView.findViewById(R.id.tv_matches_count);
        TextView victoryPercentageTextView = convertView.findViewById(R.id.tv_victory_percentage);

        Player player = players.get(position);

        playerNameTextView.setText(player.getName());
        playerScoreTextView.setText(String.valueOf(player.getScore()));

        // Calcolo numero di match e percentuale di vittorie
        //int totalMatches = player.getMatches().size();
        long totalMatches = player.getMatchesFiltered();
        //long victoryPercentage = totalMatches > 0 ? (player.getScore() * 100) / totalMatches : 0;
        long victoryPercentage = player.victoryPercentage;
        matchesCountTextView.setText("Matches: " + totalMatches);
        victoryPercentageTextView.setText("Wins: " + victoryPercentage + "%");


        if (LeaderboardActivity.orderByVict) {
            playerScoreTextView.setBackgroundColor(Color.parseColor("#FF0000")); // Rosso
            victoryPercentageTextView.setBackgroundColor(Color.TRANSPARENT); // Senza colore
        } else if (!LeaderboardActivity.orderByVict) {
            victoryPercentageTextView.setBackgroundColor(Color.parseColor("#FF0000")); // Rosso
            playerScoreTextView.setBackgroundColor(Color.TRANSPARENT); // Senza colore
        }


        ImageView playerImageView = convertView.findViewById(R.id.playerImageView);


// Divisione del nome se contiene il simbolo "-"
        String[] playerNames = player.getName().split("-");
        String firstName = playerNames[0].trim(); // Nome del primo giocatore
        String secondName = playerNames.length > 1 ? playerNames[1].trim() : ""; // Nome del secondo giocatore, se presente

        if (secondName.equals("")) {
            int resIdFirst = context.getResources().getIdentifier(firstName.toLowerCase(), "drawable", context.getPackageName());

            if (resIdFirst != 0) {
                playerImageView.setImageResource(resIdFirst);
            } else {
                playerImageView.setImageResource(R.drawable.default_player_image); // Immagine di default se non trovata
            }
            return convertView;
        }

// Caricamento delle immagini
        int resIdFirst = context.getResources().getIdentifier(firstName.toLowerCase(), "drawable", context.getPackageName());
        int resIdSecond = context.getResources().getIdentifier(secondName.toLowerCase(), "drawable", context.getPackageName());

        if (resIdFirst == 0)
            resIdFirst = context.getResources().getIdentifier("default_player_image", "drawable", context.getPackageName());
        ;
        if (resIdSecond == 0)
            resIdSecond = context.getResources().getIdentifier("default_player_image", "drawable", context.getPackageName());
        ;


        // Creazione di un'immagine composta se entrambe le immagini sono state trovate

// Creazione di un'immagine composta se entrambe le immagini sono state trovate

// Creazione di un'immagine composta se entrambe le immagini sono state trovate
        if (resIdFirst != 0 && resIdSecond != 0) {
            // Ridimensionamento delle immagini senza caricarle completamente
            Bitmap bitmapFirst = decodeSampledBitmapFromResource(resIdFirst, 100); // Imposta le dimensioni desiderate
            Bitmap bitmapSecond = decodeSampledBitmapFromResource(resIdSecond, 100); // Imposta le dimensioni desiderate

            // Calcolo delle dimensioni per la composizione
            int width = bitmapFirst.getWidth() + bitmapSecond.getWidth();
            int height = Math.max(bitmapFirst.getHeight(), bitmapSecond.getHeight());

// Calcolo dello spostamento verticale per centrare le immagini
            int verticalOffsetFirst = (height - bitmapFirst.getHeight()) / 2;
            int verticalOffsetSecond = (height - bitmapSecond.getHeight()) / 2;

// Creazione di un'immagine composta
            Bitmap combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(combinedBitmap);

// Disegno della prima immagine centrata
            canvas.drawBitmap(bitmapFirst, 0, verticalOffsetFirst, null);

// Disegno della seconda immagine centrata
            canvas.drawBitmap(bitmapSecond, bitmapFirst.getWidth(), verticalOffsetSecond, null);

// Impostazione dell'immagine composta nella ImageView
            playerImageView.setImageBitmap(combinedBitmap);

// Rilascio delle risorse delle immagini
            bitmapFirst.recycle();
            bitmapSecond.recycle();
        } else {
            // Se una delle due immagini non è stata trovata, utilizza l'immagine di default
            playerImageView.setImageResource(R.drawable.default_player_image);
        }

        return convertView;
    }

    // Funzione per ottenere l'immagine di default
    private Bitmap getDefaultBitmap() {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_player_image);
    }

    // Funzione per ridimensionare direttamente le immagini
    private Bitmap decodeSampledBitmapFromResource(int resId, int reqSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);

        // Calcola il sample size in modo che l'immagine non superi le dimensioni richieste
        options.inSampleSize = calculateInSampleSize(options, reqSize);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }


    // Funzione per calcolare il sample size in modo da ridimensionare la bitmap
    private int calculateInSampleSize(BitmapFactory.Options options, int reqSize) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqSize || width > reqSize) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqSize || (halfWidth / inSampleSize) >= reqSize) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}

