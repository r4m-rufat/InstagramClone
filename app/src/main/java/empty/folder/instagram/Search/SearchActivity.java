package empty.folder.instagram.Search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import empty.folder.instagram.Home.HomeActivity;
import empty.folder.instagram.Likes.LikesActivity;
import empty.folder.instagram.Models.User;
import empty.folder.instagram.Profile.ProfileActivity;
import empty.folder.instagram.R;
import empty.folder.instagram.Search.SearchActivity;
import empty.folder.instagram.Share.ShareActivity;
import empty.folder.instagram.Utils.SearchListAdapter;

public class SearchActivity extends AppCompatActivity {

    Context context = this;

    // widgets
    private EditText searchBar;
    private ListView listView;

    // vars
    private List<User> userList;
    private SearchListAdapter searchListAdapter;


    public SearchActivity() {
    }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_search);
        searchBar = findViewById(R.id.eTextSearch);
        listView = findViewById(R.id.listViewForSearch);


        closeKeyboardForActivity();
        setupBottomNavigationViewEx();
        initializing();
    }

    private void initializing(){
        userList = new ArrayList<>();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = searchBar.getText().toString().toLowerCase(Locale.getDefault());
                Search(text);
            }
        });
    }

    private void Search(String username){
        userList.clear();
        if (username.length() == 0){

        }else{
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query = reference.
                    child(getString(R.string.dbname_users))
                    .orderByChild(getString(R.string.field_username))
                    .equalTo(username);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()){
                        userList.add(dataSnapshot.getValue(User.class));

                        // update search list view
                        updateUserList();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void updateUserList(){

        searchListAdapter = new SearchListAdapter(SearchActivity.this, R.layout.layout_search_item, userList);

        listView.setAdapter(searchListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // navigate to profile activity
                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra(getString(R.string.calling_activity), getString(R.string.search_activity));
                intent.putExtra(getString(R.string.intent_user), userList.get(position));
                startActivity(intent);

            }
        });

    }

    private void closeKeyboardForActivity(){
        if (getCurrentFocus() != null){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    private void setupBottomNavigationViewEx() {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewEx);
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
        bottomNavigationViewEx.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.id_home:
                        startActivity(new Intent(context, HomeActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_search:
                        startActivity(new Intent(context, SearchActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_circle:
                        startActivity(new Intent(context, ShareActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_alert:
                        startActivity(new Intent(context, LikesActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;

                    case R.id.id_profile:
                        startActivity(new Intent(context, ProfileActivity.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                }
                return false;
            }
        });
    }
}
