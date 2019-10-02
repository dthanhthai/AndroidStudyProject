package com.thaidt.demologinmvvm.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Insert
    Long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM user_table")
    void deleteAllUser();

    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password ")
    User login(String username, String password);
}
