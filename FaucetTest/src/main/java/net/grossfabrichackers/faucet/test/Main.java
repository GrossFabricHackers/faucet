package net.grossfabrichackers.faucet.test;

public class Main {
    public static void main(String... args) {
        String s = new String("faucet go brr");
        System.out.println(s);
        System.out.println(new Tater() + new Tater());
    }
}

class Tater {}
class PluralTater {}
