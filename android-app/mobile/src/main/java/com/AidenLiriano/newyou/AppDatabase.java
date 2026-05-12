package com.AidenLiriano.newyou;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
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
        version = 3
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
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        // Default placeholder user — 5'9", 154 lbs (70kg)
                                        User defaultUser = new User(
                                                "Your Name", 25, 69, 154f, "Not set"
                                        );
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