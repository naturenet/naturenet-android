package org.naturenet.model;

import java.util.HashMap;
import java.util.Map;

import org.naturenet.rest.NatureNetAPI;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Table(name="CONTEXT", id="tID")
public class Context extends NNModel {

	@Override
	protected String getModelName() {
		return "Context";
	}
	
	@Expose
	@Column(name="Description")
	private String description;

	@Expose
	@Column(name="Kind")
	private String kind;

	@Expose
	@Column(name="Name")
	private String name;

	@Expose
	@Column(name="Site_ID")
	private Long site_id;

	@Expose
	@Column(name="Title")
	private String title;
	
	@Expose
	@Column(name="Extras")
	private String extras;

	public String toString(){
		return Objects.toStringHelper(this).
				add("id", getId()).
				add("uid", getUId()).
				add("name", getName()).
				add("description", getTitle()).	
				add("description", getDescription()).				
				add("site_id" , site_id).
				add("extras" , extras).
				toString();
	}
	
	public Map getExtras(){
		Gson gson = new GsonBuilder().create();
        Map p = gson.fromJson(extras, Map.class);
        return Objects.firstNonNull(p, new HashMap());
	}

	public Site getSite() {
		return Model.load(Site.class, site_id);
	}

	public static Context find_by_uid(Long uid) {
		return new Select().from(Context.class).where("uid = ?", uid).executeSingle();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSite(Site site){
		site_id = site.getId();		
	}

	@Override
	protected <T extends NNModel> T doPullByUID(NatureNetAPI api, long uID){
		return (T) api.getContext(uID).data;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
