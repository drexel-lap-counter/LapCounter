package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;

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

    /*** Units***/
    public List<Units> getAllUnits() throws InterruptedException, ExecutionException
    {
        return mRepository.getAllUnits();
    }

    public void insert(Units units)
    {
        mRepository.insert(units);
    }

    public void deleteWorkout(Units units)
    {
        mRepository.deleteUnits(units);
    }



    /*** Workout ***/
    public List<Workout> getAllWorkouts() {
        return mRepository.getAllWorkouts();
    }

    public List<Workout> getWorkoutsByDateRange(Date start_time, Date end_time)throws Exception
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
