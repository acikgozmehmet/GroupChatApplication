package client;

public class Test {

    public static void main(String[] args) {
        String text = "WEATHER Kansas City";
        String weather = text.trim().substring("WEATHER".length());
        System.out.println("weather = " + weather);

    }
}
