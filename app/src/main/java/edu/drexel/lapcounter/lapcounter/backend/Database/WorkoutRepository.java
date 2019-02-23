package edu.drexel.lapcounter.lapcounter.backend.Database;

import android.app.Application;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class WorkoutRepository {

    private WorkoutDao mWorkoutDao;
    private List<Workouts> mAllWorkouts;


    WorkoutRepository(Application application) {
        WorkoutDatabase db = WorkoutDatabase.getDatabase(application);
        mWorkoutDao = db.workoutDao();
        mAllWorkouts = mWorkoutDao.getAllWorkouts();
    }List<Workouts> getAllWorkouts() {
        return mAllWorkouts;
    }


}
