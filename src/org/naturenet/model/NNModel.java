package org.naturenet.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.naturenet.rest.NatureNetAPI;
import org.naturenet.rest.NatureNetRestAdapter;

import retrofit.RetrofitError;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public abstract class NNModel extends Model {

	public NNModel(){
		super();
		uID = -1L;
		created_at = new Date().getTime();
		state = STATE.NEW;
	}
	
	abstract protected String getModelName();

	@Column(name="syncState")
	public Integer state;	
	public static class STATE{
		static public int NEW = 1;
		static public int SAVED = 2;
		static public int MODIFIED = 3;
		static public int SYNCED = 4;
		static public int DOWNLOADED = 5;
	};

	public Integer getSyncState() {
		return state;
	}	

	public void commit() {		
		if (state == STATE.NEW || state == STATE.SAVED){
			state = STATE.SAVED;
			save();
			Log.d(TAG, "saved " + this);
		}else if (state == STATE.SYNCED || state == STATE.MODIFIED){
			state = STATE.MODIFIED;
			save();	
			Log.d(TAG, "saved " + this);			
		}else if (state == STATE.DOWNLOADED){
			resolveDependencies();
			Model ret = (new Select()).from(getClass()).where("uid = ?", getUId()).executeSingle();
			if (ret == null){
				state = STATE.SYNCED;
				save();
				Log.d(TAG, "saved " + this);				
			}else{
				// compare time stamps to figure out who is newer
				// if remote is newer, copy over, or the opposite
			}
		}		
		doCommitChildren();
	}

	protected void resolveDependencies(){	
	}

	protected void doCommitChildren() {
	}

	protected void doPushChildren(NatureNetAPI api) {		
	}

	public void push(){
		NatureNetAPI api = NatureNetRestAdapter.get();
		if (state == STATE.SAVED){
			NNModel m = doPushNew(api);
			if (m != null){
				state = STATE.SYNCED;
				uID = m.getUId();
				save();
				Log.d(TAG , "pushed (N) " + this);
				doPushChildren(api);
			}
		}else if (state == STATE.MODIFIED){
			NNModel m = doPushChanges(api);
			state = STATE.SYNCED;
			save();			
			Log.d(TAG , "pushed (C) " + this);
		}
	}

	protected <T extends NNModel> T doPushNew(NatureNetAPI api){
		return null;
	}

	protected <T extends NNModel> T doPushChanges(NatureNetAPI api){
		return null;
	}	

	protected <T extends NNModel> T doPullByUID(NatureNetAPI api, long uID){
		return null;
	}

	protected <T extends NNModel> T doPullByName(NatureNetAPI api, String name){
		return null;
	}	

	public static <T extends NNModel> T resolveByUID(Class klass, long uID) {
		T model = findByUID(klass, uID);
		if (model == null){
			model = pullByUID(klass, uID);
			if (model != null){
				model.commit();
			}
		}
		return model;
	}

	public static <T extends NNModel> T resolveByName(Class klass, String name) {
		T model = findByName(klass, name);
		if (model == null){
			model = pullByName(klass, name);
			if (model != null){
				model.commit();
			}
		}
		return model;
	}	

	public static <T extends NNModel> T pullByUID(Class klass, long uID) {
		try {
			NatureNetAPI api = NatureNetRestAdapter.get();
			try{
				T obj = (T) klass.getDeclaredConstructor().newInstance();
				obj = obj.doPullByUID(api,uID);
				if (obj != null){
					obj.state = STATE.DOWNLOADED;
				}
				Log.d(TAG, "pulled " + obj);
				return obj;				
			}catch(RetrofitError r){
				return null;
			}

		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		}
		return null;
	}

	public static <T extends NNModel> T pullByName(Class klass, String name) {
		try {
			NatureNetAPI api = NatureNetRestAdapter.get();
			try{
				T obj = (T) klass.getDeclaredConstructor().newInstance();
				obj = obj.doPullByName(api,name);
				if (obj != null){
					obj.state = STATE.DOWNLOADED; 
				}
				return obj;				
			}catch(RetrofitError r){
				return null;
			}

		} catch (IllegalArgumentException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (InvocationTargetException e) {
		} catch (NoSuchMethodException e) {
		}
		return null;
	}		

	@Expose
	@Column(name = "UID")
	@SerializedName("id")
	public Long uID = -1L;

	@Expose
	@Column(name = "created")
	private Long created_at;

	public String toString(){
		return Objects.toStringHelper(this).		
				add("id", getId()).
				add("uid", uID).
				add("created_at", getTimeCreated()).
				add("state", state).
				toString();
	}

	static protected String TAG = "NatureNetModel";

	public void setUId(Long uid){
		uID = uid;
	}

	public Long getUId() {
		return uID;
	}

	public static int countLocal(Class clazz){
		return new Select().from(clazz).count();
	}

	public static int countLocal(Class clazz, int state){
		return new Select().from(clazz).where("syncState = ?", state).count();
	}

	public static <T extends NNModel> T findByUID(Class clazz, Long uid) {
		return new Select().from(clazz).where("uid = ?", uid).executeSingle();		
	}

	public static <T extends NNModel> T findByName(Class clazz, String name) {
		return new Select().from(clazz).where("name = ?", name).executeSingle();		
	}	

	/* clear context db, added by Jinyue Xia */
	public static <T extends NNModel> T deleteByName(Class clazz, String name) {
		return new Delete().from(clazz).where("name = ?", name).executeSingle();		
	}	
	
	public Long getTimeCreated() {
		return created_at;
	}

}
