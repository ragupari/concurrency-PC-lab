import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

class Computer extends Thread{
    ReentrantLock lock;
    int computerID;
    ArrayList<ArrayList<String>> printTasks;
    SharedQueue queue;
    public Computer(int computerID, SharedQueue queue, ArrayList<ArrayList<String>> printTasks){
        this.computerID = computerID;
        this.lock = new ReentrantLock();
        this.queue = queue;
        this.printTasks = printTasks;
    }
    public synchronized ArrayList<String> getPrintTask() throws InterruptedException {
        while (this.printTasks.isEmpty()) {
            wait();
        }
        ArrayList<String> printTask = printTasks.remove(0);
        notifyAll(); 
        return printTask;
    }
    public void run() {
     
        while (true){
            this.lock.lock();
            try {
                    ArrayList<String> printTask = getPrintTask();
                    addPrintJob(printTask.get(0), printTask.get(1));
                    //Thread.sleep(1000);
                }
             catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public void addPrintJob(String fileName, String fileType){
        if (fileType.equals("pdf")){
            PrintJob printJob = new PrintJob(fileName, fileType);
            if (!queue.isFull()){
                queue.addPrintJob(printJob);
                System.out.println("Computer "+this.computerID + " added "+ fileName +" to the shared queue.");
            }else{
                System.out.println("Cannot any more print jobs.");
            }
        }else{
            try{
                throw new TypeNotSupportedException("This file type is not supported.");
            }catch(TypeNotSupportedException e){
                System.out.println(e.getMessage());
            }
        }
    
    }
}

class Printer extends Thread{
    ReentrantLock lock;
    int printerID;
    SharedQueue queue;
    public Printer(int printerID,SharedQueue queue){
        this.printerID = printerID;
        this.queue = queue;
        this.lock = new ReentrantLock();
    }
    public synchronized PrintJob getPrintDetails() throws InterruptedException {
        while (this.queue.getQueue().isEmpty()) {
            wait(); 
        }
        PrintJob printDetails = queue.getQueue().remove(0);
        notifyAll(); 
        return printDetails;
    }
    public void run() {
     
        while (true){
            this.lock.lock();
            try {
                    PrintJob printTask = getPrintDetails();
                    print(printTask);
                    Thread.sleep(10000);
                }
             catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
    public void print(PrintJob printJob){
        this.lock.lock();
        try{
        if (printJob.getFileType().equals("pdf")){
            System.out.println("Printer "+this.printerID+ " is printing "+printJob.getFileName());
        }else{
            try{
                throw new TypeNotSupportedException("This file type is not supported.");
            }catch(TypeNotSupportedException e){
                System.out.println(e.getMessage());
            }
        }
        }finally{
            this.lock.unlock();
        }
    }
}

class PrintJob {
    private String fileName;
    private String fileType;
    public PrintJob(String fileName, String fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
    }
    public String getFileName() {
        return fileName;
    }
    public String getFileType() {
        return fileType;
    }
}

class SharedQueue {
    ArrayList<PrintJob> queue;
    int capacity;

    public SharedQueue(int capacity) {
        this.queue = new ArrayList<>();
        this.capacity = capacity;
    }

    public ArrayList<PrintJob> getQueue() {
        return queue;
    }
    public void addPrintJob(PrintJob printJob) {
        queue.add(printJob);
    }
    public boolean isFull(){
        if (queue.size()<capacity){
            return false;
        }
        return true;
    }
}

class  TypeNotSupportedException extends Exception{
    public TypeNotSupportedException(String message){
        super(message);
    }
}

public class Main {

    public static void main(String[] args) {

        SharedQueue sharedQueue = new SharedQueue(5);

        ArrayList<ArrayList<String>> printTasks = new ArrayList<>();

        ArrayList<String>task1 = new ArrayList<>();
        task1.add("test.pdf");
        task1.add("pdf");
        printTasks.add(task1);

        ArrayList<String>task2 = new ArrayList<>();
        task2.add("test2.png");
        task2.add("png");
        printTasks.add(task2);

        ArrayList<String>task3 = new ArrayList<>();
        task3.add("test3.pdf");
        task3.add("pdf");
        printTasks.add(task3);

        ArrayList<String>task4 = new ArrayList<>();
        task4.add("test4.pdf");
        task4.add("pdf");
        printTasks.add(task4);

        for (int i = 1; i <= 3; i++) {
            Computer computer = new Computer(i, sharedQueue, printTasks);
            computer.start();
        }

        for (int i = 1; i <= 2; i++) {
            Printer printer = new Printer(i, sharedQueue);
            printer.start();
        }
    }
}