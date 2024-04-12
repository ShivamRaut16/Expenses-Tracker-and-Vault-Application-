import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.Console;

class ExpenseTracker {
    private static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the Expense Tracker!");

        try {
            User user = loginOrRegister(scanner);
            if (user == null) {
                System.out.println("Error occurred during login/registration.");
                return;
            }
            System.out.println("Hello, " + user.getName() + "!");

            while (true) {
                clearScreen();
                printMenu();
                int choice = getChoice(scanner);

                switch (choice) {
                    case 1:
                    case 2:
                    case 3:
                        userExpensesEntry(scanner, user, choice);
                        break;
                    case 4:
                        checkRemainingMoneyOrSavings(user);
                        break;
                    case 5:
                        seeExpenseDetails(user);
                        break;
                    case 6:
                        saveData(user);
                        System.out.println("Data saved successfully.");
                        break;
                    case 7:
                        addMoneyToSavingsVault(scanner, user);
                        break;
                    case 8:
                        withdrawMoneyFromSavingsVault(scanner, user);
                        break;
                    case 9:
                        System.out.println("Thank you for using Expense Tracker. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                System.out.println("Press Enter to continue...");
                scanner.nextLine(); // Wait for user to press Enter before clearing the screen
            }
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Error clearing screen: " + e.getMessage());
        }
    }

    private static User loginOrRegister(Scanner scanner) throws IOException {
        List<User> users = loadUsers();
        System.out.print("Do you have an account? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("yes")) {
            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Enter your password: ");
            String password = maskedPasswordInput(scanner);

            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    return user;
                }
            }
            throw new IllegalArgumentException("Invalid username or password. Please try again.");
        } else if (response.equals("no")) {
            System.out.print("Enter a username: ");
            String username = scanner.nextLine().trim();

            for (User user : users) {
                if (user.getUsername().equals(username)) {
                    throw new IllegalArgumentException("Username already exists. Please choose a different one.");
                }
            }

            System.out.print("Enter your name: ");
            String name = scanner.nextLine().trim();
            String password = null;
            while (password == null || password.isEmpty()) {
                System.out.print("Enter a password: ");
                password = maskedPasswordInput(scanner);
            }

            double initialWeeklyIncome = getValidAmount(scanner, "Enter your initial weekly income: $");

            double initialSavings = getValidAmount(scanner, "Enter initial amount for savings vault: $");

            User newUser = new User(name, username, password, initialWeeklyIncome);
            newUser.addMoneyToSavings(initialSavings);
            users.add(newUser);
            saveUsers(users);

            return newUser;
        } else {
            throw new IllegalArgumentException("Invalid response. Please enter 'yes' or 'no'.");
        }
    }

    private static List<User> loadUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);

        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(",");
                    users.add(new User(parts[0], parts[1], parts[2], Double.parseDouble(parts[3])));
                }
            }
        }

        return users;
    }

    private static void saveUsers(List<User> users) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            for (User user : users) {
                writer.println(user.getName() + "," + user.getUsername() + "," + user.getPassword() + "," + user.getTotalWeeklyIncome() + "," + user.getSavingsVault());
            }
        }
    }

    private static void printMenu() {
        System.out.println("+--------------------------------------------------------+");
        System.out.println("--------------Welcome to Your Expense Tracker!------------");
        System.out.println("+--------------------------------------------------------+");
        System.out.println("Here are the options to manage your expenses:");
        System.out.println("+--------------------------------------------------------+");
        System.out.println("1. Enter Personal Expenses");
        System.out.println("2. Enter Other Expenses");
        System.out.println("3. Enter Bill Payments");
        System.out.println("4. Check Remaining Money or Savings");
        System.out.println("5. See Details of My Expenses");
        System.out.println("6. Save Data");
        System.out.println("7. Add money to savings vault");
        System.out.println("8. Withdraw money from savings vault");
        System.out.println("9. Exit");
        System.out.println("+--------------------------------------------------------+");

        System.out.println("Please enter the number corresponding to your choice:");
    }

    private static int getChoice(Scanner scanner) {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }
    }

    private static void userExpensesEntry(Scanner scanner, User user, int choice) {
        String category;
        switch (choice) {
            case 1:
                category = "Personal";
                break;
            case 2:
                category = "Other";
                break;
            case 3:
                category = "Bills";
                break;
            default:
                System.out.println("Invalid choice for expenses entry.");
                return;
        }

        System.out.print("Enter title of expense: ");
        String title = scanner.nextLine().trim();

        double amount = getValidAmount(scanner, "Enter expense amount: $");
        LocalDateTime dateTime = LocalDateTime.now(); // Record current date and time
        user.addExpense(title, category, amount, dateTime);
        System.out.println("Expense added successfully.");
    }

    private static double getValidAmount(Scanner scanner, String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Amount cannot be empty.");
                } else {
                    double amount = Double.parseDouble(input);
                    if (amount < 0) {
                        System.out.println("Amount cannot be negative.");
                    } else {
                        return amount;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid amount.");
            }
        }
    }

    public static void checkRemainingMoneyOrSavings(User user) {
        double remainingMoney = user.getTotalWeeklyIncome() - user.getWeeklySavingsTarget() - user.getTotalExpenses();
        System.out.println("Remaining money after expenses: $" + remainingMoney);
        System.out.println("Total Savings: $" + user.getSavings());
        System.out.println("Savings Vault Balance: $" + user.getSavingsVault());
    }

    public static void seeExpenseDetails(User user) {
        System.out.println("\nExpense Details for " + user.getName() + ":");
        System.out.println("| Title              | Category           | Amount  | Date and Time           |");
        System.out.println("|--------------------|--------------------|---------|--------------------------|");
        for (Expense expense : user.getExpenses()) {
            System.out.printf("| %-18s | %-18s | $%-7.2f | %s |\n", expense.getTitle(), expense.getCategory(), 
            expense.getAmount(), expense.getFormattedDateTime());
        }
        System.out.println("Total Expenses: $" + user.getTotalExpenses());
        System.out.println("|_______|_______|___|__________|");
    }

    private static void saveData(User user) {
        String filename = user.getUsername() + "_data.txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Name: " + user.getName());
            writer.println("Username: " + user.getUsername());
            writer.println("Total Weekly Income: $" + user.getTotalWeeklyIncome());
            writer.println("Weekly Savings Target: $" + user.getWeeklySavingsTarget());
            writer.println("Savings Vault Balance: $" + user.getSavingsVault());

            writer.println("\nExpense Details:");
            writer.println("| Title              | Category           | Amount  | Date and Time           |");
            writer.println("|--------------------|--------------------|---------|--------------------------|");
            for (Expense expense : user.getExpenses()) {
                writer.printf("| %-18s | %-18s | $%-7.2f | %s |\n", expense.getTitle(), expense.getCategory(), 
                expense.getAmount(), expense.getFormattedDateTime());
            }
            writer.println("|_______|_______|___|__________|");

            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private static void addMoneyToSavingsVault(Scanner scanner, User user) {
        if (authenticateUser(scanner, user)) {
            double amount = getValidAmount(scanner, "Enter the amount to add to the savings vault: $");
            if (amount > 0) {
                user.addMoneyToSavings(amount);
                System.out.println("Amount added to savings vault successfully.");
            } else {
                System.out.println("Invalid amount. Please enter a positive number.");
            }
        } else {
            System.out.println("Authentication failed. Incorrect password.");
        }
    }

    private static void withdrawMoneyFromSavingsVault(Scanner scanner, User user) {
        if (authenticateUser(scanner, user)) {
            double amount = getValidAmount(scanner, "Enter the amount to withdraw from the savings vault: $");
            if (amount > 0 && amount <= user.getSavingsVault()) {
                user.withdrawMoneyFromSavings(amount);
                System.out.println("Amount withdrawn from savings vault successfully.");
            } else {
                System.out.println("Invalid amount or insufficient funds.");
            }
        } else {
            System.out.println("Authentication failed. Incorrect password.");
        }
    }

    private static String maskedPasswordInput(Scanner scanner) {
        Console console = System.console();
        if (console == null) {
            System.out.print("Enter your password: ");
            return scanner.nextLine().trim();
        } else {
            char[] passwordChars = console.readPassword("Enter your password: ");
            return new String(passwordChars);
        }
    }

    private static boolean authenticateUser(Scanner scanner, User user) {
        System.out.print("Enter your password: ");
        String password = maskedPasswordInput(scanner);
        return password.equals(user.getPassword());
    }
}

