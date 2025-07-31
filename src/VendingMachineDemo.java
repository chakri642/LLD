import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

enum Coin{
    ONE(1), TWO(2), FIVE(5), TEN(10);
    private int value;
    Coin(int value){
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}

enum Note{
    TEN(10), TWENTY(20), FIFTY(50), HUNDRED(100);
    private int value;
    Note(int value){
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}

class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}

class Inventory {
    private ConcurrentHashMap<Product, Integer> products = new ConcurrentHashMap<>();
//    private ReentrantLock lock = new ReentrantLock();
    void addProduct(Product product, int quantity) {
        products.put(product,quantity);
    }
    public boolean hasProduct(Product product) {
        return products.getOrDefault(product,0) > 0;
    }

    public void updateQuantity(Product product, int quantity) {
        if(products.containsKey(product)) {
            products.put(product, products.get(product) + quantity);
        }
//        products.computeIfPresent(product, (key, value) -> value + quantity);
    }

    public int getQuantity(Product product) {
        return products.getOrDefault(product, 0);
    }

    public Set<Product> getAllProducts() {
        return products.keySet();
    }

}

interface VendingMachineState {
    void selectProduct(Product product);
    void insertCoin(Coin coin);
    void insertNote(Note note);
    void dispenseProduct();
    void cancelTransaction();
}

class IdleState implements VendingMachineState {

    private final VendingMachine vm;
    public IdleState(VendingMachine vm) {
        this.vm = vm;
    }

    @Override
    public void selectProduct(Product product) {
        if(vm.getInventory().hasProduct(product)){
            vm.setSelectedProduct(product);
            vm.setState(vm.getReadyState());
            System.out.println("Product selected: " + product.getName() + " Price: " + product.getPrice());
        } else {
            System.out.println("Product not available.");
        }
    }

    @Override
    public void insertCoin(Coin coin) {
        System.out.println("Please select a product first.");
    }

    @Override
    public void insertNote(Note note) {
        System.out.println("Please select a product first.");
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Please select a product first.");
    }

    @Override
    public void cancelTransaction() {
        System.out.println("No transaction to cancel.");
    }
}

class ReadyState implements VendingMachineState {

    private final VendingMachine vm;

    public ReadyState(VendingMachine vm) {
        this.vm = vm;
    }

    @Override
    public void selectProduct(Product product) {
        System.out.println("Product already selected: " + product.getName());
    }

    @Override
    public void insertCoin(Coin coin) {
        vm.addPayment(coin.getValue());
        System.out.println("Inserted coin: " + coin.getValue() + ". Total payment: " + vm.getPayment());
        checkPayment();
    }
//
    @Override
    public void insertNote(Note note) {
        vm.addPayment(note.getValue());
        System.out.println("Inserted note: " + note.getValue() + ". Total payment: " + vm.getPayment());
        checkPayment();
    }

    private void checkPayment() {
        if (vm.getPayment() >= vm.getSelectedProduct().getPrice()) {
            vm.setState(vm.getDispenseState());
            System.out.println("Ready to dispense product.");
        } else {
            System.out.println("Insufficient payment. Please add more coins or notes.");
        }
    }

    @Override
    public void dispenseProduct() {
        System.out.println("Please complete the payment first.");
    }

    @Override
    public void cancelTransaction() {
        System.out.println("Transaction cancelled. Returning money: " + vm.getPayment());
        vm.reset();
    }
}

class DispenseState implements VendingMachineState {

    private final VendingMachine vm;

    public DispenseState(VendingMachine vm) {
        this.vm = vm;
    }

    @Override
    public void selectProduct(Product product) {
        System.out.println("Product already selected: " + product.getName());
    }

    @Override
    public void insertCoin(Coin coin) {
        System.out.println("Cannot insert coins while dispensing product.");
    }

    @Override
    public void insertNote(Note note) {
        System.out.println("Cannot insert notes while dispensing product.");
    }

    @Override
    public void dispenseProduct() {
        Product product = vm.getSelectedProduct();
        if (product != null && vm.getInventory().hasProduct(product)) {
            vm.getInventory().updateQuantity(product, -1);
            double change = vm.getPayment() - product.getPrice();
            System.out.println("Dispensed: " + product.getName());
            if (change > 0) {
                System.out.println("Returning change: " + change);
            }
            vm.addToMoneyCollected(product.getPrice());
            System.out.println("Dispensing product: " + product.getName());
            vm.reset();
        } else {
            System.out.println("Product not available for dispensing.");
        }
    }

    @Override
    public void cancelTransaction() {
        System.out.println("Cannot cancel transaction while dispensing product.");
    }
}


class VendingMachine {
    private static VendingMachine instance;
    private static final Object lock = new Object();

    private VendingMachineState state;

    private VendingMachineState idleState;
    private VendingMachineState readyState;
    private VendingMachineState dispenseState;

    private final Inventory inventory = new Inventory();
    private Product selectedProduct;
    private double payment;
    private double moneyCollected;

    private VendingMachine(){
        idleState = new IdleState(this);
        readyState = new ReadyState(this);
        dispenseState = new DispenseState(this);
        state = idleState;
    }

    public static VendingMachine getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new VendingMachine();
            }
        }
        return instance;
    }

    void selectProduct(Product product){ state.selectProduct(product); }
    void insertCoin(Coin coin){ state.insertCoin(coin); }
    void insertNote(Note note){ state.insertNote(note); }
    void dispenseProduct(){ state.dispenseProduct(); }
    void cancelTransaction(){ state.cancelTransaction(); }

    public void restock(Product product, int quantity) {
        inventory.addProduct(product, quantity);
        System.out.println("Restocked " + quantity + " of " + product.getName());
    }

    public void collectMoney(){
        System.out.println("Total money collected: " + moneyCollected);
        moneyCollected = 0;
    }

    public void addPayment(double amount) { payment += amount; }
    public void addToMoneyCollected(double amt) { moneyCollected += amt; }
    public void setSelectedProduct(Product p) { selectedProduct = p; }
    public void setState(VendingMachineState s) { state = s; }
    public void reset() {
        payment = 0;
        selectedProduct = null;
        state = idleState;
    }

    public double getPayment() { return payment; }
    public Product getSelectedProduct() { return selectedProduct; }
    public Inventory getInventory() { return inventory; }
    public VendingMachineState getReadyState() { return readyState; }
    public VendingMachineState getDispenseState() { return dispenseState; }

}

public class VendingMachineDemo {
    public static void main(String[] args) {
        VendingMachine vm = VendingMachine.getInstance();

        Product cola = new Product("Cola", 1.50);
        Product chips = new Product("Chips", 1.00);
        Product candy = new Product("Candy", 0.75);

        vm.restock(cola, 10);
        vm.restock(chips, 5);
        vm.restock(candy, 20);

        vm.selectProduct(cola);
        vm.insertCoin(Coin.ONE);
        vm.insertCoin(Coin.TWO);
        vm.insertNote(Note.TEN);
        vm.dispenseProduct();

        vm.collectMoney();
    }
}