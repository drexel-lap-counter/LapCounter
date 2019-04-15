package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {

    private WorkoutRepository mRepository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.

    public WorkoutViewModel(Application application) {
        super(application);
        mRepository = new WorkoutRepository(application);
    }

    public List<Workout> getAllWorkouts() {
        return mRepository.getAllWorkouts();
    }
    public List<Workout> getWorkoutsByDateRange(long start_time, long end_time)throws Exception
    {
        return mRepository.getWorkoutsByDateRange(start_time,end_time);
    }
    public void insert(Workout workout)
    {
        mRepository.insert(workout);
    }
    public Workout getWorkoutByID(int id)
    {
        return mRepository.getWorkoutByID(id);
    }
    public void deleteAllWorkouts(){mRepository.deleteAll();}
    public void deleteWorkout(int id){mRepository.deleteWorkoutByID(id);}


    public List<Workout> getAllWorkoutsDecending() {
        return mRepository.getAllWorkoutsDecending();
    }
}
