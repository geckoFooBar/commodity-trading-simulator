public class Player {
    private int playerId;
    private String username;
    private double cashBalance;

    public Player(int playerId, String username, double cashBalance) {
        this.playerId = playerId;
        this.username = username;
        this.cashBalance = cashBalance;
    }

    // Getters and Setters
    public int getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public double getCashBalance() { return cashBalance; }
    public void setCashBalance(double cashBalance) { this.cashBalance = cashBalance; }
}