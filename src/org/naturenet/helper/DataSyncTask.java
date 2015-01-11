package org.naturenet.helper;

import java.util.List;

import org.naturenet.fragments.ObservationFragment;
import org.naturenet.fragments.AddObservationFragment.onPassNewObservationListener;
import org.naturenet.fragments.ObservationFragment.NoteImage;
import org.naturenet.model.Account;
import org.naturenet.model.Feedback;
import org.naturenet.model.Note;

import com.activeandroid.query.Select;

import retrofit.RetrofitError;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.util.Log;

public class DataSyncTask extends AsyncTask<Void, Void, Boolean> {
    private final int SYNC_INTERVAL = 6000;
    private Account   account;
    private Note      note;
    private NoteImage image;
    private onPassNewObservationListener dataPasser;
    private int taskType;
    
    public final static int SYNC_DESIGNIDEA = 0;
    public final static int SYNC_NOTE = 1;
    public final static int UPDATE_NOTE = 2;
    public final static int SYNC_ALL = 3;
    
    public DataSyncTask() {
	
    }
    
    public DataSyncTask(int type) {
	this.taskType = type;
    }
    
    public DataSyncTask(Account mAccount) {
	this.account = mAccount;
    }
    
    /* coming a desgin idea */
    public DataSyncTask(Note note, int type) {
	this.note = note;
	this.taskType = type;
    }

    public DataSyncTask(Account mAccount, Note note, int type) {
	this.account = mAccount;
	this.note = note;
	this.taskType = type;
    }
    
    public DataSyncTask(Account account, Note note, NoteImage image,
	    onPassNewObservationListener dataPasser, int type) {
	super();
	this.account = account;
	this.note = note;
	this.image = image;
	this.dataPasser = dataPasser;
	this.taskType = type;
    }

    @Override
    protected void onPreExecute() {
	if (taskType == UPDATE_NOTE) {
	    // Log.d("debug", "in datasysnc note syncstate: " + note.getSyncState());
	    image.setNoteState(ObservationFragment.NoteImage.READY_TO_SEND);
	    dataPasser.onObservationStateChange(image);
	}
    }

    @Override
    protected Boolean doInBackground(Void... params) {
	try {
	    // this means note is design idea, to push note of design idea,
	    // it does not require user account information
	    if (taskType == SYNC_DESIGNIDEA) {
		if (account == null) {
		    note.push();
		    return true;
		}
	    }

	    if (taskType == SYNC_NOTE || taskType == UPDATE_NOTE) {
		List<Account> accounts = new Select().from(Account.class).execute();
		for (Account account : accounts) {
		    try {
			if (account.getId() == this.account.getId()) {
			    for (Note n : account.getNotes()) {
				if (n.getId() == note.getId()) {
				    note.push();
				}
			    }
			    // account.pushNotes();
			    account.pushFeedbacks();
			    return true;
			}
		    } catch (RetrofitError e) {
			e.printStackTrace();
		    }
		}
	    }

	    if (taskType == SYNC_ALL) {
		List<Account> accounts = new Select().from(Account.class).execute();
		for (Account account : accounts) {
		    try {
			account.pushNotes();
			account.pushFeedbacks();
		    } catch (RetrofitError e) {
			e.printStackTrace();
		    }
		}
	    }
	} catch (RetrofitError e) {
	    e.printStackTrace();
	}
	return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
	if (image == null) {
	    return;
	}
	
	if (taskType == UPDATE_NOTE) {
	    if (note.getSyncState() == 4) {
		Log.d("debug", "in datasysnc note syncstate: " + note.getSyncState());
		image.setNoteState(ObservationFragment.NoteImage.SENT);
		dataPasser.onPassNewObservation(image);
	    }
	}
    }

    @Override
    protected void onCancelled() {
    }
}
