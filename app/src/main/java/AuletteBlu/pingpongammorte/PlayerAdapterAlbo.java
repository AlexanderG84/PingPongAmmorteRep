package AuletteBlu.pingpongammorte;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
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


   // @Override
    public View _getView(int position, View convertView, ViewGroup parent) {
        // Logica per determinare se la posizione corrente ha più di un giocatore
        List<Player> playersAtPosition =new ArrayList<>();
        playersAtPosition.add( players.get(position).getPlayer()); // Assumendo che getPlayers() restituisca una lista di giocatori

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.player_list_item, parent, false);
        }

        LinearLayout mainLayout = (LinearLayout) convertView.findViewById(R.id.main_layout);
        mainLayout.removeAllViews();


        float weightPerPlayer = 1.0f / playersAtPosition.size();


        for (Player player : playersAtPosition) {
            // Crea un nuovo layout per ogni giocatore
            View playerView = LayoutInflater.from(context).inflate(R.layout.single_player_layout, null); // Assumendo che esista un layout per un singolo giocatore
            // Imposta il peso
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    weightPerPlayer);
            playerView.setLayoutParams(layoutParams);

            ImageView playerImageView = playerView.findViewById(R.id.playerImage);
            TextView playerName = playerView.findViewById(R.id.playerName);
            TextView playerDetails = playerView.findViewById(R.id.playerDetails);

            String date = players.get(position).getDate();
            TextView playerDate = playerView.findViewById(R.id.playerDate); // Aggiungi un TextView per la data nel tuo layout XML


            // Configura le view per ogni giocatore
            playerName.setText(player.getName());

            // Configura l'imageView...
            if (isAlboDoroView) {
                // Visualizza la data
                date = players.get(position).getDate();
                String details = "Vittorie: " + player.score+ " - Percentuale: " + player.victoryPercentage + "%";
                playerDetails.setText(details);
            } else {
                // Visualizza il conteggio
                date =Integer.toString(players.get(position).getCount());
                String details = " ";
                playerDetails.setText(details);
            }

            playerDate.setText(date);


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
               // return convertView;
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


            mainLayout.addView(playerView);
        }

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d("PlayerAdapter", "pingpong getView called for position: " + position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.player_list_item, parent, false);
        }

        ImageView playerImageView = convertView.findViewById(R.id.playerImage);
        TextView playerName = convertView.findViewById(R.id.playerName);
        TextView playerDetails = convertView.findViewById(R.id.playerDetails);
        TextView playerDate = convertView.findViewById(R.id.playerDate); // Aggiungi un TextView per la data nel tuo layout XML

        Player player = players.get(position).getPlayer();


        String date = players.get(position).getDate();

        playerName.setText(player.getName());
        // Calcola i dettagli del giocatore come numero di vittorie o percentuale

        if (isAlboDoroView) {
            // Visualizza la data
            date = players.get(position).getDate();
            String details = "Vittorie: " + player.score+ " - Percentuale: " + player.victoryPercentage + "%";
            playerDetails.setText(details);
        } else {
            // Visualizza il conteggio
            date =Integer.toString(players.get(position).getCount());
            String details = " ";
            playerDetails.setText(details);
        }

        playerDate.setText(date);


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
