package edu.drexel.lapcounter.lapcounter.backend.Database.Workout;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.drexel.lapcounter.lapcounter.backend.Database.Units.Units;

/**
 * ViewModel for Workouts that are used by UI Activities to interact with the DB.
 * Has Repo object within it that is used to interact with DAO that interacts with DB.
 * @see WorkoutRepository
 * @see WorkoutDao
 * @see Workout
 * @see edu.drexel.lapcounter.lapcounter.backend.Database.LapCounterDatabase
 */
public class WorkoutViewModel extends AndroidViewModel {

    /**
     * WorkoutRepository used for interacting with DB's WorkoutDao.
     */
    private WorkoutRepository mRepository;

    /**
     * Constructor for setting up a WorkoutViewModel
     * @param application Application to set up ViewModel for.
     */
    public WorkoutViewModel(Application application) {
        super(application);
        mRepository = new WorkoutRepository(application);
    }

    /*** Units***/
    /**
     * Gets all Units in Units table.
     * @return List of a Units objects from DB table, Empty list if none in DB.
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public List<Units> getAllUnits() throws InterruptedException, ExecutionException
    {
        return mRepository.getAllUnits();
    }

    /**
     * Insert Units into Units table.
     * @param units Units object to insert.
     */
    public void insert(Units units)
    {
        mRepository.insert(units);
    }

    /**
     * Delete a Units object from the database.
     * @param units Units object to delete from database.
     */
    public void deleteUnits(Units units)
    {
        mRepository.deleteUnits(units);
    }



    /*** Workout ***/
    /**
     * Returns list of all workouts that exist within the DB.
     * @return List of Workout object for all workouts in DB, Empty List if none in DB.
     */
    public List<Workout> getAllWorkouts() {
        return mRepository.getAllWorkouts();
    }

    /**
     * Returns list of all workouts that exist in DB within date range.
     * @param start_time Date object that is start time of the range.
     * @param end_time   Date object that is end time of the range.
     * @return List of Workouts that exist within range, Empty List if there is none.
     * @throws Exception
     */
    public List<Workout> getWorkoutsByDateRange(Date start_time, Date end_time)throws Exception
    {
        return mRepository.getWorkoutsByDateRange(start_time,end_time);
    }

    /**
     * Insert a workout into DB.
     * Will update if workout already exists within DB
     * @param workout Workout to insert.
     */
    public void insert(Workout workout)
    {
        mRepository.insert(workout);
    }

    /**
     * Returns workout with entered id.
     * @param id int id of workout to look for.
     * @return Workout object of workout with id if it exists, else null.
     */
    public Workout getWorkoutByID(int id)
    {
        return mRepository.getWorkoutByID(id);
    }

    /**
     * Delete all workouts from the database.
     */
    public void deleteAllWorkouts(){mRepository.deleteAllWorkouts();}

    /**
     * Delete workout with given id from database.
     * @param id int id of workout to delete.
     */
    public void deleteWorkout(int id){mRepository.deleteWorkoutByID(id);}

    /**
     * Gets all workouts and returns them in descending order by start date.
     * @return List of Workouts for all in db order descending by start date, Empty list if none.
     */
    public List<Workout> getAllWorkoutsDecending() {
        return mRepository.getAllWorkoutsDecending();
    }

}
