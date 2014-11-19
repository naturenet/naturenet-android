package org.naturenet.fragments;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.naturenet.activities.R;
import org.naturenet.adapters.DesignIdeasListViewAdapter;
import org.naturenet.helper.DataSyncTask;
import org.naturenet.model.Account;
import org.naturenet.model.Note;
import org.naturenet.model.Session;
import org.naturenet.model.Site;

import com.activeandroid.Model;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class DesignIdeasFragment extends Fragment {
    private ListView lvIdeas;
    private int likes[] = new int[] {1, 4, 6, 10, 8};
    private String ideas[] = new String[] {"Design Idea 1", "Design Idea 2", 
	    "Design Idea 3", "Design Idea 4", "Design Idea 5"};
    public final static String TAG = DesignIdeasFragment.class.getName();
    private Account	     	mAccount;
    private Site		mSite;
    
    /* UI elements */
    private EditText etIdea;
    private Button btnIdeaSubmit;
    private Button btnIdeaCancel;
    
    public void initModel() {
	mAccount = Session.getAccount();
	checkNotNull(mAccount);
	mSite = Session.getSite();
	checkNotNull(mSite);
    }
    
    public static DesignIdeasFragment newInstance() {
	DesignIdeasFragment f = new DesignIdeasFragment();
	return f;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
	setHasOptionsMenu(true);
	initModel();
	
	View rootView = inflater.inflate(R.layout.fragment_design_ideas, container, false);
	rootView.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
			Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(etIdea.getWindowToken(), 0);
	    }
	});
//	lvIdeas = (ListView) rootView.findViewById(R.id.listview_design_ideas_holder);
//	DesignIdeasListViewAdapter adpater = new DesignIdeasListViewAdapter(getActivity(), ideas, likes);
//	lvIdeas.setAdapter(adpater);
	etIdea = (EditText) rootView.findViewById(R.id.editText_design_idea);
	InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	imm.showSoftInput(etIdea, InputMethodManager.SHOW_IMPLICIT);
	etIdea.requestFocus();
	btnIdeaSubmit = (Button) rootView.findViewById(R.id.button_submit_idea);
	btnIdeaSubmit.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		createIdea();
		popFragment();
	    }
	});
	
	btnIdeaCancel = (Button) rootView.findViewById(R.id.button_cancel_idea);
	btnIdeaCancel.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View arg0) {
		 popFragment();
	    }
	});
	return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().getActionBar()
            .setTitle(R.string.title_fragment_design_ideas);
    }
    
    @Override
    public void onDetach() {
	super.onDetach();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
		Context.INPUT_METHOD_SERVICE);
	imm.hideSoftInputFromWindow(etIdea.getWindowToken(), 0);
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	    case android.R.id.home:
		FragmentManager fm = getActivity().getSupportFragmentManager();
		fm.popBackStack();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	}
    }
    
    // create a new idea
    private void createIdea() {
	Note ideaNote = new Note();
	String idea = etIdea.getText().toString();
	if (idea == null || idea.length() == 0) {
	    return;
	}
	
	Account account = Model.load(Account.class, mAccount.getId());
	ideaNote.setAccount(account);
	ideaNote.setKind("DesignIdea");
	ideaNote.setLongitude(null);
	ideaNote.setLatitude(null);
	ideaNote.setContent(idea);
	List<org.naturenet.model.Context> ideasContexts = mSite.getContextIdeas();
	Log.d(TAG, "ideacontexts" + ideasContexts.get(0).toString());
	ideaNote.setContext(ideasContexts.get(0));
	ideaNote.commit();
	DataSyncTask mSyncTask = new DataSyncTask(ideaNote, DataSyncTask.SYNC_DESIGNIDEA);
	mSyncTask.execute();
	Toast.makeText(getActivity(), "Your idea has been sent, thanks!", Toast.LENGTH_SHORT).show();
    }
    
    private void popFragment() {
	FragmentManager fm = getActivity().getSupportFragmentManager();
	fm.popBackStack();
    }
}
