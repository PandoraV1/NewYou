package com.AidenLiriano.newyou;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

@Entity(tableName = "user")
public class User {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "age")
    public int age;

    // Height stored in total inches internally
    @ColumnInfo(name = "height_inches")
    public int heightInches;

    // Weight stored in lbs internally
    @ColumnInfo(name = "weight_lbs")
    public float weightLbs;

    @ColumnInfo(name = "gender")
    public String gender;

    public User(String name, int age, int heightInches, float weightLbs, String gender) {
        this.name = name;
        this.age = age;
        this.heightInches = heightInches;
        this.weightLbs = weightLbs;
        this.gender = gender;
    }
}