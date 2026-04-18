package com.AidenLiriano.newyou;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabase.Callback;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.Executors;

@Database(
        entities = {
                User.class,
                Activity.class,
                RunningData.class,
                SwimmingData.class,
                BikingData.class,
                WalkingData.class,
                HikingData.class,
                MeditationData.class,
                StrengthTrainingData.class,
                YogaData.class
        },
        version = 2
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AppDao appDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "newyou_database")
                            .fallbackToDestructiveMigration()
                            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Insert a default placeholder user when DB is first created
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        User defaultUser = new User("Default User", 0, 0, 0);
                                        INSTANCE.appDao().insertUser(defaultUser);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}