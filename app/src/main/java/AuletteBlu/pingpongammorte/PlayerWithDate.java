package AuletteBlu.pingpongammorte;
public class PlayerWithDate {
    private Player player;
    private String date; // Usato per l'Albo d'Oro
    private int count; // Usato per la Classifica

    // Aggiungi un costruttore che accetta sia la data sia il conteggio
    public PlayerWithDate(Player player, String date, int count) {
        this.player = player;
        this.date = date;
        this.count = count;
    }


    public Player getPlayer() {
        return player;
    }

    public int getCount() {
        return count;
    }

    public String getDate() {
        return date;
    }
}

