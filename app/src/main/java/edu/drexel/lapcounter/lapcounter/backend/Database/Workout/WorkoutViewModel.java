package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import java.util.List;

public class WorkoutViewModel extends AndroidViewModel {

    private WorkoutRepository mRepository;
    // Using LiveData and caching what getAlphabetizedWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    private LiveData<List<Workouts>> mAllWorkouts;

    public WorkoutViewModel(Application application) {
        super(application);
        mRepository = new WorkoutRepository(application);
        mAllWorkouts = mRepository.getAllWorkouts();
    }

    public LiveData<List<Workouts>> getAllWorkouts() {
        return mAllWorkouts;
    }

    public void insert(Workouts workout)
    {
        mRepository.insert(workout);
    }
    public Workouts getWorkoutByID(int id)
    {
        return mRepository.getWorkoutByID(id);
    }


}
