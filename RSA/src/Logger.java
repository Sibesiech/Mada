public class Logger {

    boolean writeOut = false;

    public Logger(boolean writeOut) {
        this.writeOut = writeOut;
    }

    public void log(String message){
        if (writeOut){
            System.out.println(message);
        }
    }
}
