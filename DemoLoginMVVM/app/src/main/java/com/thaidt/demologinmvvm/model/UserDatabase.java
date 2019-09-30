package com.thaidt.demologinmvvm.model;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class}, version = 1)
public abstract class UserDatabase extends RoomDatabase {

    private static UserDatabase instance;

    public abstract UserDao userDao();

    public static  synchronized UserDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    UserDatabase.class, "user_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new InitMockDataAsyncTask(instance).execute();
        }
    };

    private static class InitMockDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private UserDao userDao;

        private InitMockDataAsyncTask(UserDatabase db) {
            this.userDao = db.userDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            userDao.insert(new User("abc","111"));
            userDao.insert(new User("xyz","111"));
            userDao.insert(new User("aaa","111"));
            return null;
        }
    }
}
