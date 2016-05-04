/*
 * Copyright (C) 2014 Google Inc. All Rights Reserved. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

//Some of this code was borrowed from an example file on GitHub called
//CastHelloText-Android, it was a simple application that had the Google
//Chromecast protocols almost in place for what we needed them for. So,
//the file was edited to suit our needs and our buttons / functions were
//added.

package com.senior.chromecastcheckers;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.Cast.ApplicationConnectionResult;
import com.google.android.gms.cast.Cast.MessageReceivedCallback;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;


//Main class that handles the user input AKA pressing the buttons
//and then constructing the JSON messages and handing them down to
//the functions that actually transmit the message to the Chromecast.
public class MainActivity extends ActionBarActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastListener;
    private ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private HelloWorldChannel mHelloWorldChannel;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;
    //The variables above were declared by the open source code / Google Code.


    //These were the variables we declared/used the most.
    private int UserID;
    //Gives the player a unique UserID, used so that not more than 2 players can join the game
    //and the receiver can tell which player is which so it knows who to block out and who to let
    //move.

    private int PlayerNum;
    //Tells the Android app which player it is based on what the receiver sends back in the JSON
    //message (1 or 2).

    private boolean PlayerTurn;
    //Tells the Android app which player's turn it is based on what the receiver sends back in the
    //JSON message (True/False).

    private String GameStatus;
    //Tells the Android app what the game status is based on what the receiver sends back in the
    //JSON message (active/finished).

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set random UserID identifier
        Random rand = new Random();
        UserID = rand.nextInt(1000);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(
                getResources().getColor(android.R.color.transparent)));

        // Configure Cast device discovery
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getResources()
                        .getString(R.string.app_id))).build();
        mMediaRouterCallback = new MyMediaRouterCallback();
    }

    //This is linked to the Register button on the main screen
    //when the button is pressed this function is called and sends
    //two messages to the receiver app to make sure the user is
    //registered. Sends it's unique UserID.
    public void registerPlayer(View view)
    {
        JSONObject message = new JSONObject();

        try {
            message.put("UserID", UserID+"");
            message.put("commandtype", "register");
            message.put("direction", "none");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(message.toString());
        sendMessage(message.toString());
    }

    //These functions we unfortunately could not get to work, they were
    //supposed to be functions to restart the entire game and refresh the
    //receiver app to replay the game.

    /*
    public void restartGame()
    {
        JSONObject message = new JSONObject();
        try {
            message.put("UserID", UserID+"");
            message.put("commandtype", "restart");
            message.put("direction", "none");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendMessage(message.toString());
    }

    public void promptForRestart()
    {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Play again?")
                .setMessage("The game is over, would you like to play again?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartGame();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        teardown(true);
                    }
                })
                .show();
    }
    */

    //This function is the main button that gets called whenever a button is clicked.
    //It is linked to nearly every button except for the register button.
    public void buttonClicked(View view)
    {
        JSONObject message = new JSONObject();
        if (view == findViewById(R.id.upButton))
        {
            try {
                message.put("UserID", UserID+"");
                message.put("commandtype", "move");
                message.put("direction", "up");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(message.toString());

        }
        //When the button is clicked it checks through a series of if-elseif statements
        //which button was clicked. Then constructs the JSON message accordingly and then
        //sends it to the receiver app using the sendMessage function.
        else if (view == findViewById(R.id.downButton))
        {
            try {
                message.put("UserID", UserID+"");
                message.put("commandtype", "move");
                message.put("direction", "down");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(message.toString());
        }
        else if (view == findViewById(R.id.leftButton))
        {
            try {
                message.put("UserID", UserID+"");
                message.put("commandtype", "move");
                message.put("direction", "left");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(message.toString());
        }
        else if (view == findViewById(R.id.rightButton))
        {
            try {
                message.put("UserID", UserID+"");
                message.put("commandtype", "move");
                message.put("direction", "right");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(message.toString());
        }
        else if (view == findViewById(R.id.selectButton))
        {
            try {
                message.put("UserID", UserID+"");
                message.put("commandtype", "select");
                message.put("direction", "none");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendMessage(message.toString());
        }
        else if (view == findViewById(R.id.forfeitButton)) {

            try {
                message.put("UserID", UserID+"");
                message.put("commandtype", "forfeit");
                message.put("direction", "none");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            sendMessage(message.toString());

            //This function is the same as every other button function except for
            //when it is called, the app waits 6 seconds, and then calls the teardown
            //function which destroys the connection to the receiver app and shuts it down.

            //The waiting is to let the players clearly see that one of them has won the game.
            try {
                Thread.sleep(6000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            teardown(true);
        }
    }


    //This 3 functions were a part of the downloaded code, so the code that is
    //commented out was either to make what we wanted it to do work or to not use
    //whatever feature they had implemented.
    @Override
    protected void onStart() {
        super.onStart();
        // Start media router discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop() {
        // End media router discovery
        //mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        //teardown(true);
        super.onDestroy();
    }

    //This is code to, when the top menu bar generates it shows the Chromecast
    //logo so that you can activate it and start casting to a Chromecast.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);

        MediaRouteActionProvider mediaRouteActionProvider
                = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);


        // Set the MediaRouteActionProvider selector for device discovery.
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);

        return true;
    }

    //Class that handles communication events between the receiver and sender.
    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, RouteInfo info) {
            // Handle the user route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            launchReceiver();
        }

        //When the route is unselected it sets the device it is casting to to null.
        @Override
        public void onRouteUnselected(MediaRouter router, RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            //teardown(false);
            mSelectedDevice = null;
        }
    }

    //Function to start the receiver app. All code provided from Google.
    //Utilizes the Chromecast API.
    private void launchReceiver() {
        try {
            mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    //teardown(true);
                }

            };
            // Connect to Google Play services
            mConnectionCallbacks = new ConnectionCallbacks();

            mConnectionFailedListener = new ConnectionFailedListener();

            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mSelectedDevice, mCastListener);

            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();

            mApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    //Handles the Google Play service callbacks.
    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected");

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                            && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                        //teardown(true);
                    } else {
                        // Re-create the custom message channel
                        try {
                            Cast.CastApi.setMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace(),
                                    mHelloWorldChannel);
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while creating channel", e);
                        }
                    }
                } else {
                    //Launching the receiver app if there are no problems.
                    //Google provided code, all we had to do was enter the Application ID.
                    Cast.CastApi.launchApplication(mApiClient, getString(R.string.app_id), false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG,
                                                    "ApplicationConnectionResultCallback.onResult:"
                                                            + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();
                                                mSessionId = result.getSessionId();
                                                String applicationStatus = result
                                                        .getApplicationStatus();
                                                boolean wasLaunched = result.getWasLaunched();
                                                Log.d(TAG, "application name: "
                                                        + applicationMetadata.getName()
                                                        + ", status: " + applicationStatus
                                                        + ", sessionId: " + mSessionId
                                                        + ", wasLaunched: " + wasLaunched);
                                                mApplicationStarted = true;

                                                //Custom channel creation, left the name as HelloWorld
                                                //because it doesn't matter what it's called.
                                                mHelloWorldChannel = new HelloWorldChannel();
                                                try {
                                                    Cast.CastApi.setMessageReceivedCallbacks(
                                                            mApiClient,
                                                            mHelloWorldChannel.getNamespace(),
                                                            mHelloWorldChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG, "Exception while creating channel",
                                                            e);
                                                }
                                            } else {
                                                Log.e(TAG, "application could not launch");
                                                //teardown(true);
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        //When the connection is suspended it always waits for the player to reconnect.
        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }


    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {

        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed ");

            //teardown(false);
        }
    }

    //Function that tears down the connection to the receiver by setting the channel,
    //ApiClient, SelectedDevice, and SessionId to null. Also sets WaitingForReconnect to false.
    private void teardown(boolean selectDefaultRoute) {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected() || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mHelloWorldChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace());
                            mHelloWorldChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        if (selectDefaultRoute) {
            mMediaRouter.selectRoute(mMediaRouter.getDefaultRoute());
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }

    //Function to send a string message to the receiver app.
    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient,
                        mHelloWorldChannel.getNamespace(), message).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        } else {
            //Show the command that failed to send to the user on the screen if the Chromecast
            //is not connected or there is an error sending the message.
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    //Custom message/channel class. Mostly handles messages sent back and forth.
    class HelloWorldChannel implements MessageReceivedCallback {

        public String getNamespace() {
            return getString(R.string.namespace);
        }

        //Gets called when the sender receives a message from the receiver.
        //This is the main portion that was implemented for our game and
        //player control.
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                String message) {

            //Since the first thing you press is Register, after the sender receives
            //it's first message, we hide the Register button because it is of no use anymore.
            Button Register = (Button) findViewById(R.id.registerButton);
            Register.setVisibility(View.INVISIBLE);

            //What this does is it takes the string message that was received from the receiver
            //app that is formatted in JSON and turns it into an actual JSON object, that way
            //we can actually extract the information we want from it.
            try {
                JSONObject jObject = new JSONObject(message);
                PlayerNum = jObject.getInt("player");
                PlayerTurn = jObject.getBoolean("turn");
                GameStatus = jObject.getString("gamestatus");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //This was for error testing, only logs out in the debugger so the user does
            //not see this.
            Log.d("Info: ", "Player = "+PlayerNum);
            Log.d("Info: ", "Turn = "+PlayerTurn);
            Log.d("Info: ", "Game Status = "+GameStatus);

            //Then call the handleReceived function to build the message that is to be sent back.
            handleReceived();
        }

        public void handleReceived()
        {
            //To actually edit the text views and buttons on the screen you have to identify
            //them programatically.

            TextView playeridText = (TextView) findViewById(R.id.playeridText);
            TextView turnView = (TextView) findViewById(R.id.turnView);

            ImageButton Left = (ImageButton) findViewById(R.id.leftButton);
            ImageButton Right = (ImageButton) findViewById(R.id.rightButton);
            ImageButton Up = (ImageButton) findViewById(R.id.upButton);
            ImageButton Down = (ImageButton) findViewById(R.id.downButton);

            Button Select = (Button) findViewById(R.id.selectButton);
            Button Forfeit = (Button) findViewById(R.id.forfeitButton);


            playeridText.setText("Player "+PlayerNum);

            //Unimplemented code that was going to be used to implement a restart function.
            /*
            if (PlayerNum == 1 && GameStatus.equals("finished"))
            {
                promptForRestart();
            }
            */

            //If it is Player 1's turn, and then GameStatus is finished,
            //wait 6 seconds and then end them game.
            if (PlayerNum == 1 && GameStatus.equals("finished"))
            {
                try {
                    Thread.sleep(6000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                teardown(true);
            }

            //If PlayerTurn == True, which means if it's that player's turn
            //Set the text of the turn textview to "Your turn" and make all
            //buttons clickable, if not, make the buttons unclickable and make
            //the textview say waiting for turn.
            if (PlayerTurn)
            {
                turnView.setText("Your turn");
                Left.setClickable(true);
                Right.setClickable(true);
                Up.setClickable(true);
                Down.setClickable(true);
                Select.setClickable(true);
                Forfeit.setClickable(true);
            }
            else
            {
                turnView.setText("Waiting for turn");
                Left.setClickable(false);
                Right.setClickable(false);
                Up.setClickable(false);
                Down.setClickable(false);
                Select.setClickable(false);
                Forfeit.setClickable(false);
            }

        }


    }

}
