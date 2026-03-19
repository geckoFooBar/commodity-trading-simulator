public class Commodity {
    private int commodityId;
    private String name;
    private double currentPrice;

    public Commodity(int commodityId, String name, double currentPrice) {
        this.commodityId = commodityId;
        this.name = name;
        this.currentPrice = currentPrice;
    }

    public int getCommodityId() { return commodityId; }
    public String getName() { return name; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
}