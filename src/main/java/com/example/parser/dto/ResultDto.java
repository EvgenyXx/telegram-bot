package com.example.parser.dto;

public class ResultDto {

    private String player;
    private int place;
    private int bonus;
    private int total;
    private String date;

    public ResultDto(String player, int place, int bonus, int total,String date) {
        this.player = player;
        this.place = place;
        this.bonus = bonus;
        this.total = total;
        this.date = date;
    }

    public String getPlayer() {
        return player;
    }

    public int getPlace() {
        return place;
    }

    public int getBonus() {
        return bonus;
    }

    public int getTotal() {
        return total;
    }

    public String getDate() {
        return date;
    }
}