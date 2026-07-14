import java.io.*;
import java.util.*;

/**
 * CodeAlpha - Task 2: Stock Trading Platform
 *
 * A console-based simulation of a stock trading environment.
 * Features:
 *  - Simulated market data with fluctuating prices
 *  - Buy / sell operations
 *  - Portfolio tracking (holdings, cash balance, transaction history)
 *  - File I/O to save/load the portfolio so it persists between runs
 */
public class StockTradingPlatform {

    // ---------- Stock model ----------
    static class Stock {
        String symbol;
        String name;
        double price;

        Stock(String symbol, String name, double price) {
            this.symbol = symbol;
            this.name = name;
            this.price = price;
        }

        // Simulate a random market price fluctuation of up to +/-5%
        void fluctuate(Random rand) {
            double changePercent = (rand.nextDouble() * 10 - 5) / 100.0; // -5% to +5%
            price = Math.max(1.0, price * (1 + changePercent));
        }
    }

    // ---------- Transaction record ----------
    static class Transaction implements Serializable {
        String type;   // BUY or SELL
        String symbol;
        int quantity;
        double priceAtTransaction;
        long timestamp;

        Transaction(String type, String symbol, int quantity, double priceAtTransaction) {
            this.type = type;
            this.symbol = symbol;
            this.quantity = quantity;
            this.priceAtTransaction = priceAtTransaction;
            this.timestamp = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%tF %<tT] %-4s %4d x %-5s @ $%.2f",
                    timestamp, type, quantity, symbol, priceAtTransaction);
        }
    }

    // ---------- Portfolio ----------
    static class Portfolio implements Serializable {
        double cashBalance;
        Map<String, Integer> holdings = new HashMap<>();
        List<Transaction> history = new ArrayList<>();

        Portfolio(double startingCash) {
            this.cashBalance = startingCash;
        }

        void buy(Stock stock, int qty) {
            double cost = stock.price * qty;
            cashBalance -= cost;
            holdings.merge(stock.symbol, qty, Integer::sum);
            history.add(new Transaction("BUY", stock.symbol, qty, stock.price));
        }

        void sell(Stock stock, int qty) {
            double proceeds = stock.price * qty;
            cashBalance += proceeds;
            holdings.merge(stock.symbol, -qty, Integer::sum);
            if (holdings.get(stock.symbol) <= 0) holdings.remove(stock.symbol);
            history.add(new Transaction("SELL", stock.symbol, qty, stock.price));
        }

        double getHoldingsValue(Market market) {
            double total = 0;
            for (Map.Entry<String, Integer> e : holdings.entrySet()) {
                Stock s = market.getStock(e.getKey());
                if (s != null) total += s.price * e.getValue();
            }
            return total;
        }

        double getTotalValue(Market market) {
            return cashBalance + getHoldingsValue(market);
        }
    }

    // ---------- Market ----------
    static class Market {
        private final Map<String, Stock> stocks = new LinkedHashMap<>();
        private final Random rand = new Random();

        Market() {
            addStock("AAPL", "Apple Inc.", 195.00);
            addStock("GOOG", "Alphabet Inc.", 168.50);
            addStock("MSFT", "Microsoft Corp.", 425.00);
            addStock("AMZN", "Amazon.com Inc.", 185.75);
            addStock("TSLA", "Tesla Inc.", 245.30);
            addStock("NFLX", "Netflix Inc.", 680.10);
        }

        void addStock(String symbol, String name, double price) {
            stocks.put(symbol, new Stock(symbol, name, price));
        }

        Stock getStock(String symbol) {
            return stocks.get(symbol.toUpperCase());
        }

        Collection<Stock> getAllStocks() {
            return stocks.values();
        }

        void updatePrices() {
            for (Stock s : stocks.values()) s.fluctuate(rand);
        }

        void displayMarket() {
            System.out.println("\n----------- MARKET DATA -----------");
            System.out.printf("%-6s %-18s %-10s%n", "Symbol", "Name", "Price");
            for (Stock s : stocks.values()) {
                System.out.printf("%-6s %-18s $%-9.2f%n", s.symbol, s.name, s.price);
            }
            System.out.println("------------------------------------");
        }
    }

    // ---------- Persistence ----------
    private static final String SAVE_FILE = "portfolio.dat";

    static void savePortfolio(Portfolio portfolio) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(portfolio);
            System.out.println("Portfolio saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.out.println("Error saving portfolio: " + e.getMessage());
        }
    }

    static Portfolio loadPortfolio() {
        File f = new File(SAVE_FILE);
        if (!f.exists()) {
            System.out.println("No saved portfolio found. Starting fresh with $10,000.00 cash.");
            return new Portfolio(10000.00);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            Portfolio p = (Portfolio) ois.readObject();
            System.out.println("Portfolio loaded from " + SAVE_FILE);
            return p;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading portfolio, starting fresh: " + e.getMessage());
            return new Portfolio(10000.00);
        }
    }

    // ---------- Main program ----------
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("     STOCK TRADING PLATFORM (CodeAlpha)");
        System.out.println("==========================================");

        Market market = new Market();
        Portfolio portfolio = loadPortfolio();

        boolean running = true;
        while (running) {
            market.updatePrices(); // simulate live market movement each loop
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    market.displayMarket();
                    break;
                case "2":
                    buyStock(market, portfolio);
                    break;
                case "3":
                    sellStock(market, portfolio);
                    break;
                case "4":
                    viewPortfolio(market, portfolio);
                    break;
                case "5":
                    viewHistory(portfolio);
                    break;
                case "6":
                    savePortfolio(portfolio);
                    break;
                case "7":
                    savePortfolio(portfolio);
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println("\n--- MENU ---");
        System.out.println("1. View market data");
        System.out.println("2. Buy stock");
        System.out.println("3. Sell stock");
        System.out.println("4. View portfolio");
        System.out.println("5. View transaction history");
        System.out.println("6. Save portfolio");
        System.out.println("7. Save & exit");
        System.out.print("Enter choice: ");
    }

    private static void buyStock(Market market, Portfolio portfolio) {
        market.displayMarket();
        System.out.print("Enter symbol to buy: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println("Unknown symbol.");
            return;
        }
        System.out.print("Enter quantity: ");
        int qty = readPositiveInt();
        if (qty <= 0) return;

        double cost = stock.price * qty;
        if (cost > portfolio.cashBalance) {
            System.out.printf("Insufficient funds. Cost $%.2f, available $%.2f%n", cost, portfolio.cashBalance);
            return;
        }
        portfolio.buy(stock, qty);
        System.out.printf("Bought %d shares of %s for $%.2f%n", qty, symbol, cost);
    }

    private static void sellStock(Market market, Portfolio portfolio) {
        System.out.print("Enter symbol to sell: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println("Unknown symbol.");
            return;
        }
        int owned = portfolio.holdings.getOrDefault(symbol, 0);
        if (owned <= 0) {
            System.out.println("You do not own any shares of " + symbol);
            return;
        }
        System.out.println("You own " + owned + " shares.");
        System.out.print("Enter quantity to sell: ");
        int qty = readPositiveInt();
        if (qty <= 0) return;
        if (qty > owned) {
            System.out.println("You cannot sell more than you own.");
            return;
        }
        portfolio.sell(stock, qty);
        System.out.printf("Sold %d shares of %s for $%.2f%n", qty, symbol, stock.price * qty);
    }

    private static void viewPortfolio(Market market, Portfolio portfolio) {
        System.out.println("\n--------- PORTFOLIO SUMMARY ---------");
        System.out.printf("Cash balance   : $%.2f%n", portfolio.cashBalance);
        System.out.println("Holdings:");
        if (portfolio.holdings.isEmpty()) {
            System.out.println("  (none)");
        } else {
            System.out.printf("  %-6s %-8s %-10s %-10s%n", "Symbol", "Qty", "Price", "Value");
            for (Map.Entry<String, Integer> e : portfolio.holdings.entrySet()) {
                Stock s = market.getStock(e.getKey());
                double price = s != null ? s.price : 0;
                System.out.printf("  %-6s %-8d $%-9.2f $%-9.2f%n",
                        e.getKey(), e.getValue(), price, price * e.getValue());
            }
        }
        System.out.printf("Holdings value : $%.2f%n", portfolio.getHoldingsValue(market));
        System.out.printf("TOTAL VALUE    : $%.2f%n", portfolio.getTotalValue(market));
        System.out.println("--------------------------------------");
    }

    private static void viewHistory(Portfolio portfolio) {
        System.out.println("\n----- TRANSACTION HISTORY -----");
        if (portfolio.history.isEmpty()) {
            System.out.println("(no transactions yet)");
        } else {
            for (Transaction t : portfolio.history) {
                System.out.println(t);
            }
        }
        System.out.println("--------------------------------");
    }

    private static int readPositiveInt() {
        try {
            int val = Integer.parseInt(scanner.nextLine().trim());
            if (val <= 0) {
                System.out.println("Quantity must be positive.");
                return -1;
            }
            return val;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return -1;
        }
    }
}