class User {
    private String name;
    private String username;
    private String password;
    private double totalWeeklyIncome;
    private double weeklySavingsTarget;
    private double savingsVault;
    private List<Expense> expenses;

    public User(String name, String username, String password, double initialWeeklyIncome) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.totalWeeklyIncome = initialWeeklyIncome;
        this.weeklySavingsTarget = 0.0;
        this.savingsVault = 0.0;
        this.expenses = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public double getTotalWeeklyIncome() {
        return totalWeeklyIncome;
    }

    public double getWeeklySavingsTarget() {
        return weeklySavingsTarget;
    }

    public void setWeeklySavingsTarget(double weeklySavingsTarget) {
        this.weeklySavingsTarget = weeklySavingsTarget;
    }

    public double getSavings() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public double getSavingsVault() {
        return savingsVault;
    }

    public void addMoneyToSavings(double amount) {
        savingsVault += amount;
    }

    public void withdrawMoneyFromSavings(double amount) {
        if (amount <= savingsVault) {
            savingsVault -= amount;
        } else {
            System.out.println("Insufficient funds in savings vault.");
        }
    }

    public void addExpense(String title, String category, double amount, LocalDateTime dateTime) {
        expenses.add(new Expense(title, category, amount, dateTime));
    }

    public double getTotalExpenses() {
        return expenses.stream().mapToDouble(Expense::getAmount).sum();
    }

    public List<Expense> getExpenses() {
        return expenses;
    }
}

class Expense {
    private String title;
    private String category;
    private double amount;
    private LocalDateTime dateTime;

    public Expense(String title, String category, double amount, LocalDateTime dateTime) {
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.dateTime = dateTime;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getFormattedDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
