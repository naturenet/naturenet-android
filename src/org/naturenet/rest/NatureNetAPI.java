package org.naturenet.rest;

import java.util.List;

import org.naturenet.model.Account;
import org.naturenet.model.Context;
import org.naturenet.model.Feedback;
import org.naturenet.model.Media;
import org.naturenet.model.Note;
import org.naturenet.model.Site;

import com.google.gson.annotations.Expose;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

public interface NatureNetAPI {

	public class Result<T> {	
		@Expose
		public T data;
		@Expose
		public int status_code;
		@Expose
		public String status_txt;

	}

	String TAG = "NNAPI";

	@GET("/accounts")
	Result<List<Account>> listAccounts();

	@GET("/accounts/count")
	Result<Integer> countAccounts();

	@GET("/account/{username}")
	Result<Account> getAccount(@Path("username") String username);
	
	@GET("/account/{id}?field=id")
	Result<Account> getAccount(@Path("id") Long uid);	

	@FormUrlEncoded
	@POST("/account/new/{username}")
	Result<Account> createAccount(@Path("username") String username, 
			@Field("name") String name, @Field("password") String password,
			@Field("email") String email, @Field("consent") String consent); 	

	@GET("/account/{username}/notes")
	Result<List<Note>> listNotes(@Path("username") String username);

	@GET("/notes")
	Result<List<Note>> listNotes();

	@GET("/note/{id}")
	Result<Note> getNote(@Path("id") long id);


	@FormUrlEncoded
	@POST("/note/new/{username}")
	Result<Note> createNote(@Path("username") String username, 
			@Field("kind") String kind, @Field("content") String content, @Field("context") String context,
			@Field("latitude") Double latitude, @Field("longitude") Double longitude);

	@FormUrlEncoded
	@POST("/note/{id}/update")
	Result<Note> updateNote(@Path("id") Long uid, @Field("username") String username, 
			@Field("kind") String kind, @Field("content") String content, @Field("context") String context,
			@Field("latitude") Double latitude, @Field("longitude") Double longitude);	
	
	@Multipart
	@POST("/note/{id}/new/photo")
	Result<Media> createMedia(@Path("id") Long note_id, @Part("title") String title, @Part("file") TypedFile photo);

	@Multipart
	@POST("/note/{id}/new/photo")
	Result<Media> createMedia(@Path("id") Long note_id, @Part("title") String title, @Part("link") String link);

	
	@GET("/medias")
	Result<List<Media>> listMedias();

	@GET("/contexts")
	Result<List<Context>> listContexts();

	@GET("/context/{id}")
	Result<Context> getContext(@Path("id") Long id);

	@GET("/media/{id}")
	Result<Media> getMedia(@Path("id") long iD);

	@GET("/site/{name}/long")
	Result<Site> getSite(@Path("name") String name);

	@GET("/feedback/{id}")
	Result<Feedback> getFeedback(@Path("id") long uid);

	@FormUrlEncoded
	@POST("/feedback/new/{kind}/for/{model}/{id}/by/{username}")
	Result<Feedback> createFeedback(@Path("kind") String kind,
			@Path("model") String  model, @Path("id") Long id, @Path("username") String username, @Field("content") String content);

	@FormUrlEncoded
	@POST("/feedback/{id}/update")
	Result<Feedback> updateFeedback(@Path("id") Long id, @Field("username") String username, @Field("content") String content);
}

