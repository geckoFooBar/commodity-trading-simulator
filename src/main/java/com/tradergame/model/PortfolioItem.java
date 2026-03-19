public class PortfolioItem {
    private int commodityId;
    private int quantity;
    private double averageBuyPrice;

    public PortfolioItem(int commodityId, int quantity, double averageBuyPrice) {
        this.commodityId = commodityId;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    // Getters and Setters
    public int getCommodityId() { return commodityId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getAverageBuyPrice() { return averageBuyPrice; }
    public void setAverageBuyPrice(double averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }
}