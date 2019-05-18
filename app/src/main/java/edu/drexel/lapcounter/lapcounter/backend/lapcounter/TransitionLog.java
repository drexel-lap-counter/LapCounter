package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.app.Service;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.Transition;
import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionRepository;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_AFTER;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_BEFORE;

/**
 * This component of LapCounterService listens for state transitions from the LocationStateMachine
 * and records them to the DB if the workout is saved.
 */
public class TransitionLog {
    /**
     * The CurrentWorkoutActivity can send this message to force the transition log
     * to clear the pending state transitions because the workout was canceled.
     */
    public static final String ACTION_CLEAR_LOG =
            "edu.drexel.lapcounter.lapcounter.ACTION_CLEAR_LOG;";
    /**
     * On completion of a workout, the CurrentWorkoutActivity sends this message to
     * write everything in the transition buffer to the database
     */
    public static final String ACTION_FLUSH_LOG =
            "edu.drexel.lapcounter.lapcounter.ACTION_FLUSH_LOG;";
    /**
     * When flushing the log, the sender must specify which workout to associate these
     * transitions with
     */
    public static final String EXTRA_WORKOUT_ID =
            "edu.drexel.lapcounter.lapcounter.EXTRA_WORKOUT_ID;";

    /**
     * A buffer of transitions for the current workout
     */
    private List<Transition> mTransitions = new ArrayList<>();
    /**
     * The parent service. This is needed to get a reference to the Application.
     */
    private Service mService;

    /**
     * Constructor
     * @param service the parent service so this class can get a reference to the Application.
     */
    public TransitionLog(Service service) {
        mService = service;
    }

    /**
     * When a transition event arrives, add it to the buffer.
     */
    private SimpleMessageReceiver.MessageHandler logTransition = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            LocationStateMachine.State before = (LocationStateMachine.State) message.getSerializableExtra(EXTRA_STATE_BEFORE);
            LocationStateMachine.State after = (LocationStateMachine.State) message.getSerializableExtra(EXTRA_STATE_AFTER);
            Date transitionTime = new Date();

            Transition transition = new Transition(
                    transitionTime, before.toString(), after.toString());

            mTransitions.add(transition);
        }
    };


    /**
     * If the TransitionLog gets a clear log event, discard everything in the buffer.
     */
    private SimpleMessageReceiver.MessageHandler clearLog = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mTransitions.clear();
        }
    };

    /**
     * When the TransitionLog gets a flush event, ins
     */
    private SimpleMessageReceiver.MessageHandler flushLog = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            int workoutID = message.getIntExtra(EXTRA_WORKOUT_ID, -1);
            TransitionRepository repo = new TransitionRepository(mService.getApplication());

            for (Transition transition : mTransitions) {
                transition.setWorkoutID(workoutID);
                repo.insert(transition);
            }

            mTransitions.clear();
        }
    };

    /**
     * Set up listeners for the different evennts
     * @param receiver the receiver for listening to events.
     */
    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, logTransition);
        receiver.registerHandler(ACTION_CLEAR_LOG, clearLog);
        receiver.registerHandler(ACTION_FLUSH_LOG, flushLog);
    }
}
