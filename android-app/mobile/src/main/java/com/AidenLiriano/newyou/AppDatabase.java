package com.AidenLiriano.newyou;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
                YogaData.class,
                CustomWorkout.class,
                CustomWorkoutData.class
        },
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AppDao appDao();

    private static volatile AppDatabase INSTANCE;

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS custom_workouts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "userId INTEGER NOT NULL, " +
                    "name TEXT, " +
                    "iconIndex INTEGER NOT NULL DEFAULT 0, " +
                    "trackHeartRate INTEGER NOT NULL DEFAULT 1, " +
                    "trackCalories INTEGER NOT NULL DEFAULT 1, " +
                    "trackDuration INTEGER NOT NULL DEFAULT 1, " +
                    "trackSteps INTEGER NOT NULL DEFAULT 0, " +
                    "trackDistance INTEGER NOT NULL DEFAULT 0, " +
                    "trackPace INTEGER NOT NULL DEFAULT 0, " +
                    "trackSpeed INTEGER NOT NULL DEFAULT 0, " +
                    "trackElevation INTEGER NOT NULL DEFAULT 0, " +
                    "trackLaps INTEGER NOT NULL DEFAULT 0, " +
                    "createdAt INTEGER NOT NULL DEFAULT 0)");

            db.execSQL("CREATE TABLE IF NOT EXISTS custom_workout_data (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "activityId INTEGER NOT NULL, " +
                    "customWorkoutId INTEGER NOT NULL DEFAULT 0, " +
                    "duration INTEGER NOT NULL DEFAULT 0, " +
                    "heartRate INTEGER NOT NULL DEFAULT 0, " +
                    "calories INTEGER NOT NULL DEFAULT 0, " +
                    "stepCount INTEGER NOT NULL DEFAULT 0, " +
                    "distance REAL NOT NULL DEFAULT 0, " +
                    "pace REAL NOT NULL DEFAULT 0, " +
                    "speed REAL NOT NULL DEFAULT 0, " +
                    "elevationGain REAL NOT NULL DEFAULT 0, " +
                    "elevationLoss REAL NOT NULL DEFAULT 0, " +
                    "laps INTEGER NOT NULL DEFAULT 0, " +
                    "FOREIGN KEY(activityId) REFERENCES activities(activity_id) " +
                    "ON DELETE CASCADE)");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "newyou_database")
                            .addMigrations(MIGRATION_3_4)
                            .setJournalMode(JournalMode.TRUNCATE)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}