package edu.drexel.lapcounter.lapcounter.backend.lapcounter;

import android.app.Service;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.Transition;
import edu.drexel.lapcounter.lapcounter.backend.Database.Transition.TransitionRepository;
import edu.drexel.lapcounter.lapcounter.backend.SimpleMessageReceiver;

import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_AFTER;
import static edu.drexel.lapcounter.lapcounter.backend.lapcounter.LocationStateMachine.EXTRA_STATE_BEFORE;

public class TransitionLog {
    public static final String ACTION_CLEAR_LOG =
            "edu.drexel.lapcounter.lapcounter.ACTION_CLEAR_LOG;";
    public static final String ACTION_FLUSH_LOG =
            "edu.drexel.lapcounter.lapcounter.ACTION_FLUSH_LOG;";
    public static final String EXTRA_WORKOUT_ID =
            "edu.drexel.lapcounter.lapcounter.EXTRA_WORKOUT_ID;";

    private List<Transition> mTransitions = new ArrayList<>();
    private Service mService;
    private LocalBroadcastManager mBroadcastManager;

    public TransitionLog(Service service) {
        mService = service;
        mBroadcastManager = LocalBroadcastManager.getInstance(service);
    }

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


    private SimpleMessageReceiver.MessageHandler clearLog = new SimpleMessageReceiver.MessageHandler() {
        @Override
        public void onMessage(Intent message) {
            mTransitions.clear();
        }
    };

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

    public void initCallbacks(SimpleMessageReceiver receiver) {
        receiver.registerHandler(LocationStateMachine.ACTION_STATE_TRANSITION, logTransition);
        receiver.registerHandler(ACTION_CLEAR_LOG, clearLog);
        receiver.registerHandler(ACTION_FLUSH_LOG, flushLog);
    }
}
