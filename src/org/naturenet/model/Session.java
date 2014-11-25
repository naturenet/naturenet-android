package org.naturenet.model;

import android.util.Log;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

@Table(name="SESSION", id="tID")
public class Session extends Model{

	private static final String TAG = "Session";
	@Column(name="Account")
	public Long account_id = -1L;	

	@Column(name="Site")
	public Long site_id = -1L;		
	
	static Session getSingleton(){
		Session session = (new Select()).from(Session.class).executeSingle();
		if (session == null){
			session = new Session();			
			session.save();
		}
		return session;
	}
	
	static public boolean isSignedIn(){
		return getSingleton().account_id > 0;
	}
	
	static public Account getAccount(){
		return Model.load(Account.class, getSingleton().account_id);
	}	
	
	static public Site getSite(){
		return Model.load(Site.class, getSingleton().site_id);
	}
	
	/* Delete all tables in local database
	 * Added by Jinyue Xia */
	static public void clearDB() {
	    new Delete().from(Media.class).where("note_id > ?", 0).execute();	    
	    new Delete().from(Note.class).where("tID > ?", 0).execute();
	    new Delete().from(Feedback.class).where("target_id > ?", 0).execute();
	    new Delete().from(Account.class).where("tID > ?", 0).execute();
	    new Delete().from(Context.class).where("tID > ?", 0).execute();
	    new Delete().from(Site.class).where("tID > ?", 0).execute();
	}
	
	static public void signIn(Account account, Site site){
		Log.d(TAG,"sign in: " + account + " at " + site);
		Session session = getSingleton();
		session.account_id = account.getId();		
		session.site_id = site.getId();
		session.save();
	}
	
	/* this method is added to save a session by input account id and site id
	 * Added by Jinyue Xia */
	static public void signIn(long accoun_id, long site_id){
	    	Log.d(TAG,"sign in: " + accoun_id + " at " + site_id);
		Session session = getSingleton();
		session.account_id = accoun_id;		
		session.site_id = site_id;
		session.save();
	}

	public static void signOut() {
		Session session = getSingleton();
		clearDB(); // added by Jinyue Xia
		session.account_id = -1L;		
		session.site_id = -1L;		
		session.save();
	}
	
}
