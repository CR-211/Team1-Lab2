import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Store {
    private List<Integer> stockList = new ArrayList<>();
    private final int capacity = 8;
    private boolean producersFinished = false;
    private boolean consumersFinished = false;
    private int producedCount = 0;
    private JTextArea textArea;

    public Store(JTextArea textArea) {
        this.textArea = textArea;
    }

    public synchronized void produce(int producerId) {
        Random random = new Random();
        int randomNumber1, randomNumber2;

        while (producedCount < 48) {
            while (stockList.size() + 2 <= capacity && producedCount < 48) {
                randomNumber1 = random.nextInt(100) * 2;
                randomNumber2 = random.nextInt(100) * 2;

                stockList.add(randomNumber1);
                stockList.add(randomNumber2);

                producedCount += 2;
                textArea.append("Producatorul " + producerId + " a generat: " + randomNumber1 + ", " + randomNumber2 + ". Total generat: " + producedCount + "\n");
                displayStock();
                try {
                    Thread.sleep(100); // Mic wait de 100 de milisecunde
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            if (producedCount == 48) {
                randomNumber1 = random.nextInt(100) * 2;
                stockList.add(randomNumber1);
                producedCount++;
                textArea.append("Producatorul " + producerId + " a generat: " + randomNumber1 + ". Total generat: " + producedCount + "\n");
                displayStock();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            notifyAll();

            if (producedCount == 49) {
                break;
            }

            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        producersFinished = true;
        notifyAll();
    }

    public synchronized void consume(int consumerId) {
        while (!producersFinished || stockList.size() > 0) {
            while (stockList.size() >= 2 || (producedCount == 49 && stockList.size() > 0)) {
                int consumedNumber1 = stockList.remove(0);
                int consumedNumber2 = stockList.size() > 0 ? stockList.remove(0) : -1;

                textArea.append("Consumatorul " + consumerId + " a consumat: " + consumedNumber1 + (consumedNumber2 != -1 ? ", " + consumedNumber2 : "") + "\n");
                displayStock();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            notifyAll();

            if (!producersFinished && stockList.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        consumersFinished = true;
        notifyAll();
    }

    public synchronized List<Integer> getStockList() {
        return new ArrayList<>(stockList);
    }

    public synchronized boolean areProducersFinished() {
        return producersFinished;
    }

    public synchronized boolean areConsumersFinished() {
        return consumersFinished;
    }

    public synchronized int getProducedCount() {
        return producedCount;
    }

    private void displayStock() {
        int currentSize = stockList.size();
        textArea.append("Stocul: " + currentSize + " din " + capacity + "\n");
    }

    public synchronized void setProducersFinished() {
        producersFinished = true;
        notifyAll();
    }

    public synchronized void setConsumersFinished() {
        consumersFinished = true;
        notifyAll();
    }
}

class Producer extends Thread {
    private final Store store;
    private final int producerId;

    public Producer(Store store, int producerId) {
        this.store = store;
        this.producerId = producerId;
    }

    @Override
    public void run() {
        store.produce(producerId);
    }
}

class Consumer extends Thread {
    private final Store store;
    private final int consumerId;

    public Consumer(Store store, int consumerId) {
        this.store = store;
        this.consumerId = consumerId;
    }

    @Override
    public void run() {
        store.consume(consumerId);
    }
}

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Producer-Consumer Problem");
        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        Store store = new Store(textArea);

        Producer producer1 = new Producer(store, 1);
        Producer producer2 = new Producer(store, 2);

        Consumer consumer1 = new Consumer(store, 1);
        Consumer consumer2 = new Consumer(store, 2);
        Consumer consumer3 = new Consumer(store, 3);

        producer1.start();
        producer2.start();
        consumer1.start();
        consumer2.start();
        consumer3.start();

        try {
            producer1.join();
            producer2.join();
            consumer1.join();
            consumer2.join();
            consumer3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (store.getProducedCount() == 48) {
            textArea.append("Producatorul special a generat: " + (new Random().nextInt(100) * 2) + ". Total generat: " + (store.getProducedCount() + 1) + "\n");
            textArea.append("Stocul final:\n");
            textArea.append(store.getStockList().toString() + "\n");
        }

        textArea.append("Toți producătorii și consumatorii au terminat. Programul se încheie.\n");
    }
}
