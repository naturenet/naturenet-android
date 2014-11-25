package org.naturenet.model;

import java.util.List;

import org.naturenet.rest.NatureNetAPI;
import org.naturenet.rest.NatureNetAPI.Result;
import org.naturenet.rest.NatureNetRestAdapter;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

@Table(name = "ACCOUNT", id = "tID")
public class Account extends NNModel {

    @Override
    protected String getModelName() {
	return "Account";
    }

    @Expose
    @Column(name = "Fullname")
    private String name;

    @Expose
    @Column(name = "Name")
    private String username;

    @Expose
    @Column(name = "Password")
    private String password;

    @Expose
    @Column(name = "Email")
    private String email;

    public String toString() {
	
	return Objects.toStringHelper(this).addValue(super.toString())
		.add("username", getUsername()).add("name", getName()).add("email", getEmail())
		.add("password", getPassword())
		// add("base", super.toString()).
		.toString();
    }

    @Override
    protected <T extends NNModel> T doPullByUID(NatureNetAPI api, long uID) {
	return (T) api.getAccount(uID).data;
    }

    @Override
    protected <T extends NNModel> T doPullByName(NatureNetAPI api, String name) {
	return (T) api.getAccount(name).data;
    }

    public int countNotes() {
	return new Select().from(Note.class).where("account_id = ?", getId()).count();
    }

    public List<Note> notes() {
	return new Select().from(Note.class).where("account_id = ?", getId()).execute();
    }

    public void pullNotes() {
	NatureNetAPI api = NatureNetRestAdapter.get();
	Result<List<Note>> r = api.listNotes(username);
	for (Note u : r.data) {
	    // if the image is deleted on server, don't download 
	    // added by Jinyue Xia
	    if (u.getStatus() != null && !u.getStatus().equals("deleted")) {
		    u.state = STATE.DOWNLOADED;
		    u.commit();
	    }
	}
    }

    public void pushNotes() {
	for (Note n : getNotes()) {
	    n.push();
	}
    }

    public void pushFeedbacks() {
	for (Feedback n : getFeedbacks()) {
	    n.push();
	}
    }

    public List<Note> getRecentNotes(int n) {
	return new Select().from(Note.class)
		.where("account_id = ? and kind = ?", getId(), "FieldNote").orderBy("tid DESC")
		.limit(n).execute();
    }

    public List<Note> getNotesOrderedByRecency() {
	return new Select().from(Note.class).where("account_id = ?", getId()).orderBy("tid DESC")
		.execute();
    }

    public List<Note> getNotesOrderedByRecencyAtSite(Site site) {
	List<Note> allNotes = new Select().from(Note.class)
		.where("account_id = ? and kind = ?", getId(), "FieldNote").orderBy("tid DESC")
		.execute();
	List<Note> filteredNotes = Lists.newArrayList();
	// Filter Notes by Site
	for (Note note : allNotes) {
	    if (note.getContext().getSite().getId() == site.getId()) {
		filteredNotes.add(note);
	    }
	}
	return filteredNotes;
    }

    public List<Note> getNotesOrderedByRecencyAtSite(long siteId) {
   	List<Note> allNotes = new Select().from(Note.class)
   		.where("account_id = ? and kind = ?", getId(), "FieldNote").orderBy("tid DESC")
   		.execute();
   	List<Note> filteredNotes = Lists.newArrayList();
   	// Filter Notes by Site
   	for (Note note : allNotes) {
   	    if (note.getContext().getSite().getId() == siteId) {
   		filteredNotes.add(note);
   	    }
   	}
   	return filteredNotes;
       }
    
    public List<Note> getNotes() {
	return new Select().from(Note.class).where("account_id = ?", getId()).execute();
    }

    public List<Feedback> getFeedbacks() {
	return new Select().from(Feedback.class).where("account_id = ?", getId()).execute();
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

    public String getPassword() {
	return password;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    public String getUsername() {
	return username;
    }

    public void setUsername(String username) {
	this.username = username;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

}
