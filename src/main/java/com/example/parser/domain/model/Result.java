package com.example.parser.domain.model;

public class Result {

    private int place;
    private int bonus;
    private int total;

    public Result(int place, int bonus, int total) {
        this.place = place;
        this.bonus = bonus;
        this.total = total;
    }

    public int getPlace() { return place; }
    public int getBonus() { return bonus; }
    public int getTotal() { return total; }
}