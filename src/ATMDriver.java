import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class Card {
    private String cardNumber;
    private String pin;

    Card(String cardNumber, String pin) {
        this.cardNumber = cardNumber;
        this.pin = pin;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getPin() {
        return pin;
    }

}

class Account {
    private final String accNumber;
    private double balance;

    Account(String accNumber, double balance) {
        this.accNumber = accNumber;
        this.balance = balance;
    }

    public synchronized boolean debit(double amount) {
        if (amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public synchronized boolean credit(double amount) {
        if (amount > 0) {
            balance += amount;
            return true;
        }
        return false;
    }

    public String getAccNumber() {
        return accNumber;
    }
    public synchronized double getBalance() {
        return balance;
    }

}

abstract class Transaction {
    protected final Account account;
    protected final double amount;

    Transaction(Account account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    public abstract void execute();
}

class WithdrawalTransaction extends Transaction {
    WithdrawalTransaction(Account account, double amount) {
        super(account,amount);
    }

    @Override
    public void execute() {
        if(account.debit(amount)){
            System.out.println("Withdrawal of " + amount + " successful. New balance: " + account.getBalance());
        } else {
            System.out.println("Withdrawal of " + amount + " failed. Insufficient funds.");
        }
    }

}

class DepositTransaction extends Transaction {
    DepositTransaction(Account account, double amount) {
        super(account,amount);
    }

    @Override
    public void execute() {
        if(account.credit(amount)){
            System.out.println("Deposit of " + amount + " successful. New balance: " + account.getBalance());
        } else {
            System.out.println("Deposit of " + amount + " failed.");
        }
    }
}

class BankingService {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public void addAccount(Account account) {
        accounts.put(account.getAccNumber(), account);
    }

    public Account getAccount(String accNumber) {
        return accounts.get(accNumber);
    }

    public void processTransaction(Transaction transaction) {
        lock.lock();
        try {
            transaction.execute();
        } finally {
            lock.unlock();
        }
    }

    public double getBalance(String accNumber) {
        Account account = accounts.get(accNumber);
        if (account != null) {
            return account.getBalance();
        }
        return -1; // or throw an exception
    }

    public boolean authenticate(Card card) {
        return accounts.containsKey(card.getCardNumber());
    }
}

class CashDispenser {
    private double cashAvailable;
    private final ReentrantLock lock = new ReentrantLock();

    public CashDispenser(double initialCash) {
        this.cashAvailable = initialCash;
    }
    public boolean dispenseCash(double amount) {
        lock.lock();
        try {
            if (amount <= cashAvailable) {
                cashAvailable -= amount;
                System.out.println("Dispensed Cash: " + amount);
                return true;
            } else {
                System.out.println("ATM does not have enough cash to dispense ");
                return false;
            }
        } finally {
            lock.unlock();
        }
    }
}

class ATM {
    private final BankingService bankingService;
    private final CashDispenser cashDispenser;

    public ATM(BankingService bankingService, CashDispenser cashDispenser) {
        this.bankingService = bankingService;
        this.cashDispenser = cashDispenser;
    }

    public boolean authenticate(Card card, String eneteredPin) {
        if(!bankingService.authenticate(card)){
            System.out.println("Authentication failed for card: " + card.getCardNumber());
            return false;
        }

        if (!card.getPin().equals(eneteredPin)) {
            System.out.println("Incorrect PIN for card: " + card.getCardNumber());
            return false;
        }
        System.out.println("Authentication successful for card: " + card.getCardNumber());
        return true;
    }

    public double checkBalance(Card card) {
        Account account = bankingService.getAccount(card.getCardNumber());
        if (account != null) {
            return account.getBalance();
        }
        System.out.println("Account not found for card: " + card.getCardNumber());
        return -1; // or throw an exception
    }

    public void withdrawCash(Card card, double amount) {
        Account acc = bankingService.getAccount(card.getCardNumber());
        if(acc != null && acc.getBalance() >= amount){
            if(cashDispenser.dispenseCash(amount)){
                Transaction t = new WithdrawalTransaction(acc, amount);
                bankingService.processTransaction(t);
            }
        }
        else{
            System.out.println("Insufficient balance for withdrawal or account not found for card: " + card.getCardNumber());
        }
    }

    public void depositCash(Card card, double amount) {
        Account acc = bankingService.getAccount(card.getCardNumber());
        if (acc != null) {
            Transaction t = new DepositTransaction(acc, amount);
            bankingService.processTransaction(t);
        }
    }

}




public class ATMDriver {

    public static void main(String[] args) {
        BankingService bankingService = new BankingService();
        CashDispenser cashDispenser = new CashDispenser(10000.0);

        Account acc1 = new Account("123456", 5000);
        bankingService.addAccount(acc1);

        Card card1 = new Card("123456", "1234");
        ATM atm = new ATM(bankingService, cashDispenser);

        if(atm.authenticate(card1, "1234")) {
            System.out.println("Balance: " + atm.checkBalance(card1));
            atm.withdrawCash(card1, 1000);
            System.out.println("Balance after withdrawal: " + atm.checkBalance(card1));
            atm.depositCash(card1, 500);
            System.out.println("Balance after deposit: " + atm.checkBalance(card1));
        } else {
            System.out.println("Authentication failed.");
        }
    }

}
